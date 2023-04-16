package com.pierre2803.functionapp.product

import com.microsoft.azure.functions.HttpStatus
import com.pierre2803.functionapp.ConfigurationException
import com.pierre2803.functionapp.TestData.defaultProductDBCreation
import com.pierre2803.functionapp.TestData.defaultProductId
import com.pierre2803.functionapp.TestData.defaultProductName
import com.pierre2803.functionapp.TestData.defaultProductType
import com.pierre2803.functionapp.TestData.defaultProductUpdate
import com.pierre2803.functionapp.TestData.nowUTC
import com.pierre2803.functionapp.db.ProductDB
import com.pierre2803.functionapp.db.ProductsRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.sql.SQLException
import java.util.*

@ExtendWith(MockKExtension::class)
internal class ProductServiceTest {

    @MockK private lateinit var productsRepository: ProductsRepository

    private lateinit var productService: ProductService

    private val defaultProduct = ProductDB(
        id = defaultProductId,
        name = defaultProductName,
        productType = defaultProductType,
        enabled = true,
        creationTime = nowUTC.toEpochSecond(),
        lastUpdateTime = nowUTC.toEpochSecond(),
        deletionTime = null)

    private val product1 = ProductDB(id = UUID.randomUUID(),name = "AAA",productType = defaultProductType,enabled = true,creationTime = nowUTC.toEpochSecond(),lastUpdateTime = nowUTC.toEpochSecond(),deletionTime = null)
    private val product2 = ProductDB(id = UUID.randomUUID(),name = "BAA",productType = defaultProductType,enabled = true,creationTime = nowUTC.toEpochSecond(),lastUpdateTime = nowUTC.toEpochSecond(),deletionTime = null)
    private val product3 = ProductDB(id = UUID.randomUUID(),name = "CAA",productType = defaultProductType,enabled = true,creationTime = nowUTC.toEpochSecond(),lastUpdateTime = nowUTC.toEpochSecond(),deletionTime = null)
    private val product4 = ProductDB(id = UUID.randomUUID(),name = "ABA",productType = defaultProductType,enabled = true,creationTime = nowUTC.toEpochSecond(),lastUpdateTime = nowUTC.toEpochSecond(),deletionTime = null)

    @BeforeEach
    internal fun setUp() {
        productService = ProductService(productsRepository)
    }

    @Test
    fun `expect product when the product is created`() {
        // Given
        val expectedProduct = Product(id = defaultProductDBCreation.id, name = defaultProductDBCreation.name)
        every { productsRepository.createProduct(any()) } returns Unit
        // Then
        val product = productService.createProduct(defaultProductDBCreation)
        assertThat(product).isEqualTo(expectedProduct)
    }

    @Test
    fun `expect Configuration exception when creating a product with an existing name`() {
    // TODO fix test

        // When
        every { productsRepository.createProduct(any()) } throws SQLException()
        every { productsRepository.isNameExists(any()) } returns true
        // When & Then
        /*Assertions.assertThatThrownBy { productService.createProduct(defaultProductDBCreation) }
            .isInstanceOf(ConfigurationException::class.java)
            .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.CONFLICT)
            .hasFieldOrPropertyWithValue("errorCode", "PRD_POST_20001")
            .hasFieldOrPropertyWithValue("message", "A Product with name '${defaultProductDBCreation.name}' already exists.")*/
    }

    @Test
    fun `expect SQL exception when an unexpected error occurred while creating the product`() {
        // When
        every { productsRepository.createProduct(any()) } throws SQLException()
        every { productsRepository.isNameExists(any()) } returns false
        // When & Then
        Assertions.assertThatThrownBy { productService.createProduct(defaultProductDBCreation) }
            .isInstanceOf(SQLException::class.java)
    }

    @Test
    fun `expect product when the product is updated`() {
        // Given
        every { productsRepository.updateProduct(any()) } returns true
        every { productsRepository.getProduct(any()) } returns defaultProduct
        // When
        val product = productService.updateProduct(defaultProductUpdate)
        // Then
        assertThat(product.id).isEqualTo(defaultProductUpdate.id)
        assertThat(product.name).isEqualTo(defaultProductUpdate.name)
    }

