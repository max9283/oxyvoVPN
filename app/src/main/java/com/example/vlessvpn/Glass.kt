package com.example.vlessvpn

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** «Жидкое стекло»: полупрозрачные градиентные панели со светлой кромкой. */
object Glass {
    val Fill = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.04f)))
    val FillSelected = Brush.linearGradient(
        listOf(Oxyvo.Green.copy(alpha = 0.40f), Oxyvo.GreenDeep.copy(alpha = 0.30f)),
    )
    val Border = Brush.linearGradient(
        listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.06f), Oxyvo.Green.copy(alpha = 0.30f)),
    )
    val BorderSelected = Brush.linearGradient(listOf(Oxyvo.Green.copy(alpha = 0.95f), Color.White.copy(alpha = 0.25f)))
}

fun Modifier.glass(shape: Shape = RoundedCornerShape(20.dp), selected: Boolean = false): Modifier =
    this.clip(shape)
        .background(if (selected) Glass.FillSelected else Glass.Fill)
        .border(1.dp, if (selected) Glass.BorderSelected else Glass.Border, shape)

/** Фон: тёмно-зелёный градиент со светящимися пятнами, сквозь которые «просвечивает» стекло. */
@Composable
fun GlassBackground(content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF03100A), Color(0xFF082015), Color(0xFF03100A))),
        ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c1 = Offset(size.width * 0.9f, size.height * 0.10f)
            val r1 = size.width * 0.75f
            drawCircle(
                brush = Brush.radialGradient(listOf(Oxyvo.Green.copy(alpha = 0.32f), Color.Transparent), center = c1, radius = r1),
                radius = r1,
                center = c1,
            )
            val c2 = Offset(size.width * 0.05f, size.height * 0.72f)
            val r2 = size.width * 0.70f
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFF00C2A8).copy(alpha = 0.24f), Color.Transparent), center = c2, radius = r2),
                radius = r2,
                center = c2,
            )
        }
        content()
    }
}

/** Кнопка: одна строка текста, плавное нажатие и плавная смена вида. */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false,
) {
    val shape = RoundedCornerShape(16.dp)
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, spring(dampingRatio = 0.6f, stiffness = 500f), label = "press")
    val a by animateFloatAsState(if (primary) 1f else 0f, tween(250), label = "prim")
    val textColor by animateColorAsState(if (primary) Color.White else Oxyvo.Green, tween(250), label = "btnText")
    Box(
        modifier
            .height(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .alpha(if (enabled) 1f else 0.4f)
            .glass(shape)
            .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.matchParentSize().alpha(a).background(Glass.FillSelected, shape).border(1.dp, Glass.BorderSelected, shape))
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
    }
}

/** Чип: никогда не переносит текст (внутри горизонтально прокручиваемой строки). */
@Composable
fun GlassChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, spring(dampingRatio = 0.6f, stiffness = 500f), label = "chipPress")
    val a by animateFloatAsState(if (selected) 1f else 0f, tween(250), label = "chipSel")
    val textColor by animateColorAsState(if (selected) Color.White else Oxyvo.Muted, tween(250), label = "chipText")
    Box(
        Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .glass(shape)
            .clickable(interactionSource = source, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.matchParentSize().alpha(a).background(Glass.FillSelected, shape).border(1.dp, Glass.BorderSelected, shape))
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            maxLines = 1,
            softWrap = false,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** Строка списка: выбор подсвечивается плавно. */
@Composable
fun GlassRow(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val a by animateFloatAsState(if (selected) 1f else 0f, tween(250), label = "rowSel")
    Box(modifier.glass(shape).clickable(onClick = onClick)) {
        Box(Modifier.matchParentSize().alpha(a).background(Glass.FillSelected, shape).border(1.dp, Glass.BorderSelected, shape))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun NavIcon(index: Int, color: Color) {
    Canvas(Modifier.size(24.dp)) {
        val w = size.width
        val sw = 2.dp.toPx()
        when (index) {
            0 -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, w * 0.12f)
                    lineTo(w * 0.9f, w * 0.48f)
                    lineTo(w * 0.8f, w * 0.48f)
                    lineTo(w * 0.8f, w * 0.88f)
                    lineTo(w * 0.2f, w * 0.88f)
                    lineTo(w * 0.2f, w * 0.48f)
                    lineTo(w * 0.1f, w * 0.48f)
                    close()
                }
                drawPath(path, color, style = Stroke(width = sw, join = StrokeJoin.Round))
            }
            1 -> {
                for (k in 0..2) {
                    val y = w * (0.25f + 0.25f * k)
                    drawRoundRect(
                        color,
                        topLeft = Offset(w * 0.12f, y - w * 0.07f),
                        size = Size(w * 0.76f, w * 0.14f),
                        cornerRadius = CornerRadius(w * 0.07f),
                        style = Stroke(sw),
                    )
                }
            }
            else -> {
                for (r in 0..1) {
                    for (c in 0..1) {
                        drawRoundRect(
                            color,
                            topLeft = Offset(w * (0.12f + 0.42f * c), w * (0.12f + 0.42f * r)),
                            size = Size(w * 0.34f, w * 0.34f),
                            cornerRadius = CornerRadius(w * 0.08f),
                            style = Stroke(sw),
                        )
                    }
                }
            }
        }
    }
}

/** Нижняя стеклянная панель навигации. */
@Composable
fun GlassBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    val titles = listOf("Главная", "Серверы", "Приложения")
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
            .glass(RoundedCornerShape(28.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        titles.forEachIndexed { i, title ->
            val sel = i == selected
            val shape = RoundedCornerShape(22.dp)
            val a by animateFloatAsState(if (sel) 1f else 0f, tween(300), label = "navSel")
            val color by animateColorAsState(if (sel) Color.White else Oxyvo.Muted, tween(300), label = "navColor")
            Box(Modifier.weight(1f).clip(shape).clickable { onSelect(i) }) {
                Box(Modifier.matchParentSize().alpha(a).background(Glass.FillSelected, shape).border(1.dp, Glass.BorderSelected, shape))
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NavIcon(i, color)
                    Text(
                        title,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp,
                        color = color,
                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
