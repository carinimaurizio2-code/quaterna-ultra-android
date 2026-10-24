
package com.mauriziocarini.quaterna

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object NetworkClient {
    private const val URL_GITHUB = "https://raw.githubusercontent.com/carinimaurizio2-code/archivio-lotto/main/storico.txt"

    suspend fun scaricaStorico(): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL(URL_GITHUB)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
