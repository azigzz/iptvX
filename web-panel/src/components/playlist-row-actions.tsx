"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

export function PlaylistRowActions({
  playlistId,
  name,
  enabled
}: {
  playlistId: string;
  name: string;
  enabled: boolean;
}) {
  const router = useRouter();
  const [busy, setBusy] = useState(false);

  async function patch(payload: Record<string, unknown>) {
    setBusy(true);
    await fetch(`/api/admin/playlists/${playlistId}`, {
      method: "PATCH",
      headers: { "content-type": "application/json" },
      body: JSON.stringify(payload)
    });
    setBusy(false);
    router.refresh();
  }

  async function remove() {
    if (!confirm(`Remover playlist "${name}"?`)) return;
    setBusy(true);
    await fetch(`/api/admin/playlists/${playlistId}`, { method: "DELETE" });
    setBusy(false);
    router.refresh();
  }

  return (
    <div className="flex flex-wrap items-center gap-2">
      <button
        disabled={busy}
        onClick={() => patch({ enabled: !enabled })}
        className="rounded-md border border-line px-3 py-2 text-sm hover:bg-panelSoft disabled:opacity-60"
      >
        {enabled ? "Desativar" : "Ativar"}
      </button>
      <button
        disabled={busy}
        onClick={async () => {
          const nextName = prompt("Novo nome da playlist", name);
          if (nextName) await patch({ name: nextName });
        }}
        className="rounded-md border border-line px-3 py-2 text-sm hover:bg-panelSoft disabled:opacity-60"
      >
        Editar nome
      </button>
      <button
        disabled={busy}
        onClick={remove}
        className="rounded-md border border-red-500/60 px-3 py-2 text-sm text-red-200 hover:bg-red-500/10 disabled:opacity-60"
      >
        Remover
      </button>
    </div>
  );
}
