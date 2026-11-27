package com.example.vlessvpn

import org.json.JSONObject

/** Подгружает sources.json из вашего репозитория; при ошибке берёт последнюю сохранённую копию. */
object RemoteSources {
    fun load(storage: Storage): List<Src> {
        var json: String? = null
        for (u in Defaults.REMOTE_SOURCES) {
            try {
                json = Subscription.download(u, connectMs = 6000, readMs = 8000)
                break
            } catch (_: Exception) {
            }
        }
        if (json != null && parse(json).isNotEmpty()) storage.remoteSources = json
        return parse(json ?: storage.remoteSources)
    }

    private fun parse(text: String): List<Src> = try {
        val arr = JSONObject(text).getJSONArray("sources")
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.getJSONObject(i)
            val url = o.optString("url")
            if (!url.startsWith("http")) {
                null
            } else {
                val m = o.optJSONArray("mirrors")
                val mirrors = if (m == null) emptyList() else (0 until m.length()).map { m.getString(it) }
                Src(url, o.optInt("cat", Defaults.CAT_BL), mirrors)
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}
