package com.pierre2803.functionapp.product

import com.pierre2803.functionapp.InValidUUIDProvider
import com.pierre2803.functionapp.TestData.defaultProduct
import com.pierre2803.functionapp.TestData.defaultProductId
import com.pierre2803.functionapp.TestData.defaultProductUpdateRequest
import com.pierre2803.functionapp.ValidationException
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*
import java.util.stream.Stream

internal class ProductDBUpdateRequestTest {

    @ParameterizedTest
    @ArgumentsSource(InValidUUIDProvider::class)
    fun `expect an error when the product id is null`(invalid: String) {
        testForValidationError(defaultProductUpdateRequest, productId = invalid, expectedCode = "PRD_PUT_1010", expectedMessage = "Invalid UUID has been provided '$invalid'.")
    }

    @Test
    @Suppress("UsePropertyAccessSyntax")
    fun `expects Product when the ProductUpdateRequest is valid`() {
    // TODO fix test

        // When
        val product = defaultProductUpdateRequest.validateAndTransform(defaultProduct.id.toString())
        // Then
/*        assertThat(product).usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(defaultProduct)
        assertThat(product.id).isNotNull()*/
    }

    @Test
    fun `expects error when the ProductUpdateRequest name is not define`() {
        // Given
        val invalidProductRequest = defaultProductUpdateRequest.copy(name = null)
        // When & Then
        testForValidationError(invalidProductRequest, expectedCode = "PRD_PUT_10001", expectedMessage = "No Product name provided.")
    }

    @Test
    fun `expects error when the ProductUpdateRequest name is blank`() {
        // Given
        val blankProductRequest = defaultProductUpdateRequest.copy(name = " ")
        // When & Then
        testForValidationError(blankProductRequest, expectedCode = "PRD_PUT_10001", expectedMessage = "No Product name provided.")
    }

    @Test
    fun `expects error when the ProductUpdateRequest name is too long`() {
        //Given
        val longProductRequest = defaultProductUpdateRequest.copy(name = randomAlphanumeric(101))
        // When & Then
        testForValidationError(longProductRequest, expectedCode = "PRD_PUT_10002", expectedMessage = "Product name cannot be greater than 100 characters.")
    }

    @Test
    fun `expects error when the ProductUpdateRequest name has unsupported characters`() {
        // Given
        val invalidChars = defaultProductUpdateRequest.copy(name = "@invalid@")
        // When & Then
        testForValidationError(invalidChars, expectedCode = "PRD_PUT_10003",
            expectedMessage = "Product name contains invalid characters. Letters a to z, the dash '-' and the underscore '_' are accepted.")
    }

    @ParameterizedTest
    @ArgumentsSource(ValidCharacterProvider::class)
    fun `expects no error when the ProductUpdateRequest name has supported characters`(validName: String) {
        // Given
        val request = defaultProductUpdateRequest.copy(name = validName)
        // When
        val product = request.validateAndTransform(defaultProductId.toString())
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

    private fun testForValidationError(productRequest: ProductUpdateRequest, productId: String = defaultProductId.toString(), expectedCode: String, expectedMessage: String) {
        try {
            productRequest.validateAndTransform(productId)
            Assertions.fail<String>("ValidationException expected")
        } catch (ex: ValidationException) {
            //Then
            assertThat(ex.getApplicationErrors())
                    .extracting<Pair<String, String>> { Pair(it.getCode(), it.getMessage()) }
                    .containsExactly(Pair(expectedCode, expectedMessage))
        }
    }
}