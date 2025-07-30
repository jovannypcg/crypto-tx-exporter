package com.jovannypcg.cryptotxexporter.export

import java.io.File
import com.jovannypcg.cryptotxexporter.model.Transaction as InternalTransaction

interface TransactionExporter {
    fun export(transactions: List<InternalTransaction>, outputFile: File)
}
