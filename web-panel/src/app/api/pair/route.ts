import { NextRequest, NextResponse } from "next/server";
import { requireAdminRequest } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { rateLimit } from "@/lib/rate-limit";
import { requestIp } from "@/lib/request-ip";
import { pairSchema } from "@/lib/validators";

export async function POST(request: NextRequest) {
  const unauthorized = requireAdminRequest(request);
  if (unauthorized) return unauthorized;

  const ip = requestIp(request);
  if (!rateLimit(`pair:${ip}`, 20, 60_000).ok) {
    return NextResponse.json({ error: "Muitas tentativas. Aguarde um minuto." }, { status: 429 });
  }

  const parsed = pairSchema.safeParse(await request.json());
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const device = await prisma.device.findFirst({
    where: {
      virtualMac: parsed.data.virtualMac.trim().toUpperCase(),
      deviceId: parsed.data.deviceId.trim()
    },
    select: { id: true, virtualMac: true }
  });

  if (!device) {
    return NextResponse.json({ error: "MAC ou ID invalido" }, { status: 404 });
  }

  await prisma.device.update({
    where: { id: device.id },
    data: { pairedAt: new Date(), pairingCodeHash: null, pairingExpiresAt: null }
  });
  await prisma.syncLog.create({
    data: { deviceId: device.id, status: "OK", message: "Dispositivo pareado por MAC e ID" }
  });

  return NextResponse.json({ ok: true, deviceId: device.id, virtualMac: device.virtualMac });
}
