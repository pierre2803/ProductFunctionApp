package com.pierre2803.functionapp.blackboxtesting

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
        features = ["src/test/resources/scenarios"],
        glue = ["com.pierre2803.functionapp.blackboxtesting.scenarios"],
        plugin = [
            "pretty",
            "json:target/cucumber/json/cucumber.json",
            "junit:target/cucumber/junit/cucumber.xml"]
)
class RunCucumberST
