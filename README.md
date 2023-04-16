# Introduction 
This service, implemented as an Azure Function, is triggered by HTTP call.
 
# APIs currently offered

## Product

`POST /Products`

`GET /Products/{productId}`

`GET /Products`

`PUT /Products/{productId}`

`DELETE /Products/{productId}`

## Status

`GET /status`

# Getting Started
This project requires Java 8, Maven 3.1+ and IntelliJ IDEA.

Open IntelliJ IDEA and create a New Project from Existing Sources, find the location of the project directory, choose Maven and click Finish

# Build and Test
To build the function, simply run `mvn clean verify`. This will build and execute all the tests. 
Unit tests and Cucumber testing

# Deploy the function to Azure
To deploy the function to the Azure resource group defined in the pom.xml,
run the command `mvn azure-functions:deploy`.

To combine build and deployment, run `mvn clean verify azure-functions:deploy`. 
