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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

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

    private fun startVpn() {
        startService(Intent(this, XrayVpnService::class.java).setAction(XrayVpnService.ACTION_START))
    }

    private fun autoConnect() {
        vm.autoConnect { connect() }
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
                    AppRoot(vm, onAuto = ::autoConnect, onConnect = ::connect, onDisconnect = ::stopVpn, onExit = { finish() })
                }
            }
        }
    }
}

@Composable
private fun AppRoot(
    vm: MainViewModel,
    onAuto: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onExit: () -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(0) }
    val titles = listOf("Главная", "Серверы", "Приложения")

    if (!vm.accepted) {
        AlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            title = { Text("Пользовательское соглашение") },
            text = { Column(Modifier.verticalScroll(rememberScrollState())) { Text(TERMS) } },
            confirmButton = { Button(onClick = { vm.acceptTerms() }) { Text("Принимаю") } },
            dismissButton = { TextButton(onClick = onExit) { Text("Не принимаю") } },
        )
    }

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
            0 -> HomeScreen(vm, onAuto, onConnect, onDisconnect)
            1 -> ServersScreen(vm)
            else -> SplitScreen(vm)
        }
    }
}

private const val TERMS = """Oxyvo VPN не владеет серверами и не контролирует их. Серверы берутся из публичных списков в интернете и принадлежат третьим лицам.

Владелец сервера технически может видеть, на какие сайты вы заходите, и подменять трафик. Поэтому:

• не вводите пароли, данные банковских карт и другие важные данные, особенно на сайтах без HTTPS;
• не используйте такие серверы для банков, почты и других важных аккаунтов;
• серверы могут работать медленно или пропадать в любой момент.

Приложение предоставляется «как есть», без каких-либо гарантий. Вы используете его на свой риск и самостоятельно отвечаете за соблюдение законов своей страны.

Нажимая «Принимаю», вы подтверждаете, что прочитали это и согласны."""
