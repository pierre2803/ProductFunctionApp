package com.pierre2803.functionapp.product

import com.microsoft.azure.functions.HttpMethod
import com.microsoft.azure.functions.HttpResponseMessage
import com.pierre2803.functionapp.*
import com.pierre2803.functionapp.ApplicationError.Companion.NO_PRODUCT_FOUND
import com.pierre2803.functionapp.JsonUtils.fromJson
import com.pierre2803.functionapp.JsonUtils.toJson
import com.pierre2803.functionapp.TestData.Product1
import com.pierre2803.functionapp.TestData.Product2
import com.pierre2803.functionapp.TestData.Product3
import com.pierre2803.functionapp.TestData.Product4
import com.pierre2803.functionapp.TestData.defaultInvalidSortBy
import com.pierre2803.functionapp.TestData.defaultProduct
import com.pierre2803.functionapp.TestData.defaultProductCreationRequest
import com.pierre2803.functionapp.TestData.defaultProductUpdateRequest
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URI
import java.util.*
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
internal class ProductFunctionsTest {

    @MockK private lateinit var productService: ProductService

    private lateinit var function: ProductFunctions
    private lateinit var mockPostRequest: MockHttpRequestMessage<String>
    private lateinit var mockGetRequest: MockHttpRequestMessage<String>
    private lateinit var mockPutRequest: MockHttpRequestMessage<String>
    private lateinit var mockDeleteRequest: MockHttpRequestMessage<String>

    private lateinit var responseBuilder: MockHttpResponseMessageBuilder

    private val requestUri = "http://functionapp.pjmbusnel.io/api/products"

    @BeforeEach
    internal fun setUp() {
        function = ProductFunctions(productService)
        mockPostRequest = createMockHttpRequest(HttpMethod.POST)
        mockGetRequest = createMockHttpRequest(HttpMethod.GET)
        mockPutRequest = createMockHttpRequest(HttpMethod.PUT)
        mockDeleteRequest = createMockHttpRequest(HttpMethod.DELETE)
        responseBuilder = MockHttpResponseMessageBuilder()
    }

    @Test
    fun `expects error when no payload is provided in the request`() {
        // When
        val response = function.createProduct(mockPostRequest)
        // Then
        val errors = listOf(ValidationError(errorCode = "PRD_POST_400", errorMessage = "Invalid body has been provided in the request."))
        assertResponse(response, expectedStatus = 400, expectedErrors = errors)
    }

    @Test
    fun `expects error when the product is invalid`() {
        // Given
        mockPostRequest.requestBody = toJson(defaultProductCreationRequest.copy(name = null))
        // When
        val response = function.createProduct(mockPostRequest)
        // Then
        val errors = listOf(ValidationError(errorCode = "PRD_POST_10001", errorMessage = "No Product name provided."))
        assertResponse(response, expectedStatus = 400, expectedErrors = errors)
    }

    @Test
    fun `expects http 403 when the function fails to persist the product for a known reason`() {
        // Given
        mockPostRequest.requestBody = toJson(defaultProductCreationRequest)
        every { productService.createProduct(any()) } throws ConfigurationException(NO_PRODUCT_FOUND.forProduct().forCreation().args("UNKNOWN_PRODUCT"))
        // When
        val response = function.createProduct(mockPostRequest)
        // Then
        val errors = listOf(ValidationError(errorCode = "PRD_POST_1001", errorMessage = "Not allowed to access product 'UNKNOWN_PRODUCT' or the product does not exist."))
        assertResponse(response, expectedStatus = 403, expectedErrors = errors)
    }

    @Test
    fun `expects http 201 with a location for the newly created product`() {
        // Given
        mockPostRequest.requestBody = toJson(defaultProductCreationRequest)
        every { productService.createProduct(any()) } returns defaultProduct
        // When
        val response = function.createProduct(mockPostRequest)
        // Then
        assertThat(response.statusCode).isEqualTo(201)
        val locationHeader = response.getHeader("Location")
        assertNotNull(locationHeader)
        assertNotNull(response.body)
        val product = fromJson<Product>(response.body.toString())
        assertThat(locationHeader).isEqualTo("$requestUri/${product.id}")
        assertThat(product).isEqualTo(defaultProduct)
    }

    @Test
    fun `expects http 200 when a product matches the criteria`() {
        // Given
        every { productService.getProduct(any()) } returns defaultProduct
        // When
        val response = function.getProduct(mockGetRequest, productId = defaultProduct.id.toString())
        //Then
        assertThat(response.statusCode).isEqualTo(200)
        assertNotNull(response.body)
        val product = fromJson<Product>(response.body.toString())
        assertThat(product).isEqualTo(defaultProduct)
    }

    @Test
    fun `expects http 400 when the product to retrieve has an invalid id`() {
        // When
        val response = function.getProduct(mockGetRequest, productId = "invalid-uuid")
        //Then
        val errors = listOf(ValidationError("PRD_GET_1010", "Invalid UUID has been provided 'invalid-uuid'."))
        assertResponse(response, expectedStatus = 400, expectedErrors = errors)
    }

