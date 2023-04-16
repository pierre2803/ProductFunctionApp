package com.pierre2803.functionapp.blackboxtesting.scenarios

import com.microsoft.azure.functions.HttpMethod
import com.microsoft.azure.functions.HttpRequestMessage
import com.pierre2803.functionapp.JsonUtils.fromJson
import com.pierre2803.functionapp.JsonUtils.toJson
import com.pierre2803.functionapp.MockHttpRequestMessage
import com.pierre2803.functionapp.TestData.Product1
import com.pierre2803.functionapp.TestData.Product2
import com.pierre2803.functionapp.TestData.Product3
import com.pierre2803.functionapp.TestData.Product4
import com.pierre2803.functionapp.TestData.defaultProduct
import com.pierre2803.functionapp.TestData.defaultProductId
import com.pierre2803.functionapp.blackboxtesting.scenarios.MockRequestBuilder.mockHttpGetRequest
import com.pierre2803.functionapp.blackboxtesting.scenarios.MockRequestBuilder.mockHttpPostRequest
import com.pierre2803.functionapp.blackboxtesting.scenarios.MockRequestBuilder.mockHttpPutRequest
import com.pierre2803.functionapp.db.ProductType
import com.pierre2803.functionapp.product.Product
import com.pierre2803.functionapp.product.ProductCreationRequest
import com.pierre2803.functionapp.product.ProductUpdateRequest
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import java.util.*
import kotlin.test.assertNotNull

class ProductStepDefinitions(private val testContext: TestContext, private val databaseService: DatabaseService) {

    @Given("^a request for product creation with a name (.*)$")
    fun `a request for product creation with a name (name)`(name: String) {
        testContext.input.productCreationRequest = ProductCreationRequest(name = name, productType = ProductType.ONLINE.value)
    }

    @Given("^a request for an invalid product$")
    fun `a request for an invalid product`() {
        testContext.input.productCreationRequest = ProductCreationRequest(name = null, productType = ProductType.ONLINE.value)
    }

    @Given("^a request to update the selected product with name (.*)$")
    fun `a request to update the selected product with name (updateProductName)`(name: String) {
        testContext.input.productUpdateRequest = ProductUpdateRequest(name = name)
    }

    @Given("^a request to update the product (.*) with name (.*)$")
    fun `a request to update the product (productName) with name (name)`(productName: String, newName: String) {
        testContext.input.productId = Ids.toUUID(productName)
        testContext.input.productUpdateRequest = ProductUpdateRequest(name = newName)
    }

    @Given("^a request to disable the product (.*)$")
    fun `a request to disable the product (productName)`(productName: String) {
        testContext.input.productId = Ids.toUUID(productName)
    }

    @Given("^a product with name (\\w+) exists$")
    fun `a product with name (ProductName) exists`(name: String) {
        println("tptest12")
        val productId = UUID.randomUUID()
        println("tptest34")
        databaseService.addProduct(productId = productId, name = name, productType = ProductType.ONLINE, enabled = true)
        println("tptest56")
    }

    @Given("^a request to get a default product$")
    fun `a request to get a default product`() {
        testContext.input.productId = defaultProductId
    }

    @Given("^the disabled product$")
    fun `the disabled product`() {
        databaseService.addProduct(productId = defaultProduct.id, name = defaultProduct.name, productType = ProductType.ONLINE, enabled = false)
        testContext.input.productId = defaultProduct.id
    }

    @Given("^a request to get the unknown product (.*)$")
    fun `a request to get the unknown product (productName)`(name: String) {
        testContext.input.productId = Ids.toUUID(name)
    }

    @Given("^these enabled products: (.*)$")
    fun `these enabled products (csv)`(productNamesCsv: String) {
        val products = productNamesCsv.split(",")
            .map { it.trim() }
            .map { defaultProduct.copy(id = UUID.randomUUID(), name = it) }

        products.forEach { databaseService.addProduct(productId = it.id, name = it.name, productType = ProductType.ONLINE, enabled = it.enabled) }
        testContext.input.Products.addAll(products)
    }

    @Given("^the default products$")
    fun `the default products`() {
        val products = listOf(defaultProduct, Product1, Product2, Product3, Product4)
        products.forEach { databaseService.addProduct(it.id, it.name, ProductType.ONLINE, it.enabled) }
        testContext.input.Products.addAll(products)
    }

    @Given("^the user submits the products retrieval request$")
    fun `the user submits the products retrieval request`() {
        val request = mockProductsRetrievalHttpRequest(testContext.input.sortBy)
        testContext.results.response = testContext.productFunctions?.getProducts(request)
    }

    @When("^the user submits the product retrieval request$")
    fun `the user submits the product retrieval request`() {
        val productId = testContext.input.productId
        requireNotNull(productId)
        val request = mockHttpGetRequest<String>(path = "products/$productId")
        testContext.results.response = testContext.productFunctions?.getProduct(request, productId.toString())
    }

    @When("^the user submit the product creation request$")
    fun `the user submit the product creation request`() {
        val request = mockHttpPostRequest(path = "products", body = toJson(testContext.input.productCreationRequest))
        testContext.results.response = testContext.productFunctions?.createProduct(request)
    }

