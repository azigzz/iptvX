import { requireAdminPage } from "@/lib/auth";
import { PairDeviceForm } from "@/components/pair-device-form";

export default async function PairPage() {
  await requireAdminPage();

  return (
    <div className="mx-auto max-w-3xl px-5 py-8">
      <h1 className="text-3xl font-semibold">Parear dispositivo</h1>
      <p className="mt-2 text-slate-300">
        Digite o MAC/ID virtual e o código exibidos no app. O código expira em 15 minutos.
      </p>
      <div className="mt-7 rounded-lg border border-line bg-panel p-5">
        <PairDeviceForm />
      </div>
    </div>
  );
}
