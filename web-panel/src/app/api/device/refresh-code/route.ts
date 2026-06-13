import { NextRequest, NextResponse } from "next/server";
import { findAuthorizedDevice } from "@/lib/device-auth";
import { generatePairingCode, hashPairingCode, pairingExpiresAt } from "@/lib/crypto";
import { prisma } from "@/lib/prisma";
import { deviceAuthSchema } from "@/lib/validators";

export async function POST(request: NextRequest) {
  const parsed = deviceAuthSchema.safeParse(await request.json());
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const device = await findAuthorizedDevice(parsed.data.deviceId, parsed.data.token);
  if (!device) return NextResponse.json({ error: "Unauthorized device" }, { status: 401 });

  const pairingCode = generatePairingCode();
  const updated = await prisma.device.update({
    where: { id: device.id },
    data: {
      pairingCodeHash: hashPairingCode(pairingCode),
      pairingExpiresAt: pairingExpiresAt()
    },
    select: { virtualMac: true, pairingExpiresAt: true }
  });

  await prisma.syncLog.create({
    data: { deviceId: device.id, status: "INFO", message: "Novo codigo de pareamento gerado" }
  });

  return NextResponse.json({
    virtualMac: updated.virtualMac,
    pairingCode,
    pairingExpiresAt: updated.pairingExpiresAt
  });
}
