package com.pierre2803.functionapp.blackboxtesting.scenarios

import com.pierre2803.functionapp.TestData.defaultProduct
import com.pierre2803.functionapp.TestData.defaultProductId
import com.pierre2803.functionapp.db.ProductType
import io.cucumber.java.PendingException
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import org.assertj.core.api.Assertions.assertThat
import org.skyscreamer.jsonassert.JSONAssert
import java.util.*

class CommonStepDefinitions(private val testContext: TestContext, private val databaseService: DatabaseService) {

    companion object {
        fun replaceNamesByUUIDs(expectedBodyTemplate: String): String {
            val regex = Regex(pattern = "\\\$\\{" + """replaceByUUID\("(.*)"\)""" + "}", options = setOf(RegexOption.MULTILINE, RegexOption.UNIX_LINES))
            val matchResults = regex.findAll(expectedBodyTemplate).flatMap { it.destructured.toList() }
            val namesToUUID = matchResults.map { Pair("\${replaceByUUID(\"$it\")}", "${Ids.toUUID(it)}") }.toList()
            var resultingJson = expectedBodyTemplate
            namesToUUID.forEach { pair -> resultingJson = resultingJson.replace(pair.first, pair.second, true) }
            return resultingJson
        }
    }

    @Given("^a request to get a default thing$")
    fun a_request_to_get_a_default_thing() {
        println("tptest90")
        testContext.input.sortBy = "sortBy"
    }

    @Given("^a thing with name (.*) exists$")
    fun `a thing with name (name) exists`(name: String) {
        println("tptest12")
        testContext.input.sortBy = "sortBy"
    }

    @Given("^the sortBy is ([^u].*)$")
    fun `the sortBy is (name)`(sortBy: String?) {
        testContext.input.sortBy = sortBy
    }

    @Given("^the sortBy is unspecified$")
    fun `the sortBy parameter is unspecified`() {
        testContext.input.sortBy = " "
    }

    @Given("^the default product$")
    fun `the default product`() {
        databaseService.addProduct(productId = defaultProduct.id,
            name = defaultProduct.name,
            productType = ProductType.ONLINE,
            enabled = defaultProduct.enabled)
        testContext.input.productId = defaultProductId
    }

    @Given("^an unknown product$")
    fun `an unknown product`() {
        testContext.input.productId = UUID.randomUUID()
    }

    @Given("^the unknown product (.*)$")
    fun `the unknown product (productId)`(productId: String) {
        testContext.input.productId = UUID.fromString(productId)
    }

    @Then("^the response status is (\\d+)$")
    fun `the response status is`(statusCode: Int) {
        assertThat(testContext.results.response?.statusCode).isEqualTo(statusCode)
    }

    @Then("^the following response is returned:$")
    fun theFollowingResponseIsReturned(expectedBodyTemplate: String) {
        val expectedBody = replaceNamesByUUIDs(expectedBodyTemplate)
        val actualBody = testContext.results.response?.body?.toString()
        JSONAssert.assertEquals(expectedBody, actualBody, false)
    }

}