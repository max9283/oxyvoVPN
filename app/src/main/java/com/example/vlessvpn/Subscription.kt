package com.example.vlessvpn

import android.text.Html
import java.net.HttpURLConnection
import java.net.URL

object Subscription {
    private const val UA_APP = "OxyvoVPN/1.0"
    private const val UA_BROWSER =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36"
    private val LINK = Regex("(?:vless|vmess|trojan)://[^\\s<>\"']+")

    private const val MAX_BYTES = 6_000_000
    private const val MAX_LINES = 4000

    fun download(url: String, ua: String = UA_APP, connectMs: Int = 7000, readMs: Int = 12000): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = connectMs
        conn.readTimeout = readMs
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("User-Agent", ua)
        try {
            val out = java.io.ByteArrayOutputStream()
            val buf = ByteArray(16384)
            var total = 0
            conn.inputStream.use { ins ->
                while (true) {
                    val n = ins.read(buf)
                    if (n < 0) break
                    out.write(buf, 0, n)
                    total += n
                    if (total > MAX_BYTES) break
                }
            }
            return out.toString("UTF-8")
        } finally {
            conn.disconnect()
        }
    }

    private fun parseText(body: String): List<Profile> {
        val text = if (body.contains("://")) body else LinkParser.decodeBase64(body)
        var lines = text.lines()
        if (lines.size > MAX_LINES) lines = lines.shuffled().take(MAX_LINES)
        return lines.mapNotNull { LinkParser.parse(it) }
    }

    /** Скачивает подписку (обычный текст или base64 со списком ссылок) и разбирает её. */
    fun fetch(url: String): List<Profile> = parseText(download(url, UA_APP))

    /** Страница-обёртка переводчика: вытаскиваем ссылки vless/vmess/trojan из текста страницы. */
    private fun fetchTranslated(url: String): List<Profile> {
        val body = download(url, UA_BROWSER, 10000, 20000)
        val plain = Html.fromHtml(body, Html.FROM_HTML_MODE_LEGACY).toString()
        return LINK.findAll(plain).mapNotNull { LinkParser.parse(it.value) }.toList()
    }

    /** Адреса, по которым файл можно получить через сервисы Яндекса (они обычно доступны при белых списках). */
    fun translatorUrls(url: String): List<String> {
        val noScheme = url.removePrefix("https://")
        return listOf(
            "https://translate.yandex.ru/translate?url=$url&lang=de-de",
            "https://translated.turbopages.org/proxy_u/de-de.ru/https/$noScheme",
        )
    }

    /**
     * 1) напрямую и по зеркалам; 2) если не вышло — через Яндекс Переводчик (только для списков Б.С.).
     * forceYandex = сразу через переводчик.
     */
    fun fetchWithFallback(src: Src, forceYandex: Boolean = false): List<Profile> {
        if (!forceYandex) {
            for (u in listOf(src.url) + src.mirrors) {
                try {
                    val l = fetch(u)
                    if (l.isNotEmpty()) return l
                } catch (_: Exception) {
                }
            }
        }
        if (forceYandex || src.cat == Defaults.CAT_WL) {
            for (u in translatorUrls(src.url)) {
                try {
                    val l = fetchTranslated(u)
                    if (l.isNotEmpty()) return l
                } catch (_: Exception) {
                }
            }
        }
        return emptyList()
    }
}
