// Database.kt
import com.lccuellar.models.*
import com.lccuellar.services.hashPassword
import org.jetbrains.exposed.sql.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(isDevelopment: Boolean = true) {
        val database = if (isDevelopment) {
            Database.connect(
                url = "jdbc:h2:./data/testdb;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE",
                driver = "org.h2.Driver",
                user = "sa",
                password = ""
            )
        } else {
            Database.connect(hikari())
        }

        transaction {
            println("Creating tables")
            SchemaUtils.create(Users, Cities, CoffeeShops, Scores)
            println("Tables created")

            val query = """
                SELECT TABLE_NAME 
                FROM INFORMATION_SCHEMA.TABLES 
                WHERE TABLE_SCHEMA = 'PUBLIC'
            """

            // Query to see all tables
            val results = connection.prepareStatement(query, false).executeQuery()
            println("Found tables:")
            while (results.next()) {
                println(results.getString("TABLE_NAME"))
            }

            if(User.find {Users.username eq "lccuellar"}.empty()) {
                val non_hashed_password = "password"
                User.new {
                    username = "lccuellar"
                    password = hashPassword(non_hashed_password)
                }
            }

            if(Cities.selectAll().empty()) {
                println("Creating Cities")
                val cities = listOf(
                    mapOf(
                        "name" to "El Paso",
                        "state" to "Texas",
                    ),
                    mapOf(
                        "name" to "Dallas",
                        "state" to "Texas"
                    ),
                    mapOf(
                        "name" to "Chicago",
                        "state" to "Illinois"
                    )
                )
                cities.forEach{
                    val cityName = it["name"] ?: return@forEach
                    val cityState = it["state"] ?: return@forEach
                    City.new {
                        name = cityName
                        state = cityState
                    }
                }
            }

            if(CoffeeShops.selectAll().empty()) {
                transaction {
                    val allCities = Cities.selectAll().toList()

                    data class CoffeeShopInsert(val cityID: Int, val name: String, val date: LocalDate, val address: String)
                    val coffeeShops = listOf(
                        CoffeeShopInsert(allCities[0][Cities.id].value,
                            "Axiom",
                            LocalDate(2024, 12, 20),
                            "12001 Spire Hill"
                        ),
                        CoffeeShopInsert(allCities[1][Cities.id].value,
                            "Bee",
                            LocalDate(2025,12,25),
                            "955 Banner Hill")
                    )

                    coffeeShops.forEach {
                        CoffeeShop.new {
                            city = City[it.cityID]
                            name = it.name
                            date = it.date.toJavaLocalDate()
                            address = it.address
                        }
                    }
                }
            }

            if(Scores.selectAll().empty()) {
                transaction {
                    val allCoffeeShops = CoffeeShops.selectAll().toList()
                    val allUsers = Users.selectAll().toList()

                    data class ScoreInsert(val userID: Int, val coffeeShopID: Int, val scoreNum: Float, val scoreType: String, val notes: String?)
                    val scores = listOf(
                        ScoreInsert(
                            allUsers[0][Users.id].value,
                            allCoffeeShops[0][CoffeeShops.id].value,
                            8.5f,
                            "Cold Brew",
                            "No notes"
                        ),
                        ScoreInsert(
                            allUsers[0][Users.id].value,
                            allCoffeeShops[1][CoffeeShops.id].value,
                            9.2f,
                            "Iced Latte",
                            "Some notes"
                        )
                    )

                    scores.forEach {
                        Score.new {
                            user = User[it.userID]
                            coffeeShop = CoffeeShop[it.coffeeShopID]
                            scoreNum = it.scoreNum
                            scoreType = it.scoreType
                            notes = it.notes
                        }
                    }
                }
            }
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "com.mysql.cj.jdbc.Driver"
        config.jdbcUrl = System.getenv("JDBC_URL") ?:
                "jdbc:mysql://localhost:3306/coffeeshops"
        config.username = System.getenv("DB_USER") ?: "root"
        config.password = System.getenv("DB_PASSWORD") ?: "password"
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"

        // Optional settings for better production performance
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

        return HikariDataSource(config)
    }
}