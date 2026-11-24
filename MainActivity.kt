package com.example.vlessvpn

import android.app.Activity
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    private val vpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) startVpn()
        }

    private fun connect() {
        val intent = VpnService.prepare(this)
        if (intent != null) vpnPermission.launch(intent) else startVpn()
    }

    private fun autoConnect() {
        if (vm.selectAuto()) connect()
    }

    private fun startVpn() {
        startService(Intent(this, XrayVpnService::class.java).setAction(XrayVpnService.ACTION_START))
    }

    private fun stopVpn() {
        startService(Intent(this, XrayVpnService::class.java).setAction(XrayVpnService.ACTION_STOP))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        setContent {
            OxyvoTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot(vm, onConnect = ::connect, onAutoConnect = ::autoConnect, onDisconnect = ::stopVpn)
                }
            }
        }
    }
}

@Composable
private fun AppRoot(vm: MainViewModel, onConnect: () -> Unit, onAutoConnect: () -> Unit, onDisconnect: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    val titles = listOf("Главная", "Серверы", "Б.С. / Ч.С.")

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        TabRow(
            selectedTabIndex = tab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            titles.forEachIndexed { i, title ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) })
            }
        }
        when (tab) {
            0 -> HomeScreen(vm, onAutoConnect, onConnect, onDisconnect)
            1 -> ServersScreen(vm)
            else -> SplitScreen(vm)
        }
    }
}
