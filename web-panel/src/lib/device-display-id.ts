import { createHash } from "node:crypto";

export function shortNumericDeviceId(deviceId: string) {
  const digest = createHash("sha256").update(deviceId).digest();
  return ((digest.readUInt32BE(0) % 90_000) + 10_000).toString();
}
