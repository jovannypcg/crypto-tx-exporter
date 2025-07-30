package com.jovannypcg.cryptotxexporter.client

import com.jovannypcg.cryptotxexporter.model.Transaction as InternalTransaction

interface TransactionClient {
    suspend fun getTransactions(address: String): List<InternalTransaction>
}
