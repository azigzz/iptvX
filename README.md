# iptvX v2

Monorepo para um player de mídia/IPTV de listas próprias/autorizadas, com app Android focado em Android TV/TV Box e painel web hospedável na Vercel.

O projeto não inclui listas prontas, canais, conteúdo protegido, bypass de DRM, bypass de loja ou mecanismos de ocultação.

## Estrutura

- `android-app`: app Kotlin + Jetpack Compose + Media3 ExoPlayer.
- `web-panel`: Next.js App Router + Prisma + PostgreSQL + Tailwind.
- `shared-docs`: arquitetura, API e TODOs.

## Rodar o painel local

1. Entre em `web-panel`.
2. Copie `.env.example` para `.env`.
3. Configure `DATABASE_URL`, `SECRET_KEY`, `DEVICE_TOKEN_SECRET` e `NEXT_PUBLIC_APP_PANEL_URL`. Se quiser painel sem login, use `DISABLE_ADMIN_LOGIN="true"`.
4. Instale dependências:

```bash
npm install
```

5. Gere o Prisma Client e aplique migration:

```bash
npm run prisma:generate
npm run prisma:migrate
```

6. Opcionalmente crie/atualize o admin seed:

```bash
npm run seed
```

7. Rode:

```bash
npm run dev
```

Abra `http://localhost:3000`. Com `DISABLE_ADMIN_LOGIN="true"`, o painel abre direto.

## Deploy na Vercel

1. Crie um PostgreSQL em Vercel Postgres, Neon ou Supabase.
2. Configure as variáveis do arquivo `web-panel/.env.example` no projeto da Vercel.
3. Aponte o Root Directory da Vercel para `web-panel`.
4. Rode a migration localmente contra o banco de produção ou use seu fluxo de CI:

```bash
npm run prisma:migrate
```

5. Defina `NEXT_PUBLIC_APP_PANEL_URL` como a URL pública do painel, por exemplo `https://seu-painel.vercel.app`.

## Rodar o Android

Requisitos recomendados:

- Android Studio atual.
- JDK 17.
- Android SDK 35.
- Um emulador Android TV ou dispositivo físico.

Passos:

1. Abra `android-app` no Android Studio.
2. Crie `local.properties` se o Android Studio não criar automaticamente.
3. Ajuste `DEFAULT_PANEL_URL` em `android-app/app/build.gradle.kts` para a URL do painel.
   - Emulador Android: `http://10.0.2.2:3000`.
   - Dispositivo/TV na rede: use o IP da máquina, por exemplo `http://192.168.0.10:3000`.
4. Rode o app em modo debug.

## Gerar APK

No diretório `android-app`:

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

No Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

O APK fica em `android-app/app/build/outputs/apk/`.

Observação: este workspace não tinha Android SDK nem Gradle instalado globalmente, então a compilação do APK precisa ser feita no Android Studio ou em uma máquina com toolchain Android.

## Parear dispositivo

1. Abra o app Android.
2. Ele gera um `deviceId` local estável, um MAC/ID virtual no formato `00:1A:79:XX:XX:XX` e um código de 6 caracteres.
3. No painel, entre em `Parear`.
4. Digite o MAC/ID virtual e o código.
5. Adicione uma playlist M3U ou Xtream no detalhe do dispositivo.
6. No app, clique em `Já adicionei, sincronizar`.

O MAC/ID exibido é um identificador virtual do app, não o MAC real do hardware.

## Funcionalidades implementadas

- Painel com login admin por env/cookie assinado.
- Dashboard, dispositivos, pareamento, playlists, teste de playlist e configurações.
- API de registro, refresh de código, sync, pareamento, favoritos e histórico.
- Prisma schema com Device, Playlist, DeviceSetting, Favorite, WatchHistory e SyncLog.
- Criptografia AES-GCM para credenciais de playlist usando `SECRET_KEY`.
- Hash HMAC para pairing code e token de dispositivo.
- Parser M3U/M3U8 em TypeScript e Kotlin.
- Cliente Xtream em TypeScript e Kotlin.
- App Android com pareamento, sync, adição manual M3U, cache Room, busca, categorias e player Media3.
- Estrutura pronta para VOD, séries, EPG, favoritos, histórico e controle parental.

## Testes

Painel:

```bash
cd web-panel
npm test
```

Android:

```bash
cd android-app
./gradlew testDebugUnitTest
```

## Variáveis de ambiente principais

- `DATABASE_URL`: conexão PostgreSQL.
- `ADMIN_EMAIL`: e-mail do admin.
- `ADMIN_PASSWORD`: senha simples para desenvolvimento.
- `ADMIN_PASSWORD_HASH`: alternativa a `ADMIN_PASSWORD`, formato `sha256:<hex>`.
- `DISABLE_ADMIN_LOGIN`: use `true` para abrir o painel sem login. Use apenas em painel privado/local.
- `SECRET_KEY`: chave para criptografar credenciais e assinar códigos.
- `DEVICE_TOKEN_SECRET`: chave para token por dispositivo.
- `NEXT_PUBLIC_APP_PANEL_URL`: URL pública usada no app/tela de pareamento.

## Segurança operacional

- Use HTTPS em produção.
- Use senhas fortes e segredos longos.
- Restrinja acesso ao painel.
- Cadastre somente listas próprias/autorizadas.
- Não exponha credenciais no frontend público.
