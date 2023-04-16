# Introduction 
This service, implemented as an Azure Function, is triggered by HTTP call.
Its main responsibility is to manage the entire configuration of the system.

It also contains a TimerTrigger function used to generate the work shifts starts.
    
# APIs currently offered

## Product

`POST /Products`

`GET /Products/{productId}`

`GET /Products`

`PUT /Products/{productId}`

`DELETE /Products/{productId}`

## Status

`GET /status`

    
## List of necessary Application Settings (aka Environment Variables):

| Application Settings                          | Description                                                                    | Where to find it in Azure Portal?                                                                             |
|-----------------------------------------------|--------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| WX_DB_BASIC_URL                               | Connection String to the PostgreSQL DB                                         | Application Settings                                                                                          |
| WX_DB_USER                                    | PostgreSQL DB User                                                             | Application Settings                                                                                          |
| WX_DB_PASSWORD                                | PostgreSQL DB Password                                                         | Application Settings                                                                                          |
| WX_DB_SSL_MODE                                | PostgreSQL DB SSL Mode                                                         | Application Settings                                                                                          |
| WX_DB_CONNECTION_POOL_MAX_SIZE                | PostgreSQL property                                                            |                                                                                                               |
| WX_DB_PREPARED_STATEMENT_CACHE_SIZE           | PostgreSQL property                                                            |                                                                                                               |
| WX_DB_PREPARED_STATEMENT_CACHE_SQL_MAX_LENGTH | PostgreSQL property                                                            |                                                                                                               |
| WX_PU_EVENTS_TOPIC_CONNECTION_STRING          | Connection String to the Service Bus                                           | Service Bus -> Shared access policies -> RootManageSharedAccessKey -> Primary Connection String               |
| WX_WORK_SHIFT_AUTO_START_SCHEDULE             | Schedule interval to trigger the auto-start of work shifts (ex: 0 */1 * * * *) | Application Settings                                                                                          |
| WX_USER_MANAGEMENT_SERVICE_HOST               | Host for the User Management Service                                           | go to the Resource Group, click on the service starting with TPUserManagementService, Copy the settings `URL` |
| WX_IOTHUB_CONNECTION_STRING                   | Connection String to the IoTHub                                                | IoTHub -> Shared access policies -> choose `iothubowner`                                                      |
| WX_TIMELINE_EVENTS_TOPIC_CONNECTION_STRING    | Connection String to the Service Bus                                           | Service Bus -> Topics -> <topic name> -> Shared access policies -> <key name> -> Primary Connection String    |

# Getting Started
This project requires Java 8, Maven 3.1+ and IntelliJ IDEA.

Open IntelliJ IDEA and create a New Project from Existing Sources, find the location of the project directory, choose Maven and click Finish

# Build and Test
To build the function, simply run `mvn clean verify`. This will build and execute all the unit tests + blackbox testing (using the Cucumber framework).

# Deploy the function to Azure
To deploy the function to the Azure resource group defined in the pom.xml (see the XML element `<functionResourceGroup>`),
run the command `mvn azure-functions:deploy`.

To combine build and deployment, run `mvn clean verify azure-functions:deploy`. 
