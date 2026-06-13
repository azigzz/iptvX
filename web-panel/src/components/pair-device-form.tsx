"use client";

import { FormEvent, useRef, useState } from "react";
import { useRouter } from "next/navigation";

export function PairDeviceForm() {
  const router = useRouter();
  const idInputRef = useRef<HTMLInputElement>(null);
  const [virtualMac, setVirtualMac] = useState("");
  const [deviceId, setDeviceId] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const canSubmit = virtualMac.length === 17 && /^\d{1,5}$/.test(deviceId);

  function updateMac(value: string) {
    const formatted = formatMac(value);
    setVirtualMac(formatted);
    if (formatted.length === 17) {
      window.setTimeout(() => idInputRef.current?.focus(), 0);
    }
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!canSubmit) return;
    setLoading(true);
    setMessage("");
    setError("");
    const response = await fetch("/api/pair", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        virtualMac: virtualMac.trim().toUpperCase(),
        deviceId: deviceId.trim()
      })
    });
    const payload = await response.json().catch(() => ({}));
    setLoading(false);
    if (!response.ok) {
      setError(errorMessage(payload.error, "Nao foi possivel parear"));
      return;
    }
    setMessage("Pareado. Abrindo dispositivo...");
    router.push(`/devices/${payload.deviceId}#add-playlist`);
    router.refresh();
  }

  return (
    <form onSubmit={submit} className="grid gap-4">
      <label className="grid gap-2 text-sm">
        MAC
        <input
          name="virtualMac"
          value={virtualMac}
          onChange={(event) => updateMac(event.target.value)}
          placeholder="00:1A:79:XX:XX:XX"
          autoCapitalize="characters"
          autoComplete="off"
          maxLength={17}
          required
        />
        <span className="text-xs text-slate-500">{virtualMac.length}/17 caracteres</span>
      </label>
      <label className="grid gap-2 text-sm">
        ID
        <input
          ref={idInputRef}
          name="deviceId"
          value={deviceId}
          onChange={(event) => setDeviceId(event.target.value.replace(/\D/g, "").slice(0, 5))}
          placeholder="12345"
          inputMode="numeric"
          pattern="[0-9]{1,5}"
          maxLength={5}
          autoComplete="off"
          required
        />
        <span className="text-xs text-slate-500">Somente numeros, ate 5 digitos.</span>
      </label>
      {message ? <p className="rounded-md border border-signal/50 bg-signal/10 p-3 text-sm text-signal">{message}</p> : null}
      {error ? <p className="rounded-md border border-red-500/50 bg-red-500/10 p-3 text-sm text-red-200">{error}</p> : null}
      <button className="rounded-md bg-signal px-4 py-3 font-semibold text-ink disabled:opacity-60" disabled={loading || !canSubmit}>
        {loading ? "Pareando..." : "Confirmar"}
      </button>
    </form>
  );
}

function formatMac(value: string) {
  return value
    .toUpperCase()
    .replace(/[^0-9A-F]/g, "")
    .slice(0, 12)
    .replace(/(.{2})(?=.)/g, "$1:");
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
