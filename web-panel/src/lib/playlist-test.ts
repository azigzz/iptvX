import { parseM3u } from "@/lib/iptv/m3u";
import { XtreamClient, XtreamClientError } from "@/lib/iptv/xtream";

export async function testPlaylist(payload: {
  type: "XTREAM" | "M3U";
  serverUrl?: string;
  username?: string;
  password?: string;
  m3uUrl?: string;
}) {
  if (payload.type === "M3U") {
    if (!payload.m3uUrl) return { ok: false, message: "URL M3U ausente" };
    const response = await fetch(payload.m3uUrl, { redirect: "follow", signal: AbortSignal.timeout(30_000) });
    if (!response.ok) return { ok: false, message: `Servidor respondeu HTTP ${response.status}` };
    const text = await response.text();
    const items = parseM3u(text);
    return {
      ok: items.length > 0,
      message: items.length > 0 ? `${items.length} itens encontrados` : "Nenhum canal encontrado"
    };
  }

  if (!payload.serverUrl || !payload.username || !payload.password) {
    return { ok: false, message: "Dados Xtream incompletos" };
  }

  try {
    const client = new XtreamClient({
      serverUrl: payload.serverUrl,
      username: payload.username,
      password: payload.password
    });
    await client.accountInfo();
    return { ok: true, message: "Xtream respondeu com sucesso" };
  } catch (error) {
    if (error instanceof XtreamClientError) return { ok: false, message: error.message, code: error.code };
    return { ok: false, message: "Falha ao testar Xtream" };
  }
}
