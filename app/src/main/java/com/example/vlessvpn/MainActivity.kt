package com.example.vlessvpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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

    private fun stopVpn() {
        startService(Intent(this, XrayVpnService::class.java).setAction(XrayVpnService.ACTION_STOP))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                Surface(Modifier.fillMaxSize()) {
                    Screen(vm, onConnect = ::connect, onDisconnect = ::stopVpn)
                }
            }
        }
    }
}

@Composable
private fun Screen(vm: MainViewModel, onConnect: () -> Unit, onDisconnect: () -> Unit) {
    val status by VpnState.status.collectAsState()
    val error by VpnState.error.collectAsState()
    var input by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("VlessVPN", style = MaterialTheme.typography.headlineMedium)

        val statusText = when (status) {
            VpnStatus.DISCONNECTED -> "Отключено"
            VpnStatus.CONNECTING -> "Подключение…"
            VpnStatus.CONNECTED -> "Подключено"
            VpnStatus.ERROR -> "Ошибка: ${error ?: "неизвестная"}"
        }
        Text(statusText, style = MaterialTheme.typography.titleMedium)

        val active = status == VpnStatus.CONNECTED || status == VpnStatus.CONNECTING
        Button(
            onClick = { if (active) onDisconnect() else onConnect() },
            enabled = active || vm.profiles.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (active) "Отключиться" else "Подключиться") }

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Ссылка подписки или vless:// vmess:// trojan://") },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.add(input); input = "" }) { Text("Добавить") }
            OutlinedButton(onClick = { vm.refresh() }, enabled = vm.subUrl.isNotBlank() && !vm.loading) {
                Text(if (vm.loading) "Загрузка…" else "Обновить подписку")
            }
        }
        vm.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

        LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(vm.profiles) { i, p ->
                Row(
                    Modifier.fillMaxWidth().clickable { vm.select(i) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = i == vm.selected, onClick = { vm.select(i) })
                    Column(Modifier.weight(1f)) {
                        Text(p.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${p.protocol} · ${p.address}:${p.port}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = { vm.delete(i) }) { Text("Удалить") }
                }
            }
        }
    }
}
