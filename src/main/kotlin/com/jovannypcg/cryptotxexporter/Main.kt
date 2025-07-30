package com.jovannypcg.cryptotxexporter

import com.jovannypcg.cryptotxexporter.client.TransactionClient
import com.jovannypcg.cryptotxexporter.client.ethereum.EtherscanClient
import okhttp3.OkHttpClient

suspend fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Usage: crypto-tx-exporter <etherscan_api_key> <wallet_address>")
        return
    }

    val apiKey = args[0]
    val walletAddress = args[1]

    val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val transactionClient: TransactionClient = EtherscanClient(apiKey, httpClient)

    val transactions = transactionClient
        .getTransactions(walletAddress)
        .toList()

    println(transactions)
}
