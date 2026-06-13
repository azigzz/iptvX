import Link from "next/link";
import { requireAdminPage } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { StatCard } from "@/components/stat-card";
import { formatDateTime, onlineState } from "@/components/ui-format";

export default async function DashboardPage() {
  await requireAdminPage();
  const [devices, playlists, logs] = await Promise.all([
    prisma.device.findMany({ orderBy: { updatedAt: "desc" }, take: 5 }),
    prisma.playlist.count(),
    prisma.syncLog.findMany({ orderBy: { createdAt: "desc" }, take: 8, include: { device: true } })
  ]);
  const paired = devices.filter((device) => device.pairedAt).length;
  const online = devices.filter((device) => onlineState(device.lastSeenAt).online).length;

  return (
    <div className="mx-auto max-w-6xl px-5 py-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-semibold">Dashboard</h1>
          <p className="mt-2 text-slate-300">Controle de dispositivos, pareamento e sincronização remota.</p>
        </div>
        <Link className="rounded-md bg-signal px-4 py-2 font-semibold text-ink" href="/pair">
          Parear dispositivo
        </Link>
      </div>

      <section className="mt-7 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Dispositivos recentes" value={devices.length.toString()} />
        <StatCard label="Pareados" value={paired.toString()} />
        <StatCard label="Online agora" value={online.toString()} />
        <StatCard label="Playlists" value={playlists.toString()} />
      </section>

      <section className="mt-8 grid gap-5 lg:grid-cols-[1.2fr_0.8fr]">
        <div className="rounded-lg border border-line bg-panel">
          <div className="border-b border-line px-5 py-4">
            <h2 className="font-semibold">Dispositivos recentes</h2>
          </div>
          <div className="divide-y divide-line">
            {devices.map((device) => (
              <Link
                key={device.id}
                href={`/devices/${device.id}`}
                className="grid gap-2 px-5 py-4 hover:bg-panelSoft sm:grid-cols-[1fr_auto]"
              >
                <div>
                  <p className="font-medium">{device.virtualMac}</p>
                  <p className="text-sm text-slate-400">
                    {device.manufacturer || "Fabricante desconhecido"} {device.model || ""}
                  </p>
                </div>
                <p className="text-sm text-slate-400">{formatDateTime(device.lastSeenAt)}</p>
              </Link>
            ))}
            {devices.length === 0 ? <p className="px-5 py-8 text-slate-400">Nenhum dispositivo registrado.</p> : null}
          </div>
        </div>

        <div className="rounded-lg border border-line bg-panel">
          <div className="border-b border-line px-5 py-4">
            <h2 className="font-semibold">Logs de sync</h2>
          </div>
          <div className="divide-y divide-line">
            {logs.map((log) => (
              <div key={log.id} className="px-5 py-4">
                <div className="flex items-center justify-between gap-3">
                  <span className="text-sm font-semibold text-signal">{log.status}</span>
                  <span className="text-xs text-slate-500">{formatDateTime(log.createdAt)}</span>
                </div>
                <p className="mt-1 text-sm text-slate-300">{log.message}</p>
                <p className="mt-1 text-xs text-slate-500">{log.device.virtualMac}</p>
              </div>
            ))}
            {logs.length === 0 ? <p className="px-5 py-8 text-slate-400">Sem logs ainda.</p> : null}
          </div>
        </div>
      </section>
    </div>
  );
}
