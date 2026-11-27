package com.example.vlessvpn

class Src(val url: String, val cat: Int, val mirrors: List<String> = emptyList())

/** Встроенные публичные списки серверов (GitHub). Приложение само их загружает и проверяет. */
object Defaults {
    const val CAT_WL = 1   // против белых списков (Б.С.)
    const val CAT_BL = 2   // против чёрных списков (Ч.С.) — обычный обход блокировок

    private const val IG = "https://raw.githubusercontent.com/igareck/vpn-configs-for-russia/main/"
    private const val BARRY = "https://raw.githubusercontent.com/barry-far/V2ray-config/main/"
    private const val EPO = "https://raw.githubusercontent.com/Epodonios/v2ray-configs/refs/heads/main/"

    /** Дополнительные публичные списки (обычные блокировки). Мёртвые просто пропускаются. */
    private val EXTRA_BL = listOf(
        "https://github.com/Delta-Kronecker/V2ray-Config/raw/refs/heads/main/config/protocols/vless.txt",
        "https://github.com/Delta-Kronecker/V2ray-Config/raw/refs/heads/main/config/protocols/vmess.txt",
        "https://github.com/Delta-Kronecker/V2ray-Config/raw/refs/heads/main/config/protocols/trojan.txt",
        "https://raw.githubusercontent.com/ebrasha/free-v2ray-public-list/refs/heads/main/all_extracted_configs.txt",
        "https://raw.githubusercontent.com/ebrasha/free-v2ray-public-list/refs/heads/main/V2Ray-Config-By-EbraSha.txt",
        "https://raw.githubusercontent.com/MatinGhanbari/v2ray-configs/refs/heads/main/subscriptions/v2ray/super-sub.txt",
        "https://raw.githubusercontent.com/MatinGhanbari/v2ray-configs/main/subscriptions/filtered/subs/vless.txt",
        "https://raw.githubusercontent.com/SoliSpirit/v2ray-configs/refs/heads/main/Protocols/vless.txt",
        "https://raw.githubusercontent.com/Danialsamadi/v2go/refs/heads/main/AllConfigsSub.txt",
        "https://raw.githubusercontent.com/Kolandone/v2raycollector/refs/heads/main/vless.txt",
        "https://raw.githubusercontent.com/Surfboardv2ray/TGParse/main/splitted/vless",
        "https://raw.githubusercontent.com/F0rc3Run/F0rc3Run/main/splitted-by-protocol/vless.txt",
        "https://raw.githubusercontent.com/sakha1370/OpenRay/refs/heads/main/output/all_valid_proxies.txt",
        "https://raw.githubusercontent.com/V2RayRoot/V2RayConfig/refs/heads/main/Config/vless.txt",
        "https://raw.githubusercontent.com/hamedcode/port-based-v2ray-configs/main/sub/vless.txt",
        "https://raw.githubusercontent.com/10ium/ScrapeAndCategorize/refs/heads/main/output_configs/Vless.txt",
        "https://raw.githubusercontent.com/sevcator/5ubscrpt10n/main/protocols/vl.txt",
        "https://raw.githubusercontent.com/roosterkid/openproxylist/main/V2RAY_RAW.txt",
        "https://raw.githubusercontent.com/mohamadfg-dev/telegram-v2ray-configs-collector/refs/heads/main/category/xhttp.txt",
        "https://raw.githubusercontent.com/ermaozi/get_subscribe/main/subscribe/v2ray.txt",
        "https://raw.githubusercontent.com/Leon406/SubCrawler/master/sub/share/vless",
        "https://raw.githubusercontent.com/Mahdi0024/ProxyCollector/master/sub/proxies.txt",
        "https://raw.githubusercontent.com/MrMohebi/xray-proxy-grabber-telegram/master/collected-proxies/row-url/all.txt",
        "https://raw.githubusercontent.com/Pawdroid/Free-servers/main/sub",
        "https://raw.githubusercontent.com/awesome-vpn/awesome-vpn/master/all",
        "https://raw.githubusercontent.com/nscl5/5/refs/heads/main/configs/all.txt",
    )

    /**
     * Список источников, который можно менять БЕЗ пересборки приложения: файл sources.json в вашем репозитории.
     * Если какой-то список «протух» — поправьте sources.json на GitHub, приложение подхватит при обновлении.
     */
    val REMOTE_SOURCES = listOf(
        "https://raw.githubusercontent.com/max9283/oxyvoVPN/main/sources.json",
        "https://cdn.jsdelivr.net/gh/max9283/oxyvoVPN@main/sources.json",
    )

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
            Src(IG + "Vless-Reality-White-Lists-Rus-Mobile-2.txt", CAT_WL),
            Src("https://raw.githubusercontent.com/ByeWhiteLists/ByeWhiteLists2/refs/heads/main/ByeWhiteLists2.txt", CAT_WL),
            Src("https://raw.githubusercontent.com/kort0881/vpn-checker-backend/main/checked/RU_Best/ru_white_part3.txt", CAT_WL),
            // Против чёрных списков (обычные блокировки)
            Src(IG + "BLACK_VLESS_RUS.txt", CAT_BL),
            Src(IG + "BLACK_VLESS_RUS_mobile.txt", CAT_BL),
            Src(BARRY + "Splitted-By-Protocol/vless.txt", CAT_BL),
            Src(BARRY + "Splitted-By-Protocol/trojan.txt", CAT_BL),
            Src(BARRY + "Splitted-By-Protocol/vmess.txt", CAT_BL),
            Src("https://github.com/Epodonios/v2ray-configs/raw/main/Splitted-By-Protocol/vless.txt", CAT_BL),
        ) +
            (1..8).map { Src(BARRY + "Sub$it.txt", CAT_BL) } +
            (1..8).map { Src(EPO + "Sub$it.txt", CAT_BL) } +
            EXTRA_BL.map { Src(it, CAT_BL) } +
            (1..10).map { Src("https://raw.githubusercontent.com/AvenCores/goida-vpn-configs/refs/heads/main/githubmirror/$it.txt", CAT_BL) }

    const val MAX_PER_CAT = 1500   // сколько серверов каждой категории проверять пингом
    const val KEEP_PER_CAT = 150   // сколько лучших оставить в каждой категории
    const val SCAN_BATCH = 12      // сколько серверов сканировать за раз
    const val SCAN_MAX_BATCHES = 5 // максимум пачек при автоподключении
}
