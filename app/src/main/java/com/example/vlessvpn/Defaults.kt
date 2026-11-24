package com.example.vlessvpn

/** Встроенные публичные списки серверов (GitHub). Приложение само их загружает и проверяет. */
object Defaults {
    val SOURCES = listOf(
        "https://raw.githubusercontent.com/barry-far/V2ray-config/main/Sub1.txt",
        "https://raw.githubusercontent.com/barry-far/V2ray-config/main/Sub2.txt",
        "https://raw.githubusercontent.com/barry-far/V2ray-config/main/Sub3.txt",
        "https://raw.githubusercontent.com/Epodonios/v2ray-configs/refs/heads/main/Sub1.txt",
        "https://raw.githubusercontent.com/Epodonios/v2ray-configs/refs/heads/main/Sub2.txt",
        "https://raw.githubusercontent.com/Epodonios/v2ray-configs/refs/heads/main/Sub3.txt",
    )
    const val MAX_CANDIDATES = 700   // сколько серверов проверять пингом
    const val KEEP = 40              // сколько лучших оставить
}
