import { NextRequest, NextResponse } from "next/server";
import { requireAdminRequest } from "@/lib/auth";
import { encryptSecret } from "@/lib/crypto";
import { prisma } from "@/lib/prisma";
import { playlistPayloadSchema } from "@/lib/validators";

export async function POST(request: NextRequest, context: { params: Promise<{ id: string }> }) {
  const unauthorized = requireAdminRequest(request);
  if (unauthorized) return unauthorized;
  const { id } = await context.params;

  const parsed = playlistPayloadSchema.safeParse(await request.json());
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const device = await prisma.device.findUnique({ where: { id } });
  if (!device) return NextResponse.json({ error: "Device not found" }, { status: 404 });

  const payload = parsed.data;
  const playlist = await prisma.playlist.create({
    data: {
      deviceId: device.id,
      name: payload.name,
      type: payload.type,
      serverUrl: payload.type === "XTREAM" ? payload.serverUrl || null : null,
      usernameEncrypted: encryptSecret(payload.username),
      passwordEncrypted: encryptSecret(payload.password),
      m3uUrlEncrypted: encryptSecret(payload.m3uUrl),
      epgUrlEncrypted: encryptSecret(payload.epgUrl),
      enabled: payload.enabled ?? true
    }
  });

  await prisma.syncLog.create({
    data: { deviceId: device.id, status: "INFO", message: `Playlist ${playlist.name} criada` }
  });

  return NextResponse.json({ playlist });
}