    @When("^the user submits the update product request$")
    fun `the user submits the update product request`() {
        val productId = testContext.input.productId
        requireNotNull(productId)
        val request = mockHttpPutRequest(path = "products/$productId", body = toJson(testContext.input.productUpdateRequest))
        testContext.results.response = testContext.productFunctions?.updateProduct(request, productId.toString())
    }

    @Then("^the new product is persisted$")
    fun `the new product is persisted`() {
        val request = testContext.input.productCreationRequest
        requireNotNull(request)

        val Product = fromJson<Product>(testContext.results.response?.body.toString())

        val dbProduct = databaseService.getProduct(Product.id)
        assertNotNull(dbProduct)
        assertThat(dbProduct.id).isEqualTo(Product.id)
        assertThat(dbProduct.name).isEqualTo(request.name)
        assertThat(dbProduct.enabled).isTrue
        assertThat(dbProduct.productType).isEqualTo(ProductType.ONLINE)
        assertThat(dbProduct.creationTime).isNotNull
        assertThat(dbProduct.lastUpdateTime).isEqualTo(dbProduct.creationTime)
    }

    @Then("^the new product is returned$")
    fun `the new product is returned`() {
        val request = testContext.input.productCreationRequest
        requireNotNull(request)

        val Product = fromJson<Product>(testContext.results.response?.body.toString())

        assertThat(Product.id).isNotNull
        assertThat(Product.name).isEqualTo(request.name)
        assertThat(Product.enabled).isTrue
    }

    @Then("^the product is returned$")
    fun `the product is returned`() {
        val productId = testContext.input.productId
        requireNotNull(productId)

        val Product = fromJson<Product>(testContext.results.response?.body.toString())

        val dbProduct = databaseService.getProduct(productId)
        assertNotNull(dbProduct)

        assertThat(Product.id).isEqualTo(productId)
        assertThat(Product.name).isEqualTo(dbProduct.name)
        assertThat(Product.enabled).isEqualTo(dbProduct.enabled)
    }

    @Then("^the updated product is persisted$")
    fun `the updated product is persisted`() {
        val productId = testContext.input.productId
        requireNotNull(productId)
        val request = testContext.input.productUpdateRequest
        requireNotNull(request)

        val dbProduct = databaseService.getProduct(productId)
        assertNotNull(dbProduct)

        assertThat(dbProduct.name).isEqualTo(request.name)
        assertThat(dbProduct.lastUpdateTime).isGreaterThan(dbProduct.creationTime)
    }

    @Then("^the updated product is returned$")
    fun `the updated product is returned`() {
        val response = testContext.results.response
        requireNotNull(response)
        val productId = testContext.input.productId
        requireNotNull(productId)

        val Product = fromJson<Product>(response.body.toString())

        val dbProduct = databaseService.getProduct(productId)
        assertNotNull(dbProduct)

        assertThat(Product.id).isEqualTo(productId)
        assertThat(Product.name).isEqualTo(dbProduct.name)
        assertThat(Product.enabled).isEqualTo(dbProduct.enabled)
    }

    @Then("^the disabled product is persisted$")
    fun `the disabled product is persisted`() {
        val productId = testContext.input.productId
        requireNotNull(productId)

        val dbProduct = databaseService.getProduct(productId)
        assertNotNull(dbProduct)
        assertThat(dbProduct.enabled).isFalse
    }

    @Then("^these products are returned in this order: (.*)$")
    fun `these products are returned in this order (csv)`(productNamesCsv: String) {
        val Products = fromJson<List<Product>>(testContext.results.response?.body.toString())
        val expectedProducts = productNamesCsv.split(",")
            .map { it.trim() }
            .map { findProductByName(it, testContext.input.Products) }
        assertThat(Products).containsExactly(*expectedProducts.toTypedArray())
    }

    @Then("no products are returned")
    fun `no products are returned`() {
        val Products = fromJson<List<Product>>(testContext.results.response?.body.toString())
        assertThat(Products).isEmpty()
    }

    @Then("^the response should contains a location header for the newly created product$")
    fun `the response should contains a location header for the newly created product`() {
        val location = testContext.results.response?.getHeader("Location")
        assertNotNull(location)
        val loc = location.substring(0, location.lastIndexOf("/") + 1)
        assertThat(loc).contains("http://functionapp.pjmbusnel.io/api/products/")
    }

    private fun findProductByName(name: String, Products: List<Product>) = Products.first { it.name == name }

    private fun mockProductsRetrievalHttpRequest(sortBy: String?): HttpRequestMessage<String> {
        val mockRequest = MockHttpRequestMessage<String>()
        mockRequest.requestHttpMethod = HttpMethod.GET
        if (sortBy.isNullOrBlank()) {
            mockRequest.queryParameters["sort"] = "name"
            mockRequest.requestUri = URI.create("http://functionapp.pjmbusnel.io/api/products?sort=name")
        } else {
            mockRequest.queryParameters["sort"] = sortBy.trim()
            mockRequest.requestUri = URI.create("http://functionapp.pjmbusnel.io/api/products?sort=${sortBy.trim()}")
        }
        return mockRequest
    }
}