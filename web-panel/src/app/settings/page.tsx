import { requireAdminPage } from "@/lib/auth";

export default async function SettingsPage() {
  await requireAdminPage();
  const panelUrl = process.env.NEXT_PUBLIC_APP_PANEL_URL || "http://localhost:3000";
  const hasDb = Boolean(process.env.DATABASE_URL);
  const hasSecret = Boolean(process.env.SECRET_KEY);
  const hasDeviceSecret = Boolean(process.env.DEVICE_TOKEN_SECRET);

  return (
    <div className="mx-auto max-w-4xl px-5 py-8">
      <h1 className="text-3xl font-semibold">Configurações</h1>
      <div className="mt-7 rounded-lg border border-line bg-panel p-5">
        <dl className="grid gap-4">
          <Info label="URL pública do painel" value={panelUrl} />
          <Info label="DATABASE_URL" value={hasDb ? "Configurada" : "Ausente"} />
          <Info label="SECRET_KEY" value={hasSecret ? "Configurada" : "Ausente"} />
          <Info label="DEVICE_TOKEN_SECRET" value={hasDeviceSecret ? "Configurada" : "Ausente"} />
        </dl>
      </div>
      <div className="mt-5 rounded-lg border border-line bg-panel p-5 text-sm text-slate-300">
        <p>
          Para produção, use PostgreSQL gerenciado, senha admin forte, SECRET_KEY longa e HTTPS. O app usa esta URL para
          exibir o endereço de pareamento na TV.
        </p>
      </div>
    </div>
  );
}

function Info({ label, value }: { label: string; value: string }) {
  return (
    <div className="grid gap-1 sm:grid-cols-[220px_1fr]">
      <dt className="text-slate-400">{label}</dt>
      <dd className="break-all font-medium">{value}</dd>
    </div>
  );
}
