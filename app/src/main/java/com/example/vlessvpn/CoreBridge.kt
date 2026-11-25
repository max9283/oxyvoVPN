package com.example.vlessvpn

import android.content.Context
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray

/**
 * Единственное место, где приложение общается с ядром Xray (libv2ray.aar из
 * 2dust/AndroidLibXrayLite, кладётся в app/libs). Если в вашей версии AAR
 * названия методов отличаются — правится только этот файл.
 */
class CoreBridge(private val ctx: Context) {
    private var controller: CoreController? = null

    fun start(config: String, tunFd: Int) {
        Libv2ray.initCoreEnv(ctx.filesDir.absolutePath, "")
        val handler = object : CoreCallbackHandler {
            override fun startup(): Long = 0
            override fun shutdown(): Long = 0
            override fun onEmitStatus(code: Long, msg: String?): Long = 0
        }
        val c = Libv2ray.newCoreController(handler)
        c.startLoop(config, tunFd)
        controller = c
    }

    fun stop() {
        try {
            controller?.stopLoop()
        } finally {
            controller = null
        }
    }

    companion object {
        @Volatile private var inited = false

        private fun method() = Class.forName("libv2ray.Libv2ray")
            .getMethod("measureOutboundDelay", String::class.java, String::class.java)

        /** Есть ли в подключённом ядре замер реальной задержки. */
        fun measureAvailable(): Boolean = try { method(); true } catch (e: Throwable) { false }

        /** Реальная задержка запроса к url через сервер из config (мс), -1 при ошибке. */
        fun measure(ctx: Context, config: String, url: String): Long = try {
            if (!inited) {
                Libv2ray.initCoreEnv(ctx.filesDir.absolutePath, "")
                inited = true
            }
            (method().invoke(null, config, url) as Number).toLong()
        } catch (e: Throwable) {
            -1L
        }
    }
}
