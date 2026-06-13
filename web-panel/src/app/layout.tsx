import type { Metadata } from "next";
import Link from "next/link";
import "./globals.css";
import { isAdminSignedIn } from "@/lib/auth";
import { LogoutButton } from "@/components/logout-button";

export const metadata: Metadata = {
  title: "iptvX Panel",
  description: "Painel de pareamento e gerenciamento de listas autorizadas"
};

export default async function RootLayout({ children }: { children: React.ReactNode }) {
  const signedIn = await isAdminSignedIn();

  return (
    <html lang="pt-BR">
      <body>
        <div className="min-h-screen bg-ink">
          {signedIn ? (
            <header className="border-b border-line bg-panel">
              <nav className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-5 py-4">
                <Link href="/dashboard" className="text-lg font-semibold tracking-wide text-signal">
                  iptvX Panel
                </Link>
                <div className="flex flex-wrap items-center gap-3 text-sm text-slate-200">
                  <Link href="/dashboard" className="hover:text-signal">Dashboard</Link>
                  <Link href="/devices" className="hover:text-signal">Dispositivos</Link>
                  <Link href="/pair" className="hover:text-signal">Parear</Link>
                  <Link href="/playlists" className="hover:text-signal">Playlists</Link>
                  <Link href="/settings" className="hover:text-signal">Configurações</Link>
                  <LogoutButton />
                </div>
              </nav>
            </header>
          ) : null}
          <main>{children}</main>
        </div>
      </body>
    </html>
  );
}
