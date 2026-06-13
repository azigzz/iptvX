import { NextRequest, NextResponse } from "next/server";
import { requireAdminRequest } from "@/lib/auth";
import { encryptSecret } from "@/lib/crypto";
import { prisma } from "@/lib/prisma";
import { playlistPatchSchema } from "@/lib/validators";

export async function PATCH(request: NextRequest, context: { params: Promise<{ id: string }> }) {
  const unauthorized = requireAdminRequest(request);
  if (unauthorized) return unauthorized;
  const { id } = await context.params;

  const parsed = playlistPatchSchema.safeParse(await request.json());
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const payload = parsed.data;
  const playlist = await prisma.playlist.update({
    where: { id },
    data: {
      ...(payload.name ? { name: payload.name } : {}),
      ...(payload.type ? { type: payload.type } : {}),
      ...(payload.serverUrl !== undefined ? { serverUrl: payload.serverUrl || null } : {}),
      ...(payload.username !== undefined ? { usernameEncrypted: encryptSecret(payload.username) } : {}),
      ...(payload.password !== undefined ? { passwordEncrypted: encryptSecret(payload.password) } : {}),
      ...(payload.m3uUrl !== undefined ? { m3uUrlEncrypted: encryptSecret(payload.m3uUrl) } : {}),
      ...(payload.epgUrl !== undefined ? { epgUrlEncrypted: encryptSecret(payload.epgUrl) } : {}),
      ...(payload.enabled !== undefined ? { enabled: payload.enabled } : {})
    }
  });

  await prisma.syncLog.create({
    data: { deviceId: playlist.deviceId, status: "INFO", message: `Playlist ${playlist.name} atualizada` }
  });

  return NextResponse.json({ playlist });
}

export async function DELETE(request: NextRequest, context: { params: Promise<{ id: string }> }) {
  const unauthorized = requireAdminRequest(request);
  if (unauthorized) return unauthorized;
  const { id } = await context.params;

  const playlist = await prisma.playlist.delete({ where: { id } });
  await prisma.syncLog.create({
    data: { deviceId: playlist.deviceId, status: "INFO", message: `Playlist ${playlist.name} removida` }
  });
  return NextResponse.json({ ok: true });
}
