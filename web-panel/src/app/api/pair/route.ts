import { NextRequest, NextResponse } from "next/server";
import { requireAdminRequest } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { rateLimit } from "@/lib/rate-limit";
import { requestIp } from "@/lib/request-ip";
import { pairDeviceByCode } from "@/lib/services/pairing";
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

  const result = await pairDeviceByCode(prisma, parsed.data.virtualMac, parsed.data.pairingCode);
  if (!result.ok) {
    return NextResponse.json({ error: result.error }, { status: result.status });
  }
  return NextResponse.json({ ok: true, deviceId: result.deviceId, virtualMac: result.virtualMac });
}
