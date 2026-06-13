"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";

export function PlaylistForm({ deviceId }: { deviceId: string }) {
  const router = useRouter();
  const [type, setType] = useState<"M3U" | "XTREAM">("M3U");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setMessage("");
    const form = new FormData(event.currentTarget);
    const payload = {
      name: form.get("name"),
      type,
      serverUrl: form.get("serverUrl") || undefined,
      username: form.get("username") || undefined,
      password: form.get("password") || undefined,
      m3uUrl: form.get("m3uUrl") || undefined,
      epgUrl: form.get("epgUrl") || undefined,
      enabled: true
    };
    const response = await fetch(`/api/admin/devices/${deviceId}/playlists`, {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify(payload)
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      setError(data.error?.formErrors?.[0] || data.error || "Nao foi possivel salvar");
      return;
    }
    setMessage("Playlist salva. O app recebera no proximo sync.");
    event.currentTarget.reset();
    router.refresh();
  }

  async function test(event: FormEvent<HTMLButtonElement>) {
    event.preventDefault();
    const form = event.currentTarget.form;
    if (!form) return;
    setError("");
    setMessage("Testando...");
    const formData = new FormData(form);
    const response = await fetch("/api/admin/playlists/test", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        name: formData.get("name") || "Teste",
        type,
        serverUrl: formData.get("serverUrl") || undefined,
        username: formData.get("username") || undefined,
        password: formData.get("password") || undefined,
        m3uUrl: formData.get("m3uUrl") || undefined,
        epgUrl: formData.get("epgUrl") || undefined
      })
    });
    const payload = await response.json().catch(() => ({}));
    if (!response.ok) {
      setMessage("");
      setError(payload.message || payload.error || "Teste falhou");
      return;
    }
    setMessage(payload.message || "Teste OK");
  }

  return (
    <form onSubmit={submit} className="mt-5 grid gap-4">
      <label className="grid gap-2 text-sm">
        Nome da lista
        <input name="name" placeholder="Minha lista" required />
      </label>
      <label className="grid gap-2 text-sm">
        Tipo
        <select value={type} onChange={(event) => setType(event.target.value as "M3U" | "XTREAM")}>
          <option value="M3U">M3U/M3U8</option>
          <option value="XTREAM">Xtream Codes</option>
        </select>
      </label>
      {type === "M3U" ? (
        <label className="grid gap-2 text-sm">
          URL M3U
          <input name="m3uUrl" type="url" placeholder="https://..." />
        </label>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          <label className="grid gap-2 text-sm sm:col-span-2">
            Server URL
            <input name="serverUrl" type="url" placeholder="https://servidor.example" />
          </label>
          <label className="grid gap-2 text-sm">
            Usuário
            <input name="username" autoComplete="off" />
          </label>
          <label className="grid gap-2 text-sm">
            Senha
            <input name="password" type="password" autoComplete="off" />
          </label>
        </div>
      )}
      <label className="grid gap-2 text-sm">
        EPG XMLTV opcional
        <input name="epgUrl" type="url" placeholder="https://..." />
      </label>
      {message ? <p className="rounded-md border border-signal/50 bg-signal/10 p-3 text-sm text-signal">{message}</p> : null}
      {error ? <p className="rounded-md border border-red-500/50 bg-red-500/10 p-3 text-sm text-red-200">{error}</p> : null}
      <div className="flex flex-wrap gap-3">
        <button type="button" onClick={test} className="rounded-md border border-line px-4 py-2 hover:bg-panelSoft">
          Testar
        </button>
        <button className="rounded-md bg-signal px-4 py-2 font-semibold text-ink">Salvar playlist</button>
      </div>
    </form>
  );
}