    @Test
    fun `expects http 403 when no product matches the criteria`() {
        //Given
        every { productService.getProduct(any()) } throws ConfigurationException(NO_PRODUCT_FOUND.forProduct().forRetrieval().args("UNKNOWN_PRODUCT"))
        // When
        val response = function.getProduct(mockGetRequest, defaultProduct.id.toString())
        //Then
        val errors = listOf(ValidationError(errorCode = "PRD_GET_1001", errorMessage = "Not allowed to access product 'UNKNOWN_PRODUCT' or the product does not exist."))
        assertResponse(response, expectedStatus = 403, expectedErrors = errors)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `expects 200 when sort by query parameter is null`() {
        // Given
        val activeProducts = listOf(Product1, Product4, Product2, Product3).filter { it.enabled }
        every { productService.getProducts(any()) } returns activeProducts
        // When
        val response = function.getProducts(mockGetRequest)
        //Then
        assertThat(response.statusCode).isEqualTo(200)
        assertNotNull(response.body)
        val products = fromJson<List<Product>>(response.body.toString())
        assertThat(products).containsExactly(*activeProducts.toTypedArray())
    }

    @Test
    fun `expects 400 when sort by query parameter is invalid`() {
        // Given
        mockGetRequest.queryParameters["sort"] = defaultInvalidSortBy
        // When
        val resp = function.getProducts(mockGetRequest)
        //Then
        val errors = listOf(ValidationError(errorCode = "PRD_GET_10004", errorMessage = "Sort parameter '$defaultInvalidSortBy' is invalid, supported values are 'name'."))
        assertResponse(resp, expectedStatus = 400, expectedErrors = errors)
    }


    @Test
    fun `expect 200 when updating an existing product with a valid name`() {
        // Given
        mockPutRequest.requestBody = toJson(defaultProductUpdateRequest)
        every { productService.updateProduct(any()) } returns defaultProduct
        // When
        val response = function.updateProduct(mockPutRequest, defaultProduct.id.toString())
        // Then
        assertThat(response.statusCode).isEqualTo(200)
        assertNotNull(response.body)
        val body = fromJson<Product>(response.body.toString())
        assertThat(body.id).isNotNull.isEqualTo(defaultProduct.id)
        assertThat(body.name).isNotNull.isEqualTo(defaultProduct.name)
    }

    @Test
    fun `expects http 400 when the product to update has an invalid id`() {
        //Given
        mockPutRequest.requestBody = toJson(defaultProductUpdateRequest)
        // When
        val response = function.updateProduct(mockPutRequest, productId = "invalid-uuid")
        //Then
        val errors = listOf(ValidationError("PRD_PUT_1010", "Invalid UUID has been provided 'invalid-uuid'."))
        assertResponse(response, expectedStatus = 400, expectedErrors = errors)
    }

    @Test
    fun `expect 400 error when the update request is invalid`() {
        // Given
        mockPutRequest.requestBody = toJson(ProductUpdateRequest(name = "@invalid@"))
        every { productService.updateProduct(any()) } returns defaultProduct
        // When
        val resp = function.updateProduct(mockPutRequest, defaultProduct.id.toString())
        // Then
        val errors = listOf(ValidationError(
                errorCode = "PRD_PUT_10003",
                errorMessage = "Product name contains invalid characters. Letters a to z, the dash '-' and the underscore '_' are accepted."))
        assertResponse(resp, expectedStatus = 400, expectedErrors = errors)
    }

    @Test
    fun `expect 403 error when the selected product to update doesn't exist`() {
        // Given
        mockPutRequest.requestBody = toJson(defaultProductUpdateRequest)
        every { productService.updateProduct(any()) } throws ConfigurationException(NO_PRODUCT_FOUND.forProduct().forUpdate().args("UNKNOWN_PRODUCT"))
        // When
        val resp = function.updateProduct(mockPutRequest, defaultProduct.id.toString())
        // Then
        val errors = listOf(ValidationError(errorCode = "PRD_PUT_1001", errorMessage = "Not allowed to access product 'UNKNOWN_PRODUCT' or the product does not exist."))
        assertResponse(resp, expectedStatus = 403, expectedErrors = errors)
    }

    private fun <T> createMockHttpRequest(method: HttpMethod): MockHttpRequestMessage<T> {
        val request = MockHttpRequestMessage<T>()
        request.requestHttpMethod = method
        request.requestUri = URI.create(requestUri)
        return request
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun assertResponse(response: HttpResponseMessage, expectedStatus: Int, expectedErrors: List<ValidationError>) {
        assertThat(response.statusCode).isEqualTo(expectedStatus)
        val body = response.body
        assertNotNull(body)
        assertThat(body.toString()).isNotBlank()
        val errorResponse = fromJson<ErrorResponse>(body.toString())
        assertThat(errorResponse.errors).containsExactly(*expectedErrors.toTypedArray())
    }
}