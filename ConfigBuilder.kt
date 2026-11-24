package com.example.vlessvpn

import org.json.JSONArray
import org.json.JSONObject

/** Собирает JSON-конфиг Xray: tun-инбаунд (fd отдаёт VpnService) + выбранный сервер. */
object ConfigBuilder {

    fun build(p: Profile): String {
        val root = JSONObject()
        root.put("log", JSONObject().put("loglevel", "warning"))
        root.put(
            "dns",
            JSONObject().put("servers", JSONArray().put("1.1.1.1").put("8.8.8.8")),
        )
        root.put(
            "inbounds",
            JSONArray().put(
                JSONObject()
                    .put("tag", "tun-in")
                    .put("protocol", "tun")
                    .put("settings", JSONObject().put("name", "xray0").put("MTU", 1500))
                    .put(
                        "sniffing",
                        JSONObject().put("enabled", true)
                            .put("destOverride", JSONArray().put("http").put("tls")),
                    ),
            ),
        )
        root.put(
            "outbounds",
            JSONArray()
                .put(proxyOutbound(p))
                .put(JSONObject().put("tag", "direct").put("protocol", "freedom"))
                .put(JSONObject().put("tag", "block").put("protocol", "blackhole")),
        )
        return root.toString()
    }

    private fun proxyOutbound(p: Profile): JSONObject {
        val o = JSONObject().put("tag", "proxy").put("protocol", p.protocol)
        val settings = JSONObject()
        when (p.protocol) {
            "vless" -> {
                val user = JSONObject().put("id", p.id).put("encryption", p.params["encryption"] ?: "none")
                p.params["flow"]?.takeIf { it.isNotEmpty() }?.let { user.put("flow", it) }
                settings.put(
                    "vnext",
                    JSONArray().put(
                        JSONObject().put("address", p.address).put("port", p.port)
                            .put("users", JSONArray().put(user)),
                    ),
                )
            }
            "vmess" -> {
                val user = JSONObject().put("id", p.id)
                    .put("alterId", p.params["aid"]?.toIntOrNull() ?: 0)
                    .put("security", p.params["scy"] ?: "auto")
                settings.put(
                    "vnext",
                    JSONArray().put(
                        JSONObject().put("address", p.address).put("port", p.port)
                            .put("users", JSONArray().put(user)),
                    ),
                )
            }
            "trojan" -> settings.put(
                "servers",
                JSONArray().put(
                    JSONObject().put("address", p.address).put("port", p.port).put("password", p.id),
                ),
            )
        }
        o.put("settings", settings)
        o.put("streamSettings", streamSettings(p))
        return o
    }

    private fun streamSettings(p: Profile): JSONObject {
        val net = p.params["type"] ?: "tcp"
        val security = p.params["security"] ?: "none"
        val s = JSONObject().put("network", net).put("security", security)

        when (security) {
            "tls" -> s.put(
                "tlsSettings",
                JSONObject()
                    .put("serverName", p.params["sni"] ?: p.params["host"] ?: p.address)
                    .put("allowInsecure", p.params["allowInsecure"] == "1")
                    .apply {
                        p.params["fp"]?.takeIf { it.isNotEmpty() }?.let { put("fingerprint", it) }
                        p.params["alpn"]?.takeIf { it.isNotEmpty() }
                            ?.let { put("alpn", JSONArray(it.split(","))) }
                    },
            )
            "reality" -> s.put(
                "realitySettings",
                JSONObject()
                    .put("serverName", p.params["sni"] ?: p.address)
                    .put("fingerprint", p.params["fp"] ?: "chrome")
                    .put("publicKey", p.params["pbk"] ?: "")
                    .put("shortId", p.params["sid"] ?: "")
                    .put("spiderX", p.params["spx"] ?: ""),
            )
        }

        when (net) {
            "ws" -> {
                val ws = JSONObject().put("path", p.params["path"] ?: "/")
                p.params["host"]?.takeIf { it.isNotEmpty() }
                    ?.let { ws.put("headers", JSONObject().put("Host", it)) }
                s.put("wsSettings", ws)
            }
            "grpc" -> s.put(
                "grpcSettings",
                JSONObject().put("serviceName", p.params["serviceName"] ?: p.params["path"] ?: ""),
            )
            "httpupgrade" -> s.put(
                "httpupgradeSettings",
                JSONObject().put("path", p.params["path"] ?: "/").put("host", p.params["host"] ?: ""),
            )
        }
        return s
    }
}
