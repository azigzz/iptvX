"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";

export function LoginForm() {
  const router = useRouter();
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError("");
    const form = new FormData(event.currentTarget);
    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({
        email: form.get("email"),
        password: form.get("password")
      })
    });
    setLoading(false);
    if (!response.ok) {
      const payload = await response.json().catch(() => ({ error: "Falha no login" }));
      setError(payload.error || "Falha no login");
      return;
    }
    router.push("/dashboard");
    router.refresh();
  }

  return (
    <form onSubmit={submit} className="mt-6 grid gap-4">
      <label className="grid gap-2 text-sm">
        E-mail
        <input name="email" type="email" autoComplete="username" required />
      </label>
      <label className="grid gap-2 text-sm">
        Senha
        <input name="password" type="password" autoComplete="current-password" required />
      </label>
      {error ? <p className="rounded-md border border-red-500/50 bg-red-500/10 p-3 text-sm text-red-200">{error}</p> : null}
      <button className="rounded-md bg-signal px-4 py-3 font-semibold text-ink disabled:opacity-60" disabled={loading}>
        {loading ? "Entrando..." : "Entrar"}
      </button>
    </form>
  );
}
