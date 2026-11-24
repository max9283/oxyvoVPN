package com.example.vlessvpn

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp

@Composable
fun SplitScreen(vm: MainViewModel) {
    var query by remember { mutableStateOf("") }
    val mode = vm.splitMode
    val selectedApps = vm.splitApps

    val list = remember(vm.apps, query, selectedApps) {
        vm.apps
            .filter { query.isBlank() || it.label.contains(query, ignoreCase = true) }
            .sortedByDescending { it.pkg in selectedApps }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = mode == 0, onClick = { vm.chooseMode(0) }, label = { Text("Выкл") })
            FilterChip(selected = mode == 1, onClick = { vm.chooseMode(1) }, label = { Text("Белый список") })
            FilterChip(selected = mode == 2, onClick = { vm.chooseMode(2) }, label = { Text("Чёрный список") })
        }
        Text(
            when (mode) {
                1 -> "Б.С.: через VPN работают ТОЛЬКО отмеченные приложения, остальные — напрямую."
                2 -> "Ч.С.: отмеченные приложения работают В ОБХОД VPN, все остальные — через VPN."
                else -> "Все приложения работают через VPN."
            },
            style = MaterialTheme.typography.bodySmall,
            color = Oxyvo.Muted,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            "Изменения применятся при следующем подключении.",
            style = MaterialTheme.typography.labelSmall,
            color = Oxyvo.Muted,
        )

        if (mode == 0) return@Column

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Поиск приложения") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Выбрано: ${selectedApps.size}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { vm.clearApps() }, enabled = selectedApps.isNotEmpty()) { Text("Сбросить") }
        }

        if (vm.apps.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(list, key = { it.pkg }) { app ->
                    val checked = app.pkg in selectedApps
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (checked) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                            )
                            .clickable { vm.toggleApp(app.pkg) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(app.icon, null, Modifier.size(38.dp))
                        Text(
                            app.label,
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                            maxLines = 1,
                        )
                        Checkbox(checked = checked, onCheckedChange = { vm.toggleApp(app.pkg) })
                    }
                }
            }
        }
    }
}
