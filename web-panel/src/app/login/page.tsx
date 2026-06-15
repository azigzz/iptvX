import { redirect } from "next/navigation";
import Link from "next/link";
import { isAdminLoginDisabled, isAdminSignedIn } from "@/lib/auth";
import { LoginForm } from "@/components/login-form";

export default async function LoginPage() {
  if (isAdminLoginDisabled()) redirect("/pair");
  if (await isAdminSignedIn()) redirect("/dashboard");

  return (
    <div className="mx-auto grid min-h-screen max-w-md content-center px-5">
      <div className="rounded-lg border border-line bg-panel p-6 shadow-2xl">
        <p className="text-sm uppercase tracking-[0.28em] text-signal">iptvX</p>
        <h1 className="mt-3 text-3xl font-semibold">Entrar no painel</h1>
        <p className="mt-2 text-sm text-slate-300">
          Use o e-mail e senha configurados no ambiente para gerenciar dispositivos e listas próprias.
        </p>
        <Link
          href="/download"
          className="mt-5 block rounded-md border border-line px-4 py-3 text-center text-sm font-semibold hover:bg-panelSoft"
        >
          Baixar APK para Android TV
        </Link>
        <LoginForm />
      </div>
    </div>
  );
}
