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
}
