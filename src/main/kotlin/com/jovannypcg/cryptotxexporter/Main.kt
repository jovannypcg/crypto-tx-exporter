package com.jovannypcg.cryptotxexporter

import com.jovannypcg.cryptotxexporter.client.TransactionClient
import com.jovannypcg.cryptotxexporter.client.ethereum.EtherscanClient
import com.jovannypcg.cryptotxexporter.export.CsvTransactionExporter
import okhttp3.OkHttpClient
import java.io.File

val httpClient = OkHttpClient.Builder()
    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
    .build()

suspend fun main(args: Array<String>) {
    if (args.size < 3) {
        println("Usage: crypto-tx-exporter <etherscan_api_key> <wallet_address> <output_csv_path>")
        return
    }

    val apiKey = args[0]
    val walletAddress = args[1]
    val outputPath = args[2]

    val transactionClient: TransactionClient = EtherscanClient(apiKey, httpClient)

    val transactions = transactionClient
        .getTransactions(walletAddress)
        .toList()

    val outputFile = File(outputPath)
    CsvTransactionExporter.export(transactions, outputFile)

    println("Exported ${transactions.size} transactions to ${outputFile.absolutePath}")
}
