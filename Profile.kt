package com.example.vlessvpn

import android.net.Uri
import android.util.Base64
import org.json.JSONObject

/** Один сервер. protocol: vless | vmess | trojan. id = UUID (vless/vmess) или пароль (trojan). */
data class Profile(
    val name: String,
    val protocol: String,
    val address: String,
    val port: Int,
    val id: String,
    val params: Map<String, String>,
    val ping: Int = -1,          // мс, -1 = неизвестно / не отвечает
    val auto: Boolean = false,   // true = добавлен автоматически из списков
    val listGroup: Int = 1,       // 1 = белый список, 2 = чёрный список
) {
    fun key(): String = "$protocol|$address|$port|$id"

    fun toJson(): JSONObject = JSONObject()
        .put("name", name).put("protocol", protocol)
        .put("address", address).put("port", port).put("id", id)
        .put("params", JSONObject(params))
        .put("ping", ping).put("auto", auto).put("listGroup", listGroup)

    companion object {
        fun fromJson(o: JSONObject): Profile {
            val p = o.optJSONObject("params") ?: JSONObject()
            val map = mutableMapOf<String, String>()
            p.keys().forEach { k -> map[k] = p.getString(k) }
            return Profile(
                o.getString("name"), o.getString("protocol"),
                o.getString("address"), o.getInt("port"), o.getString("id"), map,
                o.optInt("ping", -1), o.optBoolean("auto", false), o.optInt("listGroup", 1),
            )
        }
    }
}

object LinkParser {
    fun parse(raw: String): Profile? = try {
        val link = raw.trim()
        when {
            link.startsWith("vless://") -> parseUri(link, "vless")
            link.startsWith("trojan://") -> parseUri(link, "trojan")
            link.startsWith("vmess://") -> parseVmess(link)
            else -> null
        }
    } catch (e: Exception) {
        null
    }

    fun parseMany(text: String): List<Profile> =
        text.lines().mapNotNull { parse(it) }

    private fun parseUri(link: String, proto: String): Profile {
        val uri = Uri.parse(link)
        val host = uri.host ?: error("no host")
        val port = if (uri.port > 0) uri.port else 443
        val id = Uri.decode(uri.userInfo ?: error("no user"))
        val params = mutableMapOf<String, String>()
        uri.queryParameterNames.forEach { k -> uri.getQueryParameter(k)?.let { params[k] = it } }
        if (proto == "trojan" && !params.containsKey("security")) params["security"] = "tls"
        val name = uri.fragment?.takeIf { it.isNotBlank() } ?: host
        return Profile(name, proto, host, port, id, params)
    }

    private fun parseVmess(link: String): Profile {
        val json = JSONObject(decodeBase64(link.removePrefix("vmess://")))
        val params = mutableMapOf<String, String>()
        params["type"] = json.optString("net", "tcp")
        params["security"] = if (json.optString("tls") == "tls") "tls" else "none"
        listOf("sni", "host", "path", "fp", "alpn", "aid", "scy").forEach {
            val v = json.optString(it)
            if (v.isNotEmpty()) params[it] = v
        }
        val add = json.getString("add")
        return Profile(
            json.optString("ps").ifBlank { add }, "vmess", add,
            json.get("port").toString().toInt(), json.getString("id"), params,
        )
    }

    fun decodeBase64(s: String): String {
        var t = s.trim().replace('-', '+').replace('_', '/')
        while (t.length % 4 != 0) t += "="
        return String(Base64.decode(t, Base64.DEFAULT), Charsets.UTF_8)
    }
}
