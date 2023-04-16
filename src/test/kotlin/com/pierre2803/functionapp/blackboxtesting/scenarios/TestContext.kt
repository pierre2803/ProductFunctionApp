package com.pierre2803.functionapp.blackboxtesting.scenarios

import com.microsoft.azure.functions.ExecutionContext
import com.microsoft.azure.functions.HttpResponseMessage
import com.microsoft.azure.servicebus.IMessageSender
import com.pierre2803.functionapp.product.Product
import com.pierre2803.functionapp.product.ProductCreationRequest
import com.pierre2803.functionapp.product.ProductFunctions
import com.pierre2803.functionapp.product.ProductUpdateRequest
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkClass
import java.util.*
import java.util.logging.Logger

class TestContext {

    val executionContext: ExecutionContext = mockkClass(ExecutionContext::class)
    private val logger: Logger = mockkClass(Logger::class)

    init {
        every { executionContext.logger } returns logger
        every { executionContext.logger.log(any(), any<String>()) } returns Unit
        every { executionContext.logger.log(any(), any(), any<Throwable>()) } returns Unit
    }

    var productFunctions: ProductFunctions? = null

    val input = Input()
    val results = Results()

    fun reset() {
        clearAllMocks()
        results.reset()
        input.reset()
        Ids.reset()
        productFunctions = null
    }
}

class Input {
    var productId: UUID? = null
    var isFinancialAdmin: Boolean = true
    var sortBy: String? = null
    var productCreationRequest: ProductCreationRequest? = null
    var productUpdateRequest: ProductUpdateRequest? = null
    var Products = mutableListOf<Product>()

    fun reset() {
        productId = null
        isFinancialAdmin = true
        sortBy = null
        productCreationRequest = null
        productUpdateRequest = null
        Products.clear()
    }
}

class Results {
    var response: HttpResponseMessage? = null
    fun reset() {
        response = null
    }
}
