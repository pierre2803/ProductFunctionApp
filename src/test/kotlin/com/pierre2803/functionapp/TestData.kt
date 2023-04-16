package com.pierre2803.functionapp

import com.pierre2803.functionapp.db.FlywayRepository
import com.pierre2803.functionapp.db.ProductDBCreation
import com.pierre2803.functionapp.db.ProductType
import com.pierre2803.functionapp.product.Product
import com.pierre2803.functionapp.product.ProductCreationRequest
import com.pierre2803.functionapp.product.ProductUpdate
import com.pierre2803.functionapp.product.ProductUpdateRequest
import java.time.Clock
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.util.*

object TestData {

    val nowUTC: ZonedDateTime = Clock.systemUTC().instant().atZone(UTC)
    const val defaultInvalidSortBy = "InvalidSortBy"
    val defaultUserId: UUID = UUID.randomUUID()

    // ----------------------------------------------------------------------------------------------------------------
    //  ProductId
    // ----------------------------------------------------------------------------------------------------------------
    val defaultProductId: UUID = UUID.fromString("ffffffff-0941-46ea-81a5-ee392dfaa167")
    const val defaultProductName = "ProductName"
    val defaultProductType = ProductType.ONLINE
    val defaultProductCreationRequest = ProductCreationRequest(name = defaultProductName, productType = defaultProductType.value)
    val defaultProductUpdateRequest = ProductUpdateRequest(name = defaultProductName)
    val defaultProduct = Product(
        id = defaultProductId,
        name = defaultProductName,
        enabled = true,
        deletionTime = nowUTC.toInstant().toEpochMilli(),
    )
    val defaultProductDBCreation =
        ProductDBCreation(id = defaultProductId, name = defaultProductName, productType = defaultProductType)
    val defaultProductUpdate =
        ProductUpdate(id = defaultProductId, name = defaultProductName, productType = defaultProductType.name)
    val Product1 = Product(id = UUID.randomUUID(), name = "AAA", enabled = true)
    val Product2 = Product(id = UUID.randomUUID(), name = "BAA", enabled = true)
    val Product3 = Product(id = UUID.randomUUID(), name = "CAA", enabled = true)
    val Product4 = Product(id = UUID.randomUUID(), name = "ABA", enabled = true)

    // ----------------------------------------------------------------------------------------------------------------
    //  Flyway Info
    // ----------------------------------------------------------------------------------------------------------------

    val flywayInfo = FlywayRepository.FlywayInfo(
        version = 1.1,
        description = "script 1.1 Init",
        installedOn = "2023-03-28 08:00:00"
    )

}

