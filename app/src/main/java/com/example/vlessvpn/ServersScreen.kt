package com.example.vlessvpn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ServersScreen(vm: MainViewModel) {
    var input by remember { mutableStateOf("") }
    val visible = vm.profiles
        .mapIndexed { i, p -> i to p }
        .filter { vm.catFilter == 0 || it.second.cat == vm.catFilter }

    LazyColumn(
        Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { CategoryChips(vm) }
        item {
            Text(categoryHint(vm.catFilter), style = MaterialTheme.typography.bodySmall, color = Oxyvo.Muted)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.autoSetup() }, enabled = !vm.busy) { Text("Обновить список") }
                OutlinedButton(onClick = { vm.retest() }, enabled = !vm.busy && vm.profiles.isNotEmpty()) {
                    Text("Проверить пинг")
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { vm.scan() },
                    enabled = !vm.busy && vm.profiles.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Сканер обхода блокировок") }
                Text(
                    "Сканер делает реальный запрос через сервер (интернет, YouTube, Telegram) из вашей " +
                        "текущей сети. Для белых списков запускайте его там, где они действуют.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Oxyvo.Muted,
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (vm.busy) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text(vm.progress ?: "Подождите…", style = MaterialTheme.typography.bodySmall, color = Oxyvo.Muted)
                }
                vm.message?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = Oxyvo.Muted)
                }
            }
        }
        item {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Своя подписка (https://…) или vless:// vmess:// trojan://") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedButton(onClick = { vm.add(input); input = "" }, enabled = input.isNotBlank() && !vm.busy) {
                Text("Добавить")
            }
        }
        items(visible) { (i, p) ->
            val sel = i == vm.selected
            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (sel) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .clickable { vm.select(i) }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = sel, onClick = { vm.select(i) })
                Column(Modifier.weight(1f)) {
                    Text(p.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    Text(
                        (if (p.cat == Defaults.CAT_WL) "Б.С. · " else "Ч.С. · ") +
                            "${p.protocol.uppercase()} · ${p.address}:${p.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Oxyvo.Muted,
                        maxLines = 1,
                    )
                    if (p.scanned) {
                        Text(scanText(p), style = MaterialTheme.typography.labelSmall, color = scanColor(p))
                    }
                }
                Text(
                    if (p.ping >= 0) "${p.ping} мс" else "—",
                    color = pingColor(p.ping),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
                TextButton(onClick = { vm.delete(i) }) { Text("✕", color = Oxyvo.Muted) }
            }
        }
    }
}
