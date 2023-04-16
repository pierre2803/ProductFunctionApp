package com.pierre2803.functionapp.product

import com.pierre2803.functionapp.*
import com.pierre2803.functionapp.TestData.defaultProductId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*

@Suppress("SameParameterValue")
internal class ProductRetrievalRequestTest {

    @ParameterizedTest
    @ArgumentsSource(InValidUUIDProvider::class)
    fun `expect an error when the product id is invalid`(id: String) {
        val request = ProductRetrievalRequest(id)

        testForValidationError(request, expectedCode = "PRD_GET_1010", expectedMessage = "Invalid UUID has been provided '$id'.")
    }

    @Test
    @Suppress("UsePropertyAccessSyntax")
    fun `expects id when the product id is valid`() {
        // Given
        val request = ProductRetrievalRequest(defaultProductId.toString())
        // When
        val productId = request.validateAndTransform()
        // Then
        assertThat(productId).isEqualTo(defaultProductId)
    }

    private fun testForValidationError(productRequest: ProductRetrievalRequest, expectedCode: String, expectedMessage: String) {
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