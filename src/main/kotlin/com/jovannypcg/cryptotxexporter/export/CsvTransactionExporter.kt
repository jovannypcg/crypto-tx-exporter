package com.jovannypcg.cryptotxexporter.export

import com.jovannypcg.cryptotxexporter.model.Transaction
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.time.format.DateTimeFormatter

object CsvTransactionExporter : TransactionExporter {
    private val dateFormatter = DateTimeFormatter.ISO_INSTANT

    override fun export(transactions: List<Transaction>, outputFile: File) {
        FileWriter(outputFile).use { writer ->
            CSVWriter(writer).use { csv ->
                csv.writeNext(
                    arrayOf(
                        "Transaction Hash",
                        "Timestamp",
                        "From",
                        "To",
                        "Transaction Type",
                        "Asset Contract Address",
                        "Asset Symbol / Name",
                        "Token ID",
                        "Amount",
                        "Gas Fee (ETH)"
                    )
                )

                transactions.forEach { tx ->
                    csv.writeNext(
                        arrayOf(
                            tx.transactionHash,
                            dateFormatter.format(tx.timestamp),
                            tx.from,
                            tx.to,
                            tx.type.name,
                            tx.assetContractAddress ?: "",
                            tx.assetSymbolOrName ?: "",
                            tx.tokenId ?: "",
                            tx.amount.toPlainString(),
                            tx.gasFeeEth?.toPlainString() ?: ""
                        )
                    )
                }
            }
        }
    }
}
