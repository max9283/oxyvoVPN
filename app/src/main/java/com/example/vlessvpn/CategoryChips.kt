package com.example.vlessvpn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Переключатель: все серверы / против белых списков / против чёрных списков. */
@Composable
fun CategoryChips(vm: MainViewModel, modifier: Modifier = Modifier) {
    val wl = vm.profiles.count { it.cat == Defaults.CAT_WL }
    val bl = vm.profiles.count { it.cat == Defaults.CAT_BL }
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = vm.catFilter == 0, onClick = { vm.chooseCat(0) }, label = { Text("Все") })
        FilterChip(
            selected = vm.catFilter == Defaults.CAT_WL,
            onClick = { vm.chooseCat(Defaults.CAT_WL) },
            label = { Text("Против Б.С. ($wl)") },
        )
        FilterChip(
            selected = vm.catFilter == Defaults.CAT_BL,
            onClick = { vm.chooseCat(Defaults.CAT_BL) },
            label = { Text("Против Ч.С. ($bl)") },
        )
    }
}

fun categoryHint(cat: Int): String = when (cat) {
    Defaults.CAT_WL -> "Серверы для обхода белых списков (мобильный интернет РФ)."
    Defaults.CAT_BL -> "Обычные серверы для обхода блокировок (чёрные списки)."
    else -> "Показаны серверы всех категорий."
}
