package com.pierre2803.functionapp.blackboxtesting.scenarios

import com.pierre2803.functionapp.product.ProductFunctions
import com.pierre2803.functionapp.product.ProductService
import com.pierre2803.functionapp.db.ProductsRepository
import io.cucumber.java.After
import io.cucumber.java.Before
import io.mockk.every

class Setup(private val testContext: TestContext, private val databaseService: DatabaseService) {

    @Before
    fun setup() {
        testContext.reset()
        databaseService.reset()

        val databaseConnectionPool = databaseService.getDatabaseConnectionPool()

        val productsRepository = ProductsRepository(databaseConnectionPool)
        val productService = ProductService(productsRepository)

        testContext.productFunctions = ProductFunctions(productService)
    }

    @After
    fun clear() {
        testContext.reset()
        databaseService.reset()
    }
}
