import { NextRequest, NextResponse } from "next/server";
import { requireAdminRequest } from "@/lib/auth";
import { testPlaylist } from "@/lib/playlist-test";
import { playlistPayloadSchema } from "@/lib/validators";

export async function POST(request: NextRequest) {
  const unauthorized = requireAdminRequest(request);
  if (unauthorized) return unauthorized;

  const parsed = playlistPayloadSchema.safeParse(await request.json());
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }

  const result = await testPlaylist(parsed.data);
  return NextResponse.json(result, { status: result.ok ? 200 : 400 });
}
