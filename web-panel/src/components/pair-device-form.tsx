"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";

export function PairDeviceForm() {
  const router = useRouter();
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setMessage("");
    setError("");
    const form = new FormData(event.currentTarget);
    const response = await fetch("/api/pair", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        virtualMac: form.get("virtualMac"),
        deviceId: form.get("deviceId")
      })
    });
    const payload = await response.json().catch(() => ({}));
    setLoading(false);
    if (!response.ok) {
      setError(payload.error || "Nao foi possivel parear");
      return;
    }
    setMessage("Dispositivo pareado com sucesso.");
    router.refresh();
  }

  return (
    <form onSubmit={submit} className="grid gap-4">
      <label className="grid gap-2 text-sm">
        MAC
        <input name="virtualMac" placeholder="00:1A:79:XX:XX:XX" required />
      </label>
      <label className="grid gap-2 text-sm">
        ID
        <input name="deviceId" placeholder="ID exibido no app" required />
      </label>
      {message ? <p className="rounded-md border border-signal/50 bg-signal/10 p-3 text-sm text-signal">{message}</p> : null}
      {error ? <p className="rounded-md border border-red-500/50 bg-red-500/10 p-3 text-sm text-red-200">{error}</p> : null}
      <button className="rounded-md bg-signal px-4 py-3 font-semibold text-ink disabled:opacity-60" disabled={loading}>
        {loading ? "Pareando..." : "Confirmar"}
      </button>
    </form>
  );
}
