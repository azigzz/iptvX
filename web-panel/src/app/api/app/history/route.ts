import { NextRequest, NextResponse } from "next/server";
import { findAuthorizedDevice } from "@/lib/device-auth";
import { prisma } from "@/lib/prisma";
import { historySchema } from "@/lib/validators";

export async function POST(request: NextRequest) {
  const parsed = historySchema.safeParse(await request.json());
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const device = await findAuthorizedDevice(parsed.data.deviceId, parsed.data.token);
  if (!device) return NextResponse.json({ error: "Unauthorized device" }, { status: 401 });

  const playlist = await prisma.playlist.findFirst({
    where: { id: parsed.data.playlistId, deviceId: device.id }
  });
  if (!playlist) return NextResponse.json({ error: "Playlist not found" }, { status: 404 });

  const history = await prisma.watchHistory.upsert({
    where: {
      deviceId_playlistId_contentType_contentId: {
        deviceId: device.id,
        playlistId: playlist.id,
        contentType: parsed.data.contentType,
        contentId: parsed.data.contentId
      }
    },
    update: {
      positionMs: parsed.data.positionMs,
      durationMs: parsed.data.durationMs
    },
    create: {
      deviceId: device.id,
      playlistId: playlist.id,
      contentType: parsed.data.contentType,
      contentId: parsed.data.contentId,
      positionMs: parsed.data.positionMs,
      durationMs: parsed.data.durationMs
    }
  });

  return NextResponse.json({ history });
}
