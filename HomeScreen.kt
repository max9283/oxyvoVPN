package com.example.vlessvpn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(vm: MainViewModel, onAutoConnect: () -> Unit, onConnectSelected: () -> Unit, onDisconnect: () -> Unit) {
    val status by VpnState.status.collectAsState()
    val error by VpnState.error.collectAsState()
    val connected = status == VpnStatus.CONNECTED
    val connecting = status == VpnStatus.CONNECTING
    val active = connected || connecting
    val profile = vm.profiles.getOrNull(vm.selected)
    val canClick = active || vm.profiles.any { it.listGroup == vm.preferredList && it.ping >= 0 }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Логотип и название
        Box(Modifier.size(84.dp).clip(RoundedCornerShape(24.dp))) {
            Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.fillMaxSize())
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(12.dp))
        Text("Oxyvo VPN", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Oxyvo.Green)
        Text("быстро · безопасно · свободно", style = MaterialTheme.typography.bodySmall, color = Oxyvo.Muted)

        Spacer(Modifier.weight(1f))

        // Кнопка включения
        Box(Modifier.size(210.dp), contentAlignment = Alignment.Center) {
            val colors = if (active) listOf(Color(0xFF3DF28B), Color(0xFF00843B))
            else listOf(Color(0xFF1F3D2B), Color(0xFF10261A))
            Box(
                Modifier
                    .size(176.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(colors))
                    .clickable(enabled = canClick) { if (active) onDisconnect() else onAutoConnect() },
                contentAlignment = Alignment.Center,
            ) {
                PowerIcon(if (active) Color(0xFF00210E) else Oxyvo.Green)
            }
            if (connecting) {
                CircularProgressIndicator(Modifier.size(204.dp), color = Oxyvo.Green, strokeWidth = 4.dp)
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("АВТОПОДКЛЮЧЕНИЕ", style = MaterialTheme.typography.labelMedium, color = Oxyvo.Green, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        val (title, color) = when (status) {
            VpnStatus.CONNECTED -> "Подключено" to Oxyvo.Green
            VpnStatus.CONNECTING -> "Подключение…" to Oxyvo.Warn
            VpnStatus.ERROR -> "Ошибка" to Oxyvo.Bad
            VpnStatus.DISCONNECTED -> "Отключено" to Oxyvo.Muted
        }
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = color)
        if (status == VpnStatus.ERROR) {
            Text(
                error ?: "неизвестная ошибка",
                style = MaterialTheme.typography.bodySmall,
                color = Oxyvo.Bad,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (profile == null && !active) {
            Text(
                vm.progress ?: "Серверов пока нет — откройте вкладку «Серверы»",
                style = MaterialTheme.typography.bodySmall,
                color = Oxyvo.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(Modifier.weight(1f))

        // Текущий сервер и отдельное подключение к выбранному
        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Сервер", style = MaterialTheme.typography.labelSmall, color = Oxyvo.Muted)
                Text(
                    profile?.name ?: "не выбран",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                )
                if (profile != null) {
                    Text(
                        "${profile.protocol.uppercase()} · ${profile.address}:${profile.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Oxyvo.Muted,
                        maxLines = 1,
                    )
                }
            }
            if (profile != null && profile.ping >= 0) {
                Text("${profile.ping} мс", color = pingColor(profile.ping), fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(10.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = onConnectSelected,
            enabled = !active && profile != null,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Подключиться к выбранному серверу") }
    }
}

fun pingColor(ping: Int): Color = when {
    ping < 0 -> Oxyvo.Muted
    ping < 300 -> Oxyvo.Green
    ping < 800 -> Oxyvo.Warn
    else -> Oxyvo.Bad
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
