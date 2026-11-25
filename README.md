# Oxyvo VPN

Android VPN-клиент (Kotlin + Jetpack Compose) на ядре Xray в зелёном стиле.

- VLESS (TCP / WS / gRPC, TLS, Reality), VMess, Trojan
- Сам загружает публичные списки серверов с GitHub, проверяет пинг и оставляет самые быстрые
- Свои подписки и ссылки `vless://` `vmess://` `trojan://`
- **Б.С.** (белый список): через VPN идут только выбранные приложения
- **Ч.С.** (чёрный список): выбранные приложения идут в обход VPN

## Сборка APK без Android Studio

1. Залейте содержимое проекта в репозиторий GitHub.
2. **Actions → Build APK → Run workflow**.
3. Скачайте APK из **Artifacts**.

Воркфлоу сам скачивает ядро `libv2ray.aar` из 2dust/AndroidLibXrayLite.
