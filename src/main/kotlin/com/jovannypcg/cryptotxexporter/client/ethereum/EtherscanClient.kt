package com.jovannypcg.cryptotxexporter.client.ethereum

import com.jovannypcg.cryptotxexporter.client.TransactionClient
import com.jovannypcg.cryptotxexporter.model.TransactionType
import com.jovannypcg.cryptotxexporter.parser.ethereum.etherscan.TransactionParser
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import com.jovannypcg.cryptotxexporter.model.Transaction as InternalTransaction

private val transactionTypeToActionMap = mapOf(
    TransactionType.ETH_TRANSFER to "txlist",
    TransactionType.INTERNAL_TRANSFER to "txlistinternal",
    TransactionType.ERC20_TRANSFER to "tokentx",
    TransactionType.ERC721_TRANSFER to "tokennfttx",
    TransactionType.ERC1155_TRANSFER to "token1155tx",
)

class EtherscanClient(
    private val apiKey: String,
    private val client: OkHttpClient
) : TransactionClient {
    override suspend fun getTransactions(
        address: String
    ): List<InternalTransaction> = withContext(Dispatchers.IO) {
        coroutineScope {
            val normalDeferred = async { getTransactions(address, TransactionType.ETH_TRANSFER) }
            val internalDeferred = async { getTransactions(address, TransactionType.INTERNAL_TRANSFER) }
            val erc20Deferred = async { getTransactions(address, TransactionType.ERC20_TRANSFER) }
            val erc721Deferred = async { getTransactions(address, TransactionType.ERC721_TRANSFER) }
            val erc1155Deferred = async { getTransactions(address, TransactionType.ERC1155_TRANSFER) }

            normalDeferred.await() +
                    internalDeferred.await() +
                    erc20Deferred.await() +
                    erc721Deferred.await() +
                    erc1155Deferred.await()
        }
    }

    fun getTransactions(
        address: String,
        transactionType: TransactionType
    ): List<InternalTransaction> {
        val actionType = transactionTypeToActionMap.getValue(transactionType)
        val url = buildUrl(address, actionType)
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("Failed to fetch transactions from Etherscan: ${response.code}")
        }

        val body = response.body?.string()
            ?: throw RuntimeException("Empty response body from Etherscan")

        val txs = TransactionParser.parse(body, transactionType)

        return txs
    }

    private fun buildUrl(address: String, actionType: String): String {
        val encoded = URLEncoder.encode(address, "UTF-8")
        return "https://api.etherscan.io/api" +
                "?module=account" +
                "&action=$actionType" +
                "&address=$encoded" +
                "&startblock=0" +
                "&endblock=99999999" +
                "&sort=asc" +
                "&apikey=$apiKey"
    }
}
