export type XtreamCredentials = {
  serverUrl: string;
  username: string;
  password: string;
};

export type XtreamErrorCode =
  | "SERVER_OFFLINE"
  | "INVALID_CREDENTIALS"
  | "EXPIRED"
  | "INVALID_JSON"
  | "TIMEOUT"
  | "SSL"
  | "UNKNOWN";

export class XtreamClientError extends Error {
  constructor(
    message: string,
    public code: XtreamErrorCode
  ) {
    super(message);
  }
}

export class XtreamClient {
  constructor(
    private credentials: XtreamCredentials,
    private fetcher: typeof fetch = fetch,
    private timeoutMs = 10_000
  ) {}

  async accountInfo() {
    return this.call<Record<string, unknown>>({});
  }

  async liveCategories() {
    return this.call<unknown[]>({ action: "get_live_categories" });
  }

  async liveStreams(categoryId?: string) {
    return this.call<unknown[]>({
      action: "get_live_streams",
      ...(categoryId ? { category_id: categoryId } : {})
    });
  }

  async vodCategories() {
    return this.call<unknown[]>({ action: "get_vod_categories" });
  }

  async vodStreams(categoryId?: string) {
    return this.call<unknown[]>({
      action: "get_vod_streams",
      ...(categoryId ? { category_id: categoryId } : {})
    });
  }

  async seriesCategories() {
    return this.call<unknown[]>({ action: "get_series_categories" });
  }

  async series(categoryId?: string) {
    return this.call<unknown[]>({
      action: "get_series",
      ...(categoryId ? { category_id: categoryId } : {})
    });
  }

  async seriesInfo(seriesId: string) {
    return this.call<Record<string, unknown>>({ action: "get_series_info", series_id: seriesId });
  }

  async shortEpg(streamId: string) {
    return this.call<Record<string, unknown>>({ action: "get_short_epg", stream_id: streamId });
  }

  private async call<T>(params: Record<string, string>) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), this.timeoutMs);
    const url = new URL("player_api.php", normalizeBaseUrl(this.credentials.serverUrl));
    url.searchParams.set("username", this.credentials.username);
    url.searchParams.set("password", this.credentials.password);
    Object.entries(params).forEach(([key, value]) => url.searchParams.set(key, value));

    try {
      const response = await this.fetcher(url, { signal: controller.signal, redirect: "follow" });
      if (!response.ok) {
        throw new XtreamClientError(`Servidor respondeu HTTP ${response.status}`, "SERVER_OFFLINE");
      }

      const text = await response.text();
      let payload: T;
      try {
        payload = JSON.parse(text) as T;
      } catch {
        throw new XtreamClientError("JSON invalido na resposta Xtream", "INVALID_JSON");
      }

      if (isInvalidAccount(payload)) {
        throw new XtreamClientError("Usuario ou senha invalidos", "INVALID_CREDENTIALS");
      }
      if (isExpiredAccount(payload)) {
        throw new XtreamClientError("Lista expirada", "EXPIRED");
      }

      return payload;
    } catch (error) {
      if (error instanceof XtreamClientError) throw error;
      if (error instanceof Error && error.name === "AbortError") {
        throw new XtreamClientError("Timeout ao chamar Xtream", "TIMEOUT");
      }
      if (error instanceof Error && /ssl|certificate|tls/i.test(error.message)) {
        throw new XtreamClientError(error.message, "SSL");
      }
      throw new XtreamClientError(error instanceof Error ? error.message : "Erro desconhecido", "UNKNOWN");
    } finally {
      clearTimeout(timer);
    }
  }
}

function normalizeBaseUrl(value: string) {
  return value.endsWith("/") ? value : `${value}/`;
}

function isInvalidAccount(payload: unknown) {
  const info = (payload as { user_info?: { auth?: number | string } }).user_info;
  return info?.auth === 0 || info?.auth === "0";
}

function isExpiredAccount(payload: unknown) {
  const status = (payload as { user_info?: { status?: string } }).user_info?.status;
  return typeof status === "string" && status.toLowerCase() === "expired";
}
