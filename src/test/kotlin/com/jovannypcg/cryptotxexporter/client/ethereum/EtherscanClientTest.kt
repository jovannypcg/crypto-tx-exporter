package com.jovannypcg.cryptotxexporter.client.ethereum

import com.jovannypcg.cryptotxexporter.model.TransactionType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EtherscanClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: EtherscanClient

    private val dummyApiKey = "test-api-key"
    private val testAddress = "0x1234567890abcdef1234567890abcdef12345678"
    val weiToEth = BigDecimal("1000000000000000000")


    @BeforeAll
    fun setup() {
        server = MockWebServer()
        server.start()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        // Patch buildUrl to use mock server base URL
        client = object : EtherscanClient(apiKey = dummyApiKey, client = okHttpClient) {
            override fun buildUrl(address: String, actionType: String): String = server
                .url("/api?module=account&action=$actionType&address=$address&startblock=0&endblock=99999999&sort=asc&apikey=$dummyApiKey")
                .toString()
        }
    }

    @AfterAll
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should parse ETH_TRANSFER transactions from valid Etherscan response`() {
        val responseBody = """
        {
          "status": "1",
          "message": "OK",
          "result": [{
            "blockNumber": "12345678",
            "hash": "0xabc",
            "nonce": "0",
            "blockHash": "0xblockhash",
            "transactionIndex": "1",
            "from": "0xfrom",
            "to": "0xto",
            "value": "1000000000000000000",
            "gas": "21000",
            "gasPrice": "1000000000",
            "gasUsed": "21000",
            "input": "0x",
            "contractAddress": "",
            "methodId": "0x12345678",
            "tokenName": "Ethereum",
            "tokenSymbol": "ETH",
            "tokenId": "42",
            "timeStamp": "1700000000",
            "txreceipt_status": "1"
          }]
        }
        """.trimIndent()
        server.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        val result = client.getTransactions(testAddress, TransactionType.ETH_TRANSFER)

        assertThat(result).hasSize(1)
        assertThat(result[0].transactionHash).isEqualTo("0xabc")
        assertThat(result[0].amount.toPlainString()).isEqualTo("1")
        assertThat(result[0].from).isEqualTo("0xfrom")
        assertThat(result[0].to).isEqualTo("0xto")
        assertThat(result[0].type).isEqualTo(TransactionType.ETH_TRANSFER)
        assertThat(result[0].assetSymbolOrName).isEqualTo("ETH")
        assertThat(result[0].tokenId).isEqualTo("42")
        assertThat(result[0].gasFeeEth?.toPlainString()).isEqualTo("0.000021")
    }

    @Test
    fun `should throw exception on unsuccessful HTTP response`() {
        server.enqueue(MockResponse().setResponseCode(500))

        val exception = Assertions.assertThrows(RuntimeException::class.java) {
            client.getTransactions(testAddress, TransactionType.ETH_TRANSFER)
        }

        assertThat(exception).hasMessageContaining("Failed to fetch transactions")
    }

    @Test
    fun `should return empty list when result is not a valid array`() {
        val badResponse = """
        {
          "status": "0",
          "message": "NOTOK",
          "result": "Invalid API Key"
        }
        """.trimIndent()

        server.enqueue(MockResponse().setBody(badResponse).setResponseCode(200))

        val result = client.getTransactions(testAddress, TransactionType.ETH_TRANSFER)

        assertThat(result).isEmpty()
    }
}
