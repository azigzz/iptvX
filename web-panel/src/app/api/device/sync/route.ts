import { NextRequest, NextResponse } from "next/server";
import { decryptSecret } from "@/lib/crypto";
import { findAuthorizedDevice } from "@/lib/device-auth";
import { prisma } from "@/lib/prisma";
import { deviceAuthSchema } from "@/lib/validators";

export async function POST(request: NextRequest) {
  const parsed = deviceAuthSchema.safeParse(await request.json());
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const device = await findAuthorizedDevice(parsed.data.deviceId, parsed.data.token);
  if (!device) return NextResponse.json({ error: "Unauthorized device" }, { status: 401 });

  const [playlists, settings] = await Promise.all([
    prisma.playlist.findMany({
      where: { deviceId: device.id, enabled: true },
      orderBy: { createdAt: "desc" }
    }),
    prisma.deviceSetting.upsert({
      where: { deviceId: device.id },
      update: {},
      create: { deviceId: device.id }
    })
  ]);

  await prisma.device.update({
    where: { id: device.id },
    data: { lastSyncAt: new Date(), lastSeenAt: new Date() }
  });
  await prisma.syncLog.create({
    data: { deviceId: device.id, status: "OK", message: `Sync retornou ${playlists.length} playlists` }
  });

  return NextResponse.json({
    device: {
      id: device.id,
      virtualMac: device.virtualMac,
      pairedAt: device.pairedAt
    },
    settings,
    playlists: playlists.map((playlist) => ({
      id: playlist.id,
      name: playlist.name,
      type: playlist.type,
      serverUrl: playlist.serverUrl,
      username: decryptSecret(playlist.usernameEncrypted),
      password: decryptSecret(playlist.passwordEncrypted),
      m3uUrl: decryptSecret(playlist.m3uUrlEncrypted),
      epgUrl: decryptSecret(playlist.epgUrlEncrypted),
      enabled: playlist.enabled,
      updatedAt: playlist.updatedAt
    }))
  });
}
