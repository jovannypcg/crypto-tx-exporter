package com.jovannypcg.cryptotxexporter.model

import java.math.BigDecimal
import java.time.Instant

enum class TransactionType {
    ETH_TRANSFER,
    ERC20_TRANSFER,
    ERC721_TRANSFER,
    ERC1155_TRANSFER,
    INTERNAL_TRANSFER
}

data class Transaction(
    val transactionHash: String,
    val timestamp: Instant,
    val from: String,
    val to: String,
    val type: TransactionType,
    val assetContractAddress: String?,
    val assetSymbolOrName: String?,
    val tokenId: String?,
    val amount: BigDecimal,
    val gasFeeEth: BigDecimal?
)
