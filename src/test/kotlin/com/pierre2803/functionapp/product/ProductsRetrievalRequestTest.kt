package com.pierre2803.functionapp.product

import com.pierre2803.functionapp.TestData.defaultInvalidSortBy
import com.pierre2803.functionapp.ValidationException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@Suppress("UsePropertyAccessSyntax", "SameParameterValue")
internal class ProductsRetrievalRequestTest {


    @Test
    fun `expects default sort when the sort option is unspecified`() {
        // When
        val productRequestSort = ProductsRetrievalRequest("")
        val productsRetrieval = productRequestSort.validateAndTransform()
        // Then
        assertThat(productsRetrieval.sort).isEqualTo(ProductsRetrievalSort.NAME)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "   "])
    fun `expects default sort when the sort option is blank or empty`(sortBy: String) {
        // Given
        val productRequestSort = ProductsRetrievalRequest(sortBy)
        // When
        val productsRetrieval = productRequestSort.validateAndTransform()
        // Then
        assertThat(productsRetrieval.sort).isEqualTo(ProductsRetrievalSort.NAME)
    }

    @Test
    fun `expects a ValidationException when the sort option is not supported`() {
        // Given
        val productRetrievalRequest = ProductsRetrievalRequest(defaultInvalidSortBy)
        // When & Then
        testForValidationError(productRetrievalRequest, expectedErrorCode = "PRD_GET_10004", expectedErrorMessage = "Sort parameter '$defaultInvalidSortBy' is invalid, supported values are 'name'.")
    }

    private fun testForValidationError(request: ProductsRetrievalRequest, expectedErrorCode: String, expectedErrorMessage: String) {
        try {
            request.validateAndTransform()
            Assertions.fail<String>("ValidationException expected")
        } catch (ex: ValidationException) {
            // Then
            assertThat(ex.getApplicationErrors())
                .extracting<Pair<String, String>> { Pair(it.getCode(), it.getMessage()) }
                .containsExactly(Pair(expectedErrorCode, expectedErrorMessage))
        }
    }
}