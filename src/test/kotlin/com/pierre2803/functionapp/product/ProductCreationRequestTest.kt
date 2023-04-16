package com.pierre2803.functionapp.product

import com.pierre2803.functionapp.TestData.defaultProductCreationRequest
import com.pierre2803.functionapp.TestData.defaultProductDBCreation
import com.pierre2803.functionapp.ValidationException
import com.pierre2803.functionapp.db.ProductType
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

internal class ProductCreationRequestTest {

    @Test
    @Suppress("UsePropertyAccessSyntax")
    fun `expects Product when the ProductCreationRequest is valid`() {
        // When
        val product = defaultProductCreationRequest.copy(productType = "Online").validateAndTransform()
        // Then
        assertThat(product).usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(defaultProductDBCreation.copy(productType = ProductType.ONLINE))
        assertThat(product.id).isNotNull()
    }

    @Test
    fun `expects error when the ProductCreationRequest name is not define`() {
        // Given
        val invalidProductRequest = defaultProductCreationRequest.copy(name = null)
        // When & Then
        testForValidationError(invalidProductRequest, expectedCode = "PRD_POST_10001", expectedMessage = "No Product name provided.")
    }

    @Test
    fun `expects error when the ProductCreationRequest name is blank`() {
        // Given
        val blankProductRequest = defaultProductCreationRequest.copy(name = " ")
        // When & Then
        testForValidationError(blankProductRequest, expectedCode = "PRD_POST_10001", expectedMessage = "No Product name provided.")
    }

    @Test
    fun `expects error when the ProductCreationRequest name is too long`() {
        //Given
        val longProductRequest = defaultProductCreationRequest.copy(name = randomAlphanumeric(101))
        // When & Then
        testForValidationError(longProductRequest, expectedCode = "PRD_POST_10002", expectedMessage = "Product name cannot be greater than 100 characters.")
    }

    @Test
    fun `expects error when the ProductCreationRequest name has unsupported characters`() {
        // Given
        val invalidChars = defaultProductCreationRequest.copy(name = "@invalid@")
        // When & Then
        testForValidationError(invalidChars, expectedCode = "PRD_POST_10003",
                expectedMessage = "Product name contains invalid characters. Letters a to z, the dash '-' and the underscore '_' are accepted.")
    }

    @Test
    fun `expects error when the Product Type is invalid`() {
        // Given
        val invalidChars = defaultProductCreationRequest.copy(productType = "NEITHERONLINENORRETAIL")
        // When & Then
        testForValidationError(invalidChars, expectedCode = "PRD_POST_10006",
            expectedMessage = "Product Type 'NEITHERONLINENORRETAIL' is not valid. Use either Online or Retail.")
    }

    @ParameterizedTest
    @ArgumentsSource(ValidCharacterProvider::class)
    fun `expects no error when the ProductCreationRequest name has supported characters`(validName: String) {
        // When
        val accUpdateRequest = ProductCreationRequest(name = validName, productType = ProductType.ONLINE.value)
        val product = accUpdateRequest.validateAndTransform()
        // Then
        assertThat(product).isNotNull
        assertThat(product.name).isEqualTo(validName)
    }

    private class ValidCharacterProvider : ArgumentsProvider {
        override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
            return listOf(
                    "Te-st",
                    "Te_st",
                    "Te st",
                    "éèÊùö",
                    "AlphaNum24535")
                    .map { Arguments.arguments(it) }
                    .stream()
        }
    }

    @Test
    fun `expect an error when the product type is not supported`() {
        // Given
        val accUpdateRequest = defaultProductCreationRequest.copy(productType = "ABC")
        // When & Then
        testForValidationError(accUpdateRequest, expectedCode = "PRD_POST_10006", expectedMessage = "Product Type 'ABC' is not valid. Use either Online or Retail.")
    }

    private fun testForValidationError(productRequest: ProductCreationRequest, expectedCode: String, expectedMessage: String) {
        try {
            productRequest.validateAndTransform()
            Assertions.fail<String>("ValidationException expected")
        } catch (ex: ValidationException) {
            //Then
            assertThat(ex.getApplicationErrors())
                    .extracting<Pair<String, String>> { Pair(it.getCode(), it.getMessage()) }
                    .containsExactly(Pair(expectedCode, expectedMessage))
        }
    }
}