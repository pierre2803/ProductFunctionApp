package com.pierre2803.functionapp.blackboxtesting.scenarios

import java.util.*

object Ids {

    private val defaultIdByName = mapOf(
            Pair("PRODUCT_ID", UUID.fromString("11f2f484-ecf2-4e62-92c8-3a49c0b25b1b")),
            Pair("UNKNOWN_PRODUCT_ID", UUID.fromString("33c5fe4e-04be-436a-88df-fc12ae043ace")),

    )
    private val idByName = mutableMapOf<String, UUID>()

    fun reset() {
        idByName.clear()
        idByName.putAll(defaultIdByName)
    }

    fun add(name: String, id: UUID) {
        idByName[name] = id
    }

    fun toUUID(name: String): UUID = idByName[name] ?: UUID.fromString("invalid-uuid")

    fun contains(name: String): Boolean = idByName[name] != null

    fun toUUID(name: String, defaultUUID: UUID): UUID = idByName[name] ?: defaultUUID

    fun getUUIDFor(name: String): UUID {
        idByName.putIfAbsent(name, UUID.randomUUID())
        return idByName[name]!!
    }
}