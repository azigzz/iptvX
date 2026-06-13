import Link from "next/link";
import { requireAdminPage } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { formatDateTime, onlineState } from "@/components/ui-format";

export default async function DevicesPage() {
  await requireAdminPage();
  const devices = await prisma.device.findMany({
    orderBy: { updatedAt: "desc" },
    include: { _count: { select: { playlists: true } } }
  });

  return (
    <div className="mx-auto max-w-6xl px-5 py-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-semibold">Dispositivos</h1>
          <p className="mt-2 text-slate-300">Status, último acesso e playlists por aparelho.</p>
        </div>
        <Link className="rounded-md bg-signal px-4 py-2 font-semibold text-ink" href="/pair">
          Adicionar por código
        </Link>
      </div>
      <div className="mt-7 overflow-hidden rounded-lg border border-line bg-panel">
        <div className="grid grid-cols-[1.2fr_0.8fr_0.8fr_0.7fr] gap-3 border-b border-line px-5 py-3 text-xs uppercase tracking-wide text-slate-400">
          <span>Dispositivo</span>
          <span>Status</span>
          <span>Último sync</span>
          <span>Listas</span>
        </div>
        <div className="divide-y divide-line">
          {devices.map((device) => {
            const state = onlineState(device.lastSeenAt);
            return (
              <Link
                href={`/devices/${device.id}`}
                key={device.id}
                className="grid grid-cols-[1.2fr_0.8fr_0.8fr_0.7fr] gap-3 px-5 py-4 text-sm hover:bg-panelSoft"
              >
                <span>
                  <strong className="block text-base">{device.virtualMac}</strong>
                  <span className="text-slate-400">
                    {device.manufacturer || "?"} {device.model || ""} · Android {device.androidVersion || "?"}
                  </span>
                </span>
                <span className={state.online ? "text-signal" : "text-slate-400"}>{state.label}</span>
                <span className="text-slate-300">{formatDateTime(device.lastSyncAt)}</span>
                <span>{device._count.playlists}</span>
              </Link>
            );
          })}
          {devices.length === 0 ? <p className="px-5 py-8 text-slate-400">Nenhum dispositivo registrado pelo app.</p> : null}
        </div>
      </div>
    </div>
  );
}
