package com.example.vlessvpn

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun ServersScreen(vm: MainViewModel) {
    var input by remember { mutableStateOf("") }
    val visible = vm.profiles
        .mapIndexed { i, p -> i to p }
        .filter { vm.catFilter == 0 || it.second.cat == vm.catFilter }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val text = result.contents
        if (!text.isNullOrBlank()) vm.add(text) else vm.message = "QR-код не распознан"
    }

    LazyColumn(
        Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { Text("Серверы", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Oxyvo.Green) }
        item { CategoryChips(vm) }
        item {
            Text(categoryHint(vm.catFilter), style = MaterialTheme.typography.bodySmall, color = Oxyvo.Muted)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassButton("Обновить список", { vm.autoSetup() }, Modifier.weight(1f), enabled = !vm.busy, primary = true)
                GlassButton(
                    "Проверить пинг",
                    { vm.retest() },
                    Modifier.weight(1f),
                    enabled = !vm.busy && vm.profiles.isNotEmpty(),
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                GlassButton(
                    "Обновить через Яндекс Переводчик",
                    { vm.autoSetup(viaYandex = true) },
                    Modifier.fillMaxWidth(),
                    enabled = !vm.busy,
                )
                Text(
                    "Если интернет глушат (белые списки): серверы Б.С. загрузятся через translate.yandex.ru. " +
                        "Способ нестабильный — часть конфигов может прийти испорченной.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Oxyvo.Muted,
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                GlassButton(
                    "Сканер обхода блокировок",
                    { vm.scan() },
                    Modifier.fillMaxWidth(),
                    enabled = !vm.busy && vm.profiles.isNotEmpty(),
                    primary = true,
                )
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
                AnimatedVisibility(visible = vm.busy) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(Modifier.fillMaxWidth(), color = Oxyvo.Green)
                        Text(vm.progress ?: "Подождите…", style = MaterialTheme.typography.bodySmall, color = Oxyvo.Muted)
                    }
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
                label = { Text("Подписка (https://…) или vless://…", maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassButton(
                    "Добавить",
                    { vm.add(input); input = "" },
                    Modifier.weight(1f),
                    enabled = input.isNotBlank() && !vm.busy,
                    primary = true,
                )
                GlassButton(
                    "Сканировать QR",
                    {
                        scanLauncher.launch(
                            ScanOptions().apply {
                                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                setPrompt("Наведите камеру на QR-код подписки")
                                setBeepEnabled(false)
                            },
                        )
                    },
                    Modifier.weight(1f),
                    enabled = !vm.busy,
                )
            }
        }
        items(visible, key = { it.first }) { (i, p) ->
            val sel = i == vm.selected
            GlassRow(sel, { vm.select(i) }, Modifier.fillMaxWidth().animateItem()) {
                RadioButton(
                    selected = sel,
                    onClick = { vm.select(i) },
                    colors = RadioButtonDefaults.colors(selectedColor = Oxyvo.Green, unselectedColor = Oxyvo.Muted),
                )
                Column(Modifier.weight(1f)) {
                    Text(p.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        (if (p.cat == Defaults.CAT_WL) "Б.С. · " else "Ч.С. · ") +
                            "${p.protocol.uppercase()} · ${p.address}:${p.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Oxyvo.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (p.scanned) {
                        Text(
                            scanText(p),
                            style = MaterialTheme.typography.labelSmall,
                            color = scanColor(p),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Text(
                    if (p.ping >= 0) "${p.ping} мс" else "—",
                    color = pingColor(p.ping),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
                Text(
                    "✕",
                    color = Oxyvo.Muted,
                    modifier = Modifier.clickable { vm.delete(i) }.padding(horizontal = 10.dp, vertical = 8.dp),
                )
            }
        }
    }
}
