import Link from "next/link";
import { requireAdminPage } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { formatDateTime } from "@/components/ui-format";

export default async function PlaylistsPage() {
  await requireAdminPage();
  const playlists = await prisma.playlist.findMany({
    orderBy: { updatedAt: "desc" },
    include: { device: true }
  });

  return (
    <div className="mx-auto max-w-6xl px-5 py-8">
      <h1 className="text-3xl font-semibold">Playlists</h1>
      <p className="mt-2 text-slate-300">Listas cadastradas por dispositivo. Credenciais não são exibidas no painel.</p>

      <div className="mt-7 overflow-hidden rounded-lg border border-line bg-panel">
        <div className="grid grid-cols-[1fr_0.7fr_1fr_0.7fr] gap-3 border-b border-line px-5 py-3 text-xs uppercase tracking-wide text-slate-400">
          <span>Nome</span>
          <span>Tipo</span>
          <span>Dispositivo</span>
          <span>Atualizada</span>
        </div>
        <div className="divide-y divide-line">
          {playlists.map((playlist) => (
            <Link
              href={`/devices/${playlist.deviceId}`}
              key={playlist.id}
              className="grid grid-cols-[1fr_0.7fr_1fr_0.7fr] gap-3 px-5 py-4 text-sm hover:bg-panelSoft"
            >
              <span className="font-medium">{playlist.name}</span>
              <span>{playlist.type}</span>
              <span className="text-slate-300">{playlist.device.virtualMac}</span>
              <span className="text-slate-400">{formatDateTime(playlist.updatedAt)}</span>
            </Link>
          ))}
          {playlists.length === 0 ? <p className="px-5 py-8 text-slate-400">Nenhuma playlist cadastrada.</p> : null}
        </div>
      </div>
    </div>
  );
}
