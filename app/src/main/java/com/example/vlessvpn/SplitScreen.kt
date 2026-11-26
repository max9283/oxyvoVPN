package com.example.vlessvpn

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        Text("Приложения", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Oxyvo.Green)
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GlassChip("Выкл", mode == 0) { vm.chooseMode(0) }
            GlassChip("Только выбранные", mode == 1) { vm.chooseMode(1) }
            GlassChip("Кроме выбранных", mode == 2) { vm.chooseMode(2) }
        }
        Text(
            when (mode) {
                1 -> "Белый список приложений: через VPN работают ТОЛЬКО отмеченные, остальные — напрямую."
                2 -> "Чёрный список приложений: отмеченные работают В ОБХОД VPN, все остальные — через VPN."
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
            label = { Text("Поиск приложения", maxLines = 1) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Oxyvo.Green,
                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                focusedContainerColor = Color.White.copy(alpha = 0.06f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                focusedLabelColor = Oxyvo.Green,
                cursorColor = Oxyvo.Green,
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        )
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Выбрано: ${selectedApps.size}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Text(
                "Сбросить",
                color = if (selectedApps.isNotEmpty()) Oxyvo.Green else Oxyvo.Muted,
                maxLines = 1,
                modifier = Modifier
                    .clickable(enabled = selectedApps.isNotEmpty()) { vm.clearApps() }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            )
        }

        if (vm.apps.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Oxyvo.Green)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(list, key = { it.pkg }) { app ->
                    val checked = app.pkg in selectedApps
                    Row(
                        Modifier.fillMaxWidth()
                            .glass(RoundedCornerShape(14.dp), selected = checked)
                            .clickable { vm.toggleApp(app.pkg) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(app.icon, null, Modifier.size(38.dp))
                        Text(
                            app.label,
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { vm.toggleApp(app.pkg) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Oxyvo.Green,
                                checkmarkColor = Color(0xFF00210E),
                                uncheckedColor = Oxyvo.Muted,
                            ),
                        )
                    }
                }
            }
        }
    }
}
