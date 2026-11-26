package com.example.vlessvpn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    vm: MainViewModel,
    onAuto: () -> Unit,
    onConnectSelected: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val status by VpnState.status.collectAsState()
    val error by VpnState.error.collectAsState()
    val connected = status == VpnStatus.CONNECTED
    val connecting = status == VpnStatus.CONNECTING
    val active = connected || connecting
    val searching = vm.busy
    val profile = vm.profiles.getOrNull(vm.selected)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(72.dp).clip(RoundedCornerShape(21.dp)).border(1.dp, Glass.Border, RoundedCornerShape(21.dp))) {
            Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.fillMaxSize())
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(8.dp))
        Text("Oxyvo VPN", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Oxyvo.Green, maxLines = 1)
        Text("быстро · свободно", style = MaterialTheme.typography.bodySmall, color = Oxyvo.Muted, maxLines = 1)

        Spacer(Modifier.height(20.dp))

        // Главная кнопка: автоподключение к самому быстрому серверу
        Box(
            Modifier.size(210.dp).border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val on = active || searching
            val colors = if (on) listOf(Color(0xFF3DF28B), Color(0xFF00843B))
            else listOf(Color(0x33FFFFFF), Color(0x0DFFFFFF))
            Box(
                Modifier
                    .size(176.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(colors))
                    .border(2.dp, Glass.Border, CircleShape)
                    .clickable(enabled = !searching) { if (active) onDisconnect() else onAuto() },
                contentAlignment = Alignment.Center,
            ) {
                PowerIcon(if (on) Color(0xFF00210E) else Oxyvo.Green)
            }
            if (connecting || searching) {
                CircularProgressIndicator(Modifier.size(204.dp), color = Oxyvo.Green, strokeWidth = 4.dp)
            }
        }

        Spacer(Modifier.height(16.dp))
        val (title, color) = when {
            searching -> "Подбор сервера…" to Oxyvo.Warn
            status == VpnStatus.CONNECTED -> "Подключено" to Oxyvo.Green
            status == VpnStatus.CONNECTING -> "Подключение…" to Oxyvo.Warn
            status == VpnStatus.ERROR -> "Ошибка" to Oxyvo.Bad
            else -> "Отключено" to Oxyvo.Muted
        }
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = color, maxLines = 1)
        val sub = when {
            searching -> vm.progress
            status == VpnStatus.ERROR -> error ?: "неизвестная ошибка"
            active -> "Нажмите кнопку, чтобы отключиться"
            else -> vm.message ?: "Нажмите кнопку — подключимся к самому быстрому серверу"
        }
        if (sub != null) {
            Text(
                sub,
                style = MaterialTheme.typography.bodySmall,
                color = if (status == VpnStatus.ERROR && !searching) Oxyvo.Bad else Oxyvo.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(Modifier.height(20.dp))
        CategoryChips(vm)
        Text(
            categoryHint(vm.catFilter),
            style = MaterialTheme.typography.labelSmall,
            color = Oxyvo.Muted,
            modifier = Modifier.padding(top = 6.dp),
        )

        Spacer(Modifier.height(16.dp))

        // Текущий сервер
        Row(
            Modifier.fillMaxWidth().glass().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Выбранный сервер", style = MaterialTheme.typography.labelSmall, color = Oxyvo.Muted)
                Text(
                    profile?.name ?: "не выбран",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (profile != null) {
                    Text(
                        "${profile.protocol.uppercase()} · ${profile.address}:${profile.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Oxyvo.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (profile.scanned) {
                        Text(
                            scanText(profile),
                            style = MaterialTheme.typography.labelSmall,
                            color = scanColor(profile),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (profile != null && profile.ping >= 0) {
                Text(
                    "${profile.ping} мс",
                    color = pingColor(profile.ping),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        TrafficCard(vm, connected)

        Spacer(Modifier.height(12.dp))
        GlassButton(
            "Подключиться к выбранному серверу",
            onClick = onConnectSelected,
            enabled = profile != null && !active && !searching,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TrafficCard(vm: MainViewModel, connected: Boolean) {
    Column(Modifier.fillMaxWidth().glass().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Расход трафика",
                style = MaterialTheme.typography.labelMedium,
                color = Oxyvo.Muted,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Text(
                "Сбросить",
                style = MaterialTheme.typography.labelMedium,
                color = Oxyvo.Green,
                maxLines = 1,
                modifier = Modifier.clickable { vm.resetTraffic() },
            )
        }
        TrafficRow("Сессия", vm.sessionRx, vm.sessionTx)
        if (connected) TrafficRow("Скорость", vm.speedRx, vm.speedTx, perSecond = true)
        TrafficRow("Всего", vm.totalRx, vm.totalTx)
        Text(
            "Считается обмен приложения с сервером, значения приблизительные.",
            style = MaterialTheme.typography.labelSmall,
            color = Oxyvo.Muted,
        )
    }
}

@Composable
private fun TrafficRow(label: String, rx: Long, tx: Long, perSecond: Boolean = false) {
    val suffix = if (perSecond) "/с" else ""
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Oxyvo.Muted, fontSize = 14.sp, maxLines = 1, modifier = Modifier.weight(0.8f))
        Text(
            "↓ ${formatBytes(rx)}$suffix",
            color = Oxyvo.Green,
            fontSize = 14.sp,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.weight(1.1f),
        )
        Text(
            "↑ ${formatBytes(tx)}$suffix",
            color = Color(0xFF7ED9E6),
            fontSize = 14.sp,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.weight(1.1f),
        )
    }
}

fun formatBytes(b: Long): String = when {
    b < 1024L -> "$b Б"
    b < 1024L * 1024 -> "%.1f КБ".format(b / 1024.0)
    b < 1024L * 1024 * 1024 -> "%.1f МБ".format(b / (1024.0 * 1024))
    else -> "%.2f ГБ".format(b / (1024.0 * 1024 * 1024))
}

fun pingColor(ping: Int): Color = when {
    ping < 0 -> Oxyvo.Muted
    ping < 300 -> Oxyvo.Green
    ping < 800 -> Oxyvo.Warn
    else -> Oxyvo.Bad
}

fun scanText(p: Profile): String =
    if (p.real < 0) "Сканер: не прошёл"
    else "Сканер: ${p.real} мс · YouTube ${if (p.yt) "✓" else "✗"} · Telegram ${if (p.tg) "✓" else "✗"}"

fun scanColor(p: Profile): Color = when {
    p.real < 0 -> Oxyvo.Bad
    p.yt && p.tg -> Oxyvo.Green
    else -> Oxyvo.Warn
}

@Composable
private fun PowerIcon(color: Color) {
    Canvas(Modifier.size(64.dp)) {
        val w = size.width
        val stroke = 6.dp.toPx()
        drawArc(
            color = color,
            startAngle = -60f,
            sweepAngle = 300f,
            useCenter = false,
            topLeft = Offset(w * 0.1f, w * 0.15f),
            size = Size(w * 0.8f, w * 0.8f),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        drawLine(
            color = color,
            start = Offset(w / 2, 0f),
            end = Offset(w / 2, w * 0.45f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}
