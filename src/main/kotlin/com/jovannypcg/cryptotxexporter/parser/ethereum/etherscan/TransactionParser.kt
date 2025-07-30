package com.jovannypcg.cryptotxexporter.parser.ethereum.etherscan

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jovannypcg.cryptotxexporter.dto.ethereum.etherscan.Transaction
import com.jovannypcg.cryptotxexporter.dto.ethereum.etherscan.TransactionsResponse
import com.jovannypcg.cryptotxexporter.model.TransactionType
import com.jovannypcg.cryptotxexporter.model.Transaction as InternalTransaction

object TransactionParser {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .registerModule(JavaTimeModule())

    fun parse(
        json: String,
        transactionType: TransactionType
    ): List<InternalTransaction> {
        val rawResponse = objectMapper.readValue<TransactionsResponse>(json)

        return when {
            rawResponse.status == "1" && rawResponse.result.isArray -> {
                parseTransactions(
                    rawResponse.result.toString(),
                    transactionType
                )
            }

            else -> {
                println("Etherscan error: ${rawResponse.result.asText()}")
                emptyList()
            }
        }
    }

    fun parseTransactions(
        transactionsJson: String,
        transactionType: TransactionType
    ): List<InternalTransaction> {
        val transactionDtos: List<Transaction> = objectMapper.readValue(
            transactionsJson,
            object : TypeReference<List<Transaction>>() {}
        )

        return transactionDtos.map { it.toInternal(transactionType) }
    }
}
