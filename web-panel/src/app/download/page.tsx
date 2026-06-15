import Link from "next/link";
import { apkDownloadPath, externalApkUrl } from "@/lib/apk-download";

export default function DownloadPage() {
  const externalUrl = externalApkUrl();

  return (
    <div className="mx-auto grid min-h-screen max-w-3xl content-center px-5 py-10">
      <section className="rounded-2xl border border-line bg-panel p-7 shadow-2xl">
        <p className="text-sm uppercase tracking-[0.28em] text-signal">iptvX Android TV</p>
        <h1 className="mt-4 text-4xl font-semibold">Baixar aplicativo</h1>
        <p className="mt-3 max-w-2xl text-slate-300">
          Abra esta pagina no navegador do aparelho Android TV ou TV Box e toque no botao abaixo para baixar o APK.
        </p>

        <div className="mt-7 flex flex-wrap gap-3">
          <a
            href={apkDownloadPath()}
            download
            className="rounded-md bg-signal px-5 py-3 text-base font-semibold text-ink"
          >
            Baixar APK
          </a>
          <Link href="/pair" className="rounded-md border border-line px-5 py-3 text-base font-semibold hover:bg-panelSoft">
            Parear aparelho
          </Link>
        </div>

        <div className="mt-7 rounded-lg border border-line bg-panelSoft p-4 text-sm text-slate-300">
          <p className="font-semibold text-white">Dica para publicar uma nova versao</p>
          <p className="mt-2">
            Substitua o arquivo em <code className="text-signal">web-panel/public/downloads/iptvx.apk</code> e publique o site de novo.
          </p>
          {externalUrl ? (
            <p className="mt-2 text-slate-400">Download externo ativo: {externalUrl}</p>
          ) : null}
        </div>
      </section>
    </div>
  );
}
