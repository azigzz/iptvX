# TODOs futuros

- Persistir playlists manuais em DataStore/Room para aparecerem após reiniciar sem novo sync.
- Completar VOD com categorias, detalhes, capas, progresso e favoritos.
- Completar séries com temporadas e episódios via Xtream.
- Expandir parser XMLTV com cache incremental e mapeamento por `tvg-id`/`tvg-name`.
- Sincronizar favoritos/histórico em background com retry/backoff.
- Criar tela real de controle parental por PIN e ocultação de categorias.
- Adicionar ordenação manual de categorias/canais.
- Implementar backup/restore local das configurações não sensíveis.
- Adicionar migration inicial versionada do Prisma para produção.
- Adicionar rate limit persistente/Redis em produção.
- Adicionar CORS restrito por domínio em produção.
- Criar pipeline CI para `npm test`, `next build` e `assembleRelease`.
- Adicionar testes instrumentados em Android TV para foco D-pad.
