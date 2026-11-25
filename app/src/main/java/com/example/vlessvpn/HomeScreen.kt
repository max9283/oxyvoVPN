package com.example.vlessvpn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedButton
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
        // Логотип и название
        Box(Modifier.size(72.dp).clip(RoundedCornerShape(21.dp))) {
            Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.fillMaxSize())
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(8.dp))
        Text("Oxyvo VPN", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Oxyvo.Green)
        Text("быстро · безопасно · свободно", style = MaterialTheme.typography.bodySmall, color = Oxyvo.Muted)

        Spacer(Modifier.height(20.dp))

        // Главная кнопка: автоподключение к самому быстрому серверу
        Box(Modifier.size(210.dp), contentAlignment = Alignment.Center) {
            val on = active || searching
            val colors = if (on) listOf(Color(0xFF3DF28B), Color(0xFF00843B))
            else listOf(Color(0xFF1F3D2B), Color(0xFF10261A))
            Box(
                Modifier
                    .size(176.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(colors))
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
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = color)
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
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Выбранный сервер", style = MaterialTheme.typography.labelSmall, color = Oxyvo.Muted)
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

        // Второстепенная кнопка: подключиться к выбранному вручную серверу
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onConnectSelected,
            enabled = profile != null && !active && !searching,
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
