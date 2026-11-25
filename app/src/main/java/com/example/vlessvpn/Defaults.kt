package com.example.vlessvpn

class Src(val url: String, val cat: Int, val mirrors: List<String> = emptyList())

/** Встроенные публичные списки серверов (GitHub). Приложение само их загружает и проверяет. */
object Defaults {
    const val CAT_WL = 1   // против белых списков (Б.С.)
    const val CAT_BL = 2   // против чёрных списков (Ч.С.) — обычный обход блокировок

    private const val IG = "https://raw.githubusercontent.com/igareck/vpn-configs-for-russia/main/"
    private const val BARRY = "https://raw.githubusercontent.com/barry-far/V2ray-config/main/"
    private const val EPO = "https://raw.githubusercontent.com/Epodonios/v2ray-configs/refs/heads/main/"

    val SOURCES: List<Src> =
        listOf(
            // Против белых списков (мобильный интернет РФ)
            Src(IG + "Vless-Reality-White-Lists-Rus-Mobile.txt", CAT_WL),
            Src(IG + "WHITE-CIDR-RU-all.txt", CAT_WL),
            Src(IG + "WHITE-CIDR-RU-checked.txt", CAT_WL),
            Src(IG + "WHITE-SNI-RU-all.txt", CAT_WL),
            // Случайная выборка свежих серверов для белых списков (несколько зеркал на разных хостингах)
            Src(
                "https://random.solovyov-jenya2004.workers.dev/?n=300",
                CAT_WL,
                mirrors = listOf(
                    "https://solovyov-jenya2004.vercel.app/random/?n=300",
                    "https://all-subs.netlify.app/random/?n=300",
                    "https://all-subs.solovyov-jenya2004.deno.net/random/?n=300",
                    "https://ddrnvxbsxvidyagqoewg.supabase.co/functions/v1/all-subs/random/?n=300",
                    "https://all-subs.onrender.com/random/?n=300",
                ),
            ),
            // Против чёрных списков (обычные блокировки)
            Src(IG + "BLACK_VLESS_RUS.txt", CAT_BL),
            Src(IG + "BLACK_VLESS_RUS_mobile.txt", CAT_BL),
            Src(BARRY + "Splitted-By-Protocol/vless.txt", CAT_BL),
            Src(BARRY + "Splitted-By-Protocol/trojan.txt", CAT_BL),
            Src(BARRY + "Splitted-By-Protocol/vmess.txt", CAT_BL),
            Src("https://github.com/Epodonios/v2ray-configs/raw/main/Splitted-By-Protocol/vless.txt", CAT_BL),
        ) +
            (1..8).map { Src(BARRY + "Sub$it.txt", CAT_BL) } +
            (1..8).map { Src(EPO + "Sub$it.txt", CAT_BL) }

    const val MAX_PER_CAT = 1500   // сколько серверов каждой категории проверять пингом
    const val KEEP_PER_CAT = 150   // сколько лучших оставить в каждой категории
    const val SCAN_BATCH = 12      // сколько серверов сканировать за раз
    const val SCAN_MAX_BATCHES = 5 // максимум пачек при автоподключении
}
