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

    LazyColumn(
        Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("Настройки автоподключения", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Какую группу использовать в первую очередь?", style = MaterialTheme.typography.bodySmall, color = Oxyvo.Muted)
            Text("Группы серверов назначаются вручную кнопкой «В Ч.С.» / «В Б.С.»: без заданной сети и критерия приложение не может достоверно определить, какие серверы проходят внешний белый или чёрный список.", style = MaterialTheme.typography.bodySmall, color = Oxyvo.Muted)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = vm.preferredList == 1, onClick = { vm.setPreferredList(1) })
                Text("Белый список")
                RadioButton(selected = vm.preferredList == 2, onClick = { vm.setPreferredList(2) })
                Text("Чёрный список")
            }
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
            if (vm.busy) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text(vm.progress ?: "Подождите…", style = MaterialTheme.typography.bodySmall, color = Oxyvo.Muted)
                }
            }
            vm.message?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Oxyvo.Muted) }
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
        item { Text("БЕЛЫЙ СПИСОК", style = MaterialTheme.typography.titleSmall, color = Oxyvo.Green, fontWeight = FontWeight.Bold) }
        items(vm.profiles.withIndex().filter { it.value.listGroup == 1 }.toList(), key = { it.value.key() }) { entry ->
            ServerRow(vm, entry.index, entry.value)
        }
        item { Text("ЧЁРНЫЙ СПИСОК", style = MaterialTheme.typography.titleSmall, color = Oxyvo.Warn, fontWeight = FontWeight.Bold) }
        items(vm.profiles.withIndex().filter { it.value.listGroup == 2 }.toList(), key = { it.value.key() }) { entry ->
            ServerRow(vm, entry.index, entry.value)
        }
    }
}

@Composable
private fun ServerRow(vm: MainViewModel, index: Int, p: Profile) {
    val selected = index == vm.selected
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { vm.select(index) }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = { vm.select(index) })
        Column(Modifier.weight(1f)) {
            Text(p.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Text("${p.protocol.uppercase()} · ${p.address}:${p.port}", style = MaterialTheme.typography.bodySmall, color = Oxyvo.Muted, maxLines = 1)
        }
        Text(if (p.ping >= 0) "${p.ping} мс" else "—", color = pingColor(p.ping), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 4.dp))
        TextButton(onClick = { vm.setProfileListGroup(index, if (p.listGroup == 1) 2 else 1) }) {
            Text(if (p.listGroup == 1) "В Ч.С." else "В Б.С.", color = Oxyvo.Muted)
        }
        TextButton(onClick = { vm.delete(index) }) { Text("✕", color = Oxyvo.Muted) }
    }
}
