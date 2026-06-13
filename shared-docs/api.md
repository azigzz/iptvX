# API

## App

- `POST /api/device/register`: registra ou atualiza dispositivo e retorna MAC virtual, código e token inicial.
- `POST /api/device/refresh-code`: gera novo código usando `deviceId` + token.
- `POST /api/device/sync`: retorna playlists habilitadas e settings apenas do dispositivo autenticado.
- `POST /api/app/favorites`: cria/atualiza favorito local sincronizado por device.
- `POST /api/app/history`: cria/atualiza progresso.
- `POST /api/app/epg`: placeholder para EPG remoto; o app também pode baixar pela `epgUrl`.

## Painel

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/pair`
- `GET /api/admin/devices`
- `GET /api/admin/devices/:id`
- `POST /api/admin/devices/:id/playlists`
- `PATCH /api/admin/playlists/:id`
- `DELETE /api/admin/playlists/:id`
- `POST /api/admin/playlists/test`

## Payload de playlist

M3U:

```json
{
  "name": "Minha lista",
  "type": "M3U",
  "m3uUrl": "https://example.com/lista.m3u",
  "epgUrl": "https://example.com/epg.xml",
  "enabled": true
}
```

Xtream:

```json
{
  "name": "Minha lista",
  "type": "XTREAM",
  "serverUrl": "https://servidor.example",
  "username": "usuario",
  "password": "senha",
  "epgUrl": "https://example.com/epg.xml",
  "enabled": true
}
```
