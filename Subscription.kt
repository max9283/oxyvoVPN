package com.example.vlessvpn

import java.net.HttpURLConnection
import java.net.URL

object Subscription {
    /** Скачивает подписку (обычный текст или base64 со списком ссылок) и разбирает её. */
    fun fetch(url: String): List<Profile> {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.setRequestProperty("User-Agent", "VlessVPN/1.0")
        val body = try {
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
        val text = if (body.contains("://")) body else LinkParser.decodeBase64(body)
        return LinkParser.parseMany(text)
    }
}
