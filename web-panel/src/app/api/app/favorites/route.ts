import { NextRequest, NextResponse } from "next/server";
import { findAuthorizedDevice } from "@/lib/device-auth";
import { prisma } from "@/lib/prisma";
import { favoriteSchema } from "@/lib/validators";

export async function POST(request: NextRequest) {
  const parsed = favoriteSchema.safeParse(await request.json());
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const device = await findAuthorizedDevice(parsed.data.deviceId, parsed.data.token);
  if (!device) return NextResponse.json({ error: "Unauthorized device" }, { status: 401 });

  const playlist = await prisma.playlist.findFirst({
    where: { id: parsed.data.playlistId, deviceId: device.id }
  });
  if (!playlist) return NextResponse.json({ error: "Playlist not found" }, { status: 404 });

  const favorite = await prisma.favorite.upsert({
    where: {
      deviceId_playlistId_contentType_contentId: {
        deviceId: device.id,
        playlistId: playlist.id,
        contentType: parsed.data.contentType,
        contentId: parsed.data.contentId
      }
    },
    update: { name: parsed.data.name },
    create: {
      deviceId: device.id,
      playlistId: playlist.id,
      contentType: parsed.data.contentType,
      contentId: parsed.data.contentId,
      name: parsed.data.name
    }
  });

  return NextResponse.json({ favorite });
}