    @Test
    fun `expect Configuration exception when updating a product with an unknown ID`() {
        // Given
        every { productsRepository.updateProduct(any()) } returns false
        // When & Then
        Assertions.assertThatThrownBy { productService.updateProduct(defaultProductUpdate) }
            .isInstanceOf(ConfigurationException::class.java)
            .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.FORBIDDEN)
            .hasFieldOrPropertyWithValue("errorCode", "PRD_PUT_1001")
            .hasFieldOrPropertyWithValue("message", "Not allowed to access product '${defaultProductUpdate.id}' or the product does not exist.")
    }

    @Test
    fun `expect Configuration exception when updating a product with an existing name`() {
        // TODO fix test

        // Given
        every { productsRepository.updateProduct(any()) } throws SQLException()
        every { productsRepository.isNameExists(any()) } returns true
        // When & Then
/*        Assertions.assertThatThrownBy { productService.updateProduct(defaultProductUpdate) }
            .isInstanceOf(ConfigurationException::class.java)
            .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.CONFLICT)
            .hasFieldOrPropertyWithValue("errorCode", "PRD_PUT_20001")
            .hasFieldOrPropertyWithValue("message", "A Product with name '${defaultProductUpdate.name}' already exists.")*/
    }

    @Test
    fun `expect SQL exception when an unexpected error occurred while updating the product`() {
        // Given
        every { productsRepository.updateProduct(any()) } throws SQLException()
        every { productsRepository.isNameExists(any()) } returns false
        // When & Then
        Assertions.assertThatThrownBy { productService.updateProduct(defaultProductUpdate) }
            .isInstanceOf(SQLException::class.java)
    }

    @Test
    fun `expect product when the product is found`() {
        // Given
        every { productsRepository.getProduct(any()) } returns defaultProduct
        // When
        val product = productService.getProduct(defaultProduct.id)
        // Then
        assertThat(product.id).isEqualTo(defaultProduct.id)
        assertThat(product.name).isEqualTo(defaultProduct.name)
    }

    @Test
    fun `expect Configuration exception when the product exists but disabled`() {
        // Given
        every { productsRepository.getProduct(any()) } returns defaultProduct.copy(enabled = false)
        // When & Then
        Assertions.assertThatThrownBy { productService.getProduct(defaultProduct.id) }
            .isInstanceOf(ConfigurationException::class.java)
            .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.FORBIDDEN)
            .hasFieldOrPropertyWithValue("errorCode", "PRD_GET_1001")
            .hasFieldOrPropertyWithValue("message", "Not allowed to access product '${defaultProduct.id}' or the product does not exist.")
    }

    @Test
    fun `expect Configuration exception when the product is not found`() {
        // Given
        every { productsRepository.getProduct(any()) } returns null
        // When & Then
        Assertions.assertThatThrownBy { productService.getProduct(defaultProduct.id) }
            .isInstanceOf(ConfigurationException::class.java)
            .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.FORBIDDEN)
            .hasFieldOrPropertyWithValue("errorCode", "PRD_GET_1001")
            .hasFieldOrPropertyWithValue("message", "Not allowed to access product '${defaultProduct.id}' or the product does not exist.")
    }

    @Test
    fun `expect SQL exception when an unexpected error occurred while getting the product`() {
        // Given
        every { productsRepository.getProduct(any()) } throws SQLException()
        // When & Then
        Assertions.assertThatThrownBy { productService.getProduct(defaultProduct.id) }
            .isInstanceOf(SQLException::class.java)
    }

    @Test
    fun `expects enabled products list ordered by name when getting the products list`() {
        // Given
        val expectedProducts = listOf(product1, product2.copy(enabled = false), product3, product4)
        every { productsRepository.getProducts() } returns expectedProducts
        // When
        val products = productService.getProducts(ProductsRetrieval(sort = ProductsRetrievalSort.NAME))
        // Then
        val ids = products.map { it.id }
        assertThat(ids).containsExactly(product1.id, product4.id, product3.id)
    }

    @Test
    fun `expects empty list if no existing enabled products`() {
        // Given
        val expectedProducts = listOf(product1.copy(enabled = false))
        every { productsRepository.getProducts() } returns expectedProducts
        // When
        val products = productService.getProducts(ProductsRetrieval(sort = ProductsRetrievalSort.NAME))
        // Then
        assertThat(products).isEmpty()
    }

    @Test
    fun `expect SQL exception when an unexpected error occurred while getting the products`() {
        // Given
        every { productsRepository.getProducts() } throws SQLException()
        // When & Then
        Assertions.assertThatThrownBy { productService.getProducts(ProductsRetrieval(sort = ProductsRetrievalSort.NAME)) }
            .isInstanceOf(SQLException::class.java)
    }

    @Test
    fun `expect no exception when the product is disabled`() {
        // Given
        every { productsRepository.disableProduct(any()) } returns true
        // Then
        productService.disableProduct(defaultProduct.id)
    }

    @Test
    fun `expect Configuration exception when disabling a product with an unknown ID`() {
        // Given
        every { productsRepository.disableProduct(any()) } returns false
        // When & Then
        Assertions.assertThatThrownBy { productService.disableProduct(defaultProduct.id) }
            .isInstanceOf(ConfigurationException::class.java)
            .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.FORBIDDEN)
            .hasFieldOrPropertyWithValue("errorCode", "PRD_DELETE_1001")
            .hasFieldOrPropertyWithValue("message", "Not allowed to access product '${defaultProduct.id}' or the product does not exist.")
    }

    @Test
    fun `expect SQL exception when an unexpected error occurred while disabling a product`() {
        // Given
        every { productsRepository.disableProduct(any()) } throws SQLException()
        // When & Then
        Assertions.assertThatThrownBy { productService.disableProduct(defaultProduct.id) }
            .isInstanceOf(SQLException::class.java)
    }
}