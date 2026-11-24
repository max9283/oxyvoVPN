# VlessVPN

Android VPN-клиент (Kotlin + Jetpack Compose) на ядре Xray.
Поддерживает VLESS (TCP/WS/gRPC, TLS и Reality), VMess, Trojan и импорт подписок.

## Как получить APK (без Android Studio)

1. Создайте репозиторий на GitHub и залейте туда содержимое этого проекта.
2. Откройте вкладку **Actions** → **Build APK** → **Run workflow**.
3. Когда сборка закончится, скачайте APK из **Artifacts** (`VlessVPN-apk`).
   Если запушить тег (`git tag v1.0 && git push --tags`), APK появится ещё и в **Releases**.

Воркфлоу сам скачивает ядро `libv2ray.aar` из
[2dust/AndroidLibXrayLite](https://github.com/2dust/AndroidLibXrayLite).

## Локальная сборка

```bash
./scripts/download-core.sh
gradle assembleRelease      # или откройте проект в Android Studio
```
(Android Studio сама создаст Gradle Wrapper.)

## Как пользоваться

Вставьте ссылку подписки (`https://...`) или одиночные ссылки `vless://`, `vmess://`, `trojan://`,
нажмите «Добавить», выберите сервер и нажмите «Подключиться».

## Структура

- `CoreBridge.kt` — единственное место, где вызывается ядро Xray
- `ConfigBuilder.kt` — генерация JSON-конфига Xray из ссылки
- `XrayVpnService.kt` — VpnService, поднимает TUN и отдаёт его ядру
- `Profile.kt` — разбор ссылок, `Subscription.kt` — загрузка подписок
