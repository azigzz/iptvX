import { readFile } from "node:fs/promises";
import path from "node:path";
import { NextResponse } from "next/server";
import { APK_FILE_NAME, externalApkUrl } from "@/lib/apk-download";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

export async function GET() {
  const externalUrl = externalApkUrl();
  if (externalUrl) {
    return NextResponse.redirect(externalUrl);
  }

  const apkPath = path.join(process.cwd(), "public", "downloads", APK_FILE_NAME);
  try {
    const apk = await readFile(apkPath);
    return new NextResponse(apk, {
      headers: {
        "Content-Type": "application/vnd.android.package-archive",
        "Content-Disposition": `attachment; filename="${APK_FILE_NAME}"`,
        "Cache-Control": "public, max-age=300"
      }
    });
  } catch {
    return new NextResponse("APK ainda nao foi publicado.", {
      status: 404,
      headers: { "Content-Type": "text/plain; charset=utf-8" }
    });
  }
}
