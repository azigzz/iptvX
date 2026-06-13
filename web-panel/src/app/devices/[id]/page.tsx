import { notFound } from "next/navigation";
import { requireAdminPage } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { PlaylistForm } from "@/components/playlist-form";
import { PlaylistRowActions } from "@/components/playlist-row-actions";
import { formatDateTime, onlineState } from "@/components/ui-format";

export const dynamic = "force-dynamic";

export default async function DeviceDetailPage({ params }: { params: Promise<{ id: string }> }) {
  await requireAdminPage();
  const { id } = await params;
  const device = await prisma.device.findUnique({
    where: { id },
    include: {
      playlists: { orderBy: { createdAt: "desc" } },
      settings: true,
      syncLogs: { orderBy: { createdAt: "desc" }, take: 12 }
    }
  });
  if (!device) notFound();
  const state = onlineState(device.lastSeenAt);

  return (
    <div className="mx-auto max-w-6xl px-5 py-8">
      <div className="grid gap-5 lg:grid-cols-[0.9fr_1.1fr]">
        <section className="rounded-lg border border-line bg-panel p-5">
          <p className="text-sm uppercase tracking-wide text-slate-400">Dispositivo</p>
          <h1 className="mt-2 text-3xl font-semibold">{device.virtualMac}</h1>
          <dl className="mt-5 grid gap-3 text-sm">
            <Info label="Status" value={state.label} />
            <Info label="Device ID" value={device.deviceId} />
            <Info label="Modelo" value={`${device.manufacturer || "?"} ${device.model || ""}`} />
            <Info label="Android" value={device.androidVersion || "-"} />
            <Info label="App" value={device.appVersion || "-"} />
            <Info label="Pareado em" value={formatDateTime(device.pairedAt)} />
            <Info label="Último acesso" value={formatDateTime(device.lastSeenAt)} />
            <Info label="Último sync" value={formatDateTime(device.lastSyncAt)} />
          </dl>
        </section>

        <section className="rounded-lg border border-line bg-panel p-5">
          <h2 className="text-xl font-semibold">Adicionar playlist</h2>
          <PlaylistForm deviceId={device.id} />
        </section>
      </div>

      <section className="mt-7 rounded-lg border border-line bg-panel">
        <div className="border-b border-line px-5 py-4">
          <h2 className="font-semibold">Playlists</h2>
        </div>
        <div className="divide-y divide-line">
          {device.playlists.map((playlist) => (
            <div key={playlist.id} className="grid gap-4 px-5 py-4 lg:grid-cols-[1fr_auto]">
              <div>
                <div className="flex flex-wrap items-center gap-3">
                  <p className="font-semibold">{playlist.name}</p>
                  <span className="rounded bg-panelSoft px-2 py-1 text-xs text-slate-300">{playlist.type}</span>
                  <span className={playlist.enabled ? "text-sm text-signal" : "text-sm text-warn"}>
                    {playlist.enabled ? "Ativa" : "Inativa"}
                  </span>
                </div>
                <p className="mt-1 text-sm text-slate-400">
                  Atualizada em {formatDateTime(playlist.updatedAt)}
                </p>
              </div>
              <PlaylistRowActions playlistId={playlist.id} name={playlist.name} enabled={playlist.enabled} />
            </div>
          ))}
          {device.playlists.length === 0 ? <p className="px-5 py-8 text-slate-400">Nenhuma playlist neste dispositivo.</p> : null}
        </div>
      </section>

      <section className="mt-7 rounded-lg border border-line bg-panel">
        <div className="border-b border-line px-5 py-4">
          <h2 className="font-semibold">Logs básicos</h2>
        </div>
        <div className="divide-y divide-line">
          {device.syncLogs.map((log) => (
            <div key={log.id} className="px-5 py-4">
              <div className="flex items-center justify-between gap-3">
                <span className="text-sm font-semibold text-signal">{log.status}</span>
                <span className="text-xs text-slate-500">{formatDateTime(log.createdAt)}</span>
              </div>
              <p className="mt-1 text-sm text-slate-300">{log.message}</p>
            </div>
          ))}
          {device.syncLogs.length === 0 ? <p className="px-5 py-8 text-slate-400">Sem logs.</p> : null}
        </div>
      </section>
    </div>
  );
}

function Info({ label, value }: { label: string; value: string }) {
  return (
    <div className="grid gap-1 sm:grid-cols-[120px_1fr]">
      <dt className="text-slate-400">{label}</dt>
      <dd className="break-all text-slate-100">{value}</dd>
    </div>
  );
}
