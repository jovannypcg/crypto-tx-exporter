package com.jovannypcg.cryptotxexporter.export

import com.jovannypcg.cryptotxexporter.model.Transaction
import com.jovannypcg.cryptotxexporter.model.TransactionType
import com.opencsv.CSVReader
import org.junit.jupiter.api.Test
import java.io.FileReader
import java.math.BigDecimal
import java.nio.file.Files
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat

class CsvTransactionExporterTest {

    @Test
    fun `should export transactions to CSV file with expected content`() {
        val tx = Transaction(
            transactionHash = "0xabc123",
            timestamp = Instant.parse("2025-07-28T15:30:00Z"),
            from = "0xfrom",
            to = "0xto",
            type = TransactionType.ETH_TRANSFER,
            assetContractAddress = "0xcontract",
            assetSymbolOrName = "ETH",
            tokenId = "1",
            amount = BigDecimal("0.5"),
            gasFeeEth = BigDecimal("0.00021")
        )

        val outputFile = Files.createTempFile("transactions", ".csv").toFile()
        CsvTransactionExporter.export(listOf(tx), outputFile)

        val reader = CSVReader(FileReader(outputFile))
        val rows = reader.readAll()

        assertThat(rows).hasSize(2)

        val header = rows[0]
        assertThat(header).containsExactly(
            "Transaction Hash", "Timestamp", "From", "To", "Transaction Type",
            "Asset Contract Address", "Asset Symbol / Name", "Token ID", "Amount", "Gas Fee (ETH)"
        )

        val row = rows[1]
        assertThat(row).containsExactly(
            "0xabc123",
            "2025-07-28T15:30:00Z",
            "0xfrom",
            "0xto",
            "ETH_TRANSFER",
            "0xcontract",
            "ETH",
            "1",
            "0.5",
            "0.00021"
        )

        outputFile.delete()
    }
}
