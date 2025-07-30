package com.jovannypcg.cryptotxexporter

import com.jovannypcg.cryptotxexporter.client.TransactionClient
import com.jovannypcg.cryptotxexporter.export.CsvTransactionExporter
import com.jovannypcg.cryptotxexporter.export.TransactionExporter
import com.jovannypcg.cryptotxexporter.model.Transaction
import com.jovannypcg.cryptotxexporter.model.TransactionType
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.math.BigDecimal
import java.time.Instant

class MainTest {
    private val dummyTransaction = Transaction(
        transactionHash = "0xabc",
        timestamp = Instant.parse("2025-07-28T00:00:00Z"),
        from = "0xfrom",
        to = "0xto",
        type = TransactionType.ETH_TRANSFER,
        assetContractAddress = "0xcontract",
        assetSymbolOrName = "ETH",
        tokenId = "123",
        amount = BigDecimal("1.0"),
        gasFeeEth = BigDecimal("0.00042")
    )

    class FakeTransactionClient(private val result: List<Transaction>) : TransactionClient {
        override suspend fun getTransactions(address: String): List<Transaction> = result
    }

    @Test
    fun `should write CSV file with one transaction`() {
        val tempFile = File.createTempFile("tx-", ".csv")
        val client = FakeTransactionClient(listOf(dummyTransaction))

        runBlocking {
            runExportTest(client, tempFile)
        }

        val lines = tempFile.readLines()
        assertThat(lines).hasSize(2)
        assertThat(lines[1]).contains("0xabc", "0xfrom", "0xto", "ETH", "1.0", "0.00042")
        tempFile.delete()
    }

    @Test
    fun `should write empty CSV if no transactions`() {
        val tempFile = File.createTempFile("tx-", ".csv")
        val client = FakeTransactionClient(emptyList())

        runBlocking {
            runExportTest(client, tempFile)
        }

        val lines = tempFile.readLines()
        assertThat(lines).hasSize(1) // Only header row
        assertThat(lines[0]).contains("Transaction Hash", "Timestamp", "Amount") // Optional: verify header
        tempFile.delete()
    }

    // Utility function to inject mocks into the exported logic
    private suspend fun runExportTest(client: TransactionClient, outputFile: File) {
        object : TransactionExporter by CsvTransactionExporter {
            fun run() = runBlocking {
                CsvTransactionExporter.export(client.getTransactions("0xwallet"), outputFile)
            }
        }.run()
    }
}
