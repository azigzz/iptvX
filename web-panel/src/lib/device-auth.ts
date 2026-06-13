import { prisma } from "@/lib/prisma";
import { verifyDeviceToken } from "@/lib/crypto";

export async function findAuthorizedDevice(deviceId: string, token: string) {
  const device = await prisma.device.findUnique({ where: { deviceId } });
  if (!device || !verifyDeviceToken(token, device.deviceTokenHash)) {
    return null;
  }
  await prisma.device.update({
    where: { id: device.id },
    data: { lastSeenAt: new Date() }
  });
  return device;
}
