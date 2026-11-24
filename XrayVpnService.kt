package com.example.vlessvpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.flow.MutableStateFlow

enum class VpnStatus { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

object VpnState {
    val status = MutableStateFlow(VpnStatus.DISCONNECTED)
    val error = MutableStateFlow<String?>(null)
}

class XrayVpnService : VpnService() {
    private var pfd: ParcelFileDescriptor? = null
    private var core: CoreBridge? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            cleanup()
            VpnState.status.value = VpnStatus.DISCONNECTED
            stopSelf()
            return START_NOT_STICKY
        }
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (core != null) return
        val storage = Storage(this)
        val profile = storage.selectedProfile()
        if (profile == null) {
            fail("Нет выбранного сервера")
            return
        }
        val mode = storage.splitMode
        val apps = storage.splitApps
        if (mode == 1 && apps.isEmpty()) {
            fail("Белый список пуст: выберите приложения на вкладке Б.С. / Ч.С.")
            return
        }
        VpnState.error.value = null
        VpnState.status.value = VpnStatus.CONNECTING
        Thread {
            try {
                val builder = Builder()
                    .setSession("Oxyvo VPN")
                    .setMtu(1500)
                    .addAddress("172.19.0.1", 30)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer("1.1.1.1")
                    .addDnsServer("8.8.8.8")

                var allowed = 0
                if (mode == 1) {
                    // Белый список: через VPN идут только выбранные приложения
                    apps.forEach {
                        try { builder.addAllowedApplication(it); allowed++ } catch (_: Exception) {}
                    }
                }
                if (mode == 1 && allowed > 0) {
                    // само приложение не добавлено в allowed -> идёт мимо туннеля
                } else {
                    if (mode == 2) {
                        // Чёрный список: выбранные приложения идут в обход VPN
                        apps.forEach {
                            try { builder.addDisallowedApplication(it) } catch (_: Exception) {}
                        }
                    }
                    // Ядро внутри приложения должно идти мимо туннеля — защита от петли.
                    builder.addDisallowedApplication(packageName)
                }

                val fd = builder.establish() ?: error("VPN-разрешение не выдано")
                pfd = fd
                val bridge = CoreBridge(this)
                bridge.start(ConfigBuilder.build(profile), fd.fd)
                core = bridge
                VpnState.status.value = VpnStatus.CONNECTED
            } catch (e: Throwable) {
                cleanup()
                fail(e.message ?: e.toString())
            }
        }.start()
    }

    private fun fail(msg: String) {
        VpnState.error.value = msg
        VpnState.status.value = VpnStatus.ERROR
        stopSelf()
    }

    @Synchronized
    private fun cleanup() {
        try { core?.stop() } catch (_: Throwable) {}
        core = null
        try { pfd?.close() } catch (_: Throwable) {}
        pfd = null
    }

    override fun onRevoke() {
        cleanup()
        VpnState.status.value = VpnStatus.DISCONNECTED
        super.onRevoke()
    }

    override fun onDestroy() {
        cleanup()
        if (VpnState.status.value != VpnStatus.ERROR) VpnState.status.value = VpnStatus.DISCONNECTED
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.example.vlessvpn.START"
        const val ACTION_STOP = "com.example.vlessvpn.STOP"
    }
}
