export const APK_FILE_NAME = "iptvx.apk";

export function apkDownloadPath() {
  return "/api/download/apk";
}

export function externalApkUrl() {
  return process.env.APK_DOWNLOAD_URL?.trim() || process.env.NEXT_PUBLIC_APK_DOWNLOAD_URL?.trim() || "";
}
