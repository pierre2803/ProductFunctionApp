package com.pierre2803.functionapp

import com.microsoft.azure.servicebus.ClientFactory
import com.microsoft.azure.servicebus.IMessageSender

object MessageSender {

    private val messageSender = buildClient()

    private fun buildClient(): IMessageSender {
        val connectionString = System.getenv("WX_PU_EVENTS_TOPIC_CONNECTION_STRING") ?: throw IllegalArgumentException("Could not retrieve the topic Connection String.")
        return ClientFactory.createMessageSenderFromConnectionString(connectionString)
    }

    fun getSender() = messageSender
}