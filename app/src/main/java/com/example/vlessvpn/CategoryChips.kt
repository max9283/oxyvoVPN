package com.example.vlessvpn

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Переключатель: все серверы / против белых списков / против чёрных списков. */
@Composable
fun CategoryChips(vm: MainViewModel, modifier: Modifier = Modifier) {
    val wl = vm.profiles.count { it.cat == Defaults.CAT_WL }
    val bl = vm.profiles.count { it.cat == Defaults.CAT_BL }
    Row(
        modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GlassChip("Все", vm.catFilter == 0) { vm.chooseCat(0) }
        GlassChip("Против Б.С. ($wl)", vm.catFilter == Defaults.CAT_WL) { vm.chooseCat(Defaults.CAT_WL) }
        GlassChip("Против Ч.С. ($bl)", vm.catFilter == Defaults.CAT_BL) { vm.chooseCat(Defaults.CAT_BL) }
    }
}

fun categoryHint(cat: Int): String = when (cat) {
    Defaults.CAT_WL -> "Против Б.С.: серверы для мест, где открыты только госсервисы (белые списки, мобильный интернет)."
    Defaults.CAT_BL -> "Против Ч.С.: серверы для обхода блокировок YouTube, Telegram и т.д."
    else -> "Показаны серверы всех категорий."
}
