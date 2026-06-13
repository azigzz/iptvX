import { NextRequest, NextResponse } from "next/server";
import {
  generateDeviceToken,
  generatePairingCode,
  hashDeviceToken,
  hashPairingCode,
  pairingExpiresAt,
  virtualMacFromDeviceId
} from "@/lib/crypto";
import { prisma } from "@/lib/prisma";
import { deviceRegisterSchema } from "@/lib/validators";

export async function POST(request: NextRequest) {
  const parsed = deviceRegisterSchema.safeParse(await request.json());
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const pairingCode = generatePairingCode();
  const deviceToken = generateDeviceToken();
  const virtualMac = virtualMacFromDeviceId(parsed.data.deviceId);

  const existing = await prisma.device.findUnique({
    where: { deviceId: parsed.data.deviceId },
    select: { id: true, deviceTokenHash: true, pairedAt: true }
  });

  const device = await prisma.device.upsert({
    where: { deviceId: parsed.data.deviceId },
    update: {
      ...parsed.data,
      virtualMac,
      pairingCodeHash: hashPairingCode(pairingCode),
      pairingExpiresAt: pairingExpiresAt(),
      deviceTokenHash: existing?.deviceTokenHash || hashDeviceToken(deviceToken),
      lastSeenAt: new Date()
    },
    create: {
      ...parsed.data,
      virtualMac,
      pairingCodeHash: hashPairingCode(pairingCode),
      pairingExpiresAt: pairingExpiresAt(),
      deviceTokenHash: hashDeviceToken(deviceToken),
      lastSeenAt: new Date(),
      settings: { create: {} }
    },
    select: {
      id: true,
      virtualMac: true,
      pairingExpiresAt: true,
      pairedAt: true
    }
  });

  await prisma.syncLog.create({
    data: { deviceId: device.id, status: "INFO", message: "Registro/refresh de codigo pelo app" }
  });

  return NextResponse.json({
    virtualMac: device.virtualMac,
    pairingCode,
    pairingExpiresAt: device.pairingExpiresAt,
    panelUrl: process.env.NEXT_PUBLIC_APP_PANEL_URL || "http://localhost:3000",
    paired: Boolean(device.pairedAt || existing?.pairedAt),
    deviceToken: existing?.deviceTokenHash ? undefined : deviceToken
  });
}
