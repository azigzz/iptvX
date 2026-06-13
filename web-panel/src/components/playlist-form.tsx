"use client";

import { FormEvent, useEffect, useMemo, useRef, useState } from "react";
import { useRouter } from "next/navigation";

type PlaylistType = "M3U" | "XTREAM";

export function PlaylistForm({ deviceId }: { deviceId: string }) {
  const router = useRouter();
  const firstFieldRef = useRef<HTMLInputElement>(null);
  const [type, setType] = useState<PlaylistType>("XTREAM");
  const [name, setName] = useState("");
  const [serverUrl, setServerUrl] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [m3uUrl, setM3uUrl] = useState("");
  const [epgUrl, setEpgUrl] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState<"test" | "save" | null>(null);
  const detectedXtream = useMemo(() => parseXtreamM3u(m3uUrl), [m3uUrl]);

  useEffect(() => {
    if (window.location.hash === "#add-playlist") {
      firstFieldRef.current?.focus();
    }
  }, []);

  const canSave =
    type === "XTREAM"
      ? serverUrl.trim().length > 0 && username.trim().length > 0 && password.length > 0
      : m3uUrl.trim().length > 0;

  function payload() {
    return {
      name: name.trim() || (type === "XTREAM" ? username.trim() || "Xtream" : "M3U"),
      type,
      serverUrl: type === "XTREAM" ? normalizeUrl(serverUrl) : undefined,
      username: type === "XTREAM" ? username.trim() : undefined,
      password: type === "XTREAM" ? password : undefined,
      m3uUrl: type === "M3U" ? normalizeUrl(m3uUrl) : undefined,
      epgUrl: epgUrl.trim() ? normalizeUrl(epgUrl) : undefined,
      enabled: true
    };
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!canSave || busy) return;
    setError("");
    setMessage("");
    setBusy("save");

    const response = await fetch(`/api/admin/devices/${deviceId}/playlists`, {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify(payload())
    });
    const data = await response.json().catch(() => ({}));
    setBusy(null);

    if (!response.ok) {
      setError(errorMessage(data.error, "Nao foi possivel salvar"));
      return;
    }

    setMessage("Playlist salva. Abra o app ou aguarde o proximo sync.");
    setName("");
    setServerUrl("");
    setUsername("");
    setPassword("");
    setM3uUrl("");
    setEpgUrl("");
    router.refresh();
  }

  async function test() {
    if (!canSave || busy) return;
    setError("");
    setMessage("Testando...");
    setBusy("test");

    const response = await fetch("/api/admin/playlists/test", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify(payload())
    });
    const data = await response.json().catch(() => ({}));
    setBusy(null);

    if (!response.ok || data.ok === false) {
      setMessage("");
      setError(data.message || errorMessage(data.error, "Teste falhou"));
      return;
    }
    setMessage(data.message || "Teste OK");
  }

  function useDetectedXtream() {
    if (!detectedXtream) return;
    setType("XTREAM");
    setServerUrl(detectedXtream.serverUrl);
    setUsername(detectedXtream.username);
    setPassword(detectedXtream.password);
    if (!name.trim()) setName(detectedXtream.username);
    setMessage("Dados Xtream preenchidos pela URL M3U.");
    setError("");
  }

  return (
    <form onSubmit={submit} className="mt-5 grid gap-4">
      <label className="grid gap-2 text-sm">
        Nome da lista
        <input
          ref={firstFieldRef}
          name="name"
          value={name}
          onChange={(event) => setName(event.target.value)}
          placeholder={type === "XTREAM" ? "Xtream" : "Minha lista"}
        />
      </label>
      <label className="grid gap-2 text-sm">
        Tipo
        <select value={type} onChange={(event) => setType(event.target.value as PlaylistType)}>
          <option value="XTREAM">Xtream Codes</option>
          <option value="M3U">M3U/M3U8</option>
        </select>
      </label>

      {type === "M3U" ? (
        <div className="grid gap-3">
          <label className="grid gap-2 text-sm">
            URL M3U
            <input
              name="m3uUrl"
              inputMode="url"
              value={m3uUrl}
              onChange={(event) => setM3uUrl(event.target.value.trim())}
              onBlur={() => setM3uUrl((value) => normalizeUrl(value))}
              placeholder="http://servidor/get.php?username=..."
            />
          </label>
          {detectedXtream ? (
            <button
              type="button"
              onClick={useDetectedXtream}
              className="rounded-md border border-signal/60 px-4 py-2 text-left text-sm text-signal hover:bg-signal/10"
            >
              Detectei Xtream nessa URL. Preencher como login Xtream.
            </button>
          ) : null}
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          <label className="grid gap-2 text-sm sm:col-span-2">
            DNS / Server URL
            <input
              name="serverUrl"
              inputMode="url"
              value={serverUrl}
              onChange={(event) => setServerUrl(event.target.value.trim())}
              onBlur={() => setServerUrl((value) => normalizeUrl(value))}
              placeholder="http://servidor.com:8080"
            />
          </label>
          <label className="grid gap-2 text-sm">
            Usuario
            <input name="username" value={username} onChange={(event) => setUsername(event.target.value.trim())} autoComplete="off" />
          </label>
          <label className="grid gap-2 text-sm">
            Senha
            <input name="password" value={password} onChange={(event) => setPassword(event.target.value)} type="password" autoComplete="off" />
          </label>
        </div>
      )}

      <label className="grid gap-2 text-sm">
        EPG XMLTV opcional
        <input
          name="epgUrl"
          inputMode="url"
          value={epgUrl}
          onChange={(event) => setEpgUrl(event.target.value.trim())}
          onBlur={() => setEpgUrl((value) => (value.trim() ? normalizeUrl(value) : ""))}
          placeholder="https://..."
        />
      </label>

      {message ? <p className="rounded-md border border-signal/50 bg-signal/10 p-3 text-sm text-signal">{message}</p> : null}
      {error ? <p className="rounded-md border border-red-500/50 bg-red-500/10 p-3 text-sm text-red-200">{error}</p> : null}

      <div className="flex flex-wrap gap-3">
        <button type="button" onClick={test} disabled={!canSave || Boolean(busy)} className="rounded-md border border-line px-4 py-2 hover:bg-panelSoft disabled:opacity-60">
          {busy === "test" ? "Testando..." : "Testar"}
        </button>
        <button disabled={!canSave || Boolean(busy)} className="rounded-md bg-signal px-4 py-2 font-semibold text-ink disabled:opacity-60">
          {busy === "save" ? "Salvando..." : "Salvar playlist"}
        </button>
      </div>
    </form>
  );
}

function normalizeUrl(value: string) {
  const trimmed = value.trim();
  if (!trimmed) return "";
  if (/^https?:\/\//i.test(trimmed)) return trimmed;
  return `http://${trimmed}`;
}

function parseXtreamM3u(value: string) {
  try {
    const url = new URL(normalizeUrl(value));
    const username = url.searchParams.get("username") || "";
    const password = url.searchParams.get("password") || "";
    if (!username || !password) return null;
    return {
      serverUrl: `${url.protocol}//${url.host}`,
      username,
      password
    };
  } catch {
    return null;
  }
}

function errorMessage(error: unknown, fallback: string) {
  if (typeof error === "string") return error;
  if (error && typeof error === "object") {
    const maybeZod = error as { formErrors?: string[]; fieldErrors?: Record<string, string[]> };
    const formError = maybeZod.formErrors?.[0];
    if (formError) return formError;
    const fieldError = Object.values(maybeZod.fieldErrors || {}).flat()[0];
    if (fieldError) return fieldError;
  }
  return fallback;
}
