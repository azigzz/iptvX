import { NextRequest, NextResponse } from "next/server";
import { requireAdminRequest } from "@/lib/auth";
import { prisma } from "@/lib/prisma";

export async function GET(request: NextRequest) {
  const unauthorized = requireAdminRequest(request);
  if (unauthorized) return unauthorized;

  const devices = await prisma.device.findMany({
    orderBy: { updatedAt: "desc" },
    include: {
      _count: { select: { playlists: true } }
    }
  });

  return NextResponse.json({ devices });
}
