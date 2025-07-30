package com.jovannypcg.cryptotxexporter.dto.ethereum.etherscan

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.jovannypcg.cryptotxexporter.model.TransactionType
import java.math.BigDecimal
import java.time.Instant
import com.jovannypcg.cryptotxexporter.model.Transaction as InternalTransaction

@JsonIgnoreProperties(ignoreUnknown = true)
data class TransactionsResponse(
    val status: String,
    val message: String,
    val result: JsonNode
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Transaction(
    val blockNumber: String,
    val hash: String,
    val nonce: String?,
    val blockHash: String?,
    val transactionIndex: String?,
    val from: String,
    val to: String,
    val value: Double = 0.0,
    val gas: String,
    val gasPrice: Double = 0.0,
    val gasUsed: Double = 0.0,
    val input: String,
    val contractAddress: String,
    val methodId: String?,
    val tokenName: String?,
    val tokenSymbol: String?,
    val tokenId: String?,

    @JsonProperty("timeStamp")
    val timestamp: Instant,

    @JsonProperty("txreceipt_status")
    val txReceiptStatus: String?,
) {
    val weiToEth = BigDecimal("1000000000000000000")

    fun toInternal(
        transactionType: TransactionType
    ): InternalTransaction = InternalTransaction(
        transactionHash = hash,
        timestamp = timestamp,
        from = from,
        to = to,
        type = transactionType,
        assetContractAddress = contractAddress,
        assetSymbolOrName = tokenSymbol,
        tokenId = tokenId,
        amount = BigDecimal.valueOf(value).divide(weiToEth),
        gasFeeEth = calculateGasFee(gasUsed, gasPrice)
    )

    fun calculateGasFee(gasUsed: Double, gasPrice: Double): BigDecimal {
        val weiUsed = BigDecimal(gasUsed)
        val weiPrice = BigDecimal(gasPrice)

        val totalGasInWei = weiUsed.multiply(weiPrice)

        return totalGasInWei.divide(weiToEth)
    }
}
