package com.pierre2803.functionapp.blackboxtesting.scenarios

import com.pierre2803.functionapp.TestData.defaultProductId
import com.pierre2803.functionapp.Validations
import com.pierre2803.functionapp.db.ProductDBCreation
import com.pierre2803.functionapp.db.ProductType
import com.pierre2803.functionapp.db.ProductsRepository
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import io.zonky.test.db.postgres.embedded.FlywayPreparer
import java.sql.Connection
import java.util.*
import kotlin.random.Random

private var databaseConnectionPool: javax.sql.DataSource? = null
private var flywayPreparer: FlywayPreparer? = null
private val clearTablesSQLs = Validations::class.java.getResource("/database/clearSchema.sql").readText()

class DatabaseService {

    init {
        databaseConnectionPool = databaseConnectionPool ?: createDatabase()
    }

    fun getDatabaseConnectionPool() = databaseConnectionPool!!

    private fun createDatabase(): javax.sql.DataSource {
        val port = Random.nextInt(50005, 50555)
        val ds: javax.sql.DataSource = EmbeddedPostgres.builder().setPort(port).start().postgresDatabase
        ds.connection.use { createSchema(connection = it, ds) }
        return ds
    }

    private fun createSchema(connection: Connection, dataSource: javax.sql.DataSource) {
        val resultSet = connection.createStatement().executeQuery("SELECT count(*) as db_count FROM pg_database WHERE datname = 'product_db'")
        resultSet.next()
        val dbExists = (resultSet.getInt("db_count") > 0)
        if (dbExists) {
            connection.createStatement().executeUpdate("DROP DATABASE product_db")
        }
        connection.createStatement().executeUpdate("CREATE DATABASE product_db")
        flywayPreparer = FlywayPreparer.forClasspathLocation("database/migration")
        flywayPreparer?.prepare(dataSource)
    }

    fun reset() = getDatabaseConnectionPool().connection.use { it.createStatement().executeUpdate(clearTablesSQLs) }

    fun addProduct(productId: UUID = defaultProductId, name: String, productType: ProductType, enabled: Boolean) {
        ProductsRepository(getDatabaseConnectionPool()).createProduct(
            ProductDBCreation(
                id = productId,
                name = name,
                productType = productType,
                enabled = enabled
            )
        )
    }

    fun getProduct(productId: UUID) = ProductsRepository(getDatabaseConnectionPool()).getProduct(productId)
}