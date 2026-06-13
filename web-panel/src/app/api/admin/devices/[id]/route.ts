import { NextRequest, NextResponse } from "next/server";
import { requireAdminRequest } from "@/lib/auth";
import { prisma } from "@/lib/prisma";

export async function GET(request: NextRequest, context: { params: Promise<{ id: string }> }) {
  const unauthorized = requireAdminRequest(request);
  if (unauthorized) return unauthorized;
  const { id } = await context.params;

  const device = await prisma.device.findUnique({
    where: { id },
    include: {
      playlists: { orderBy: { createdAt: "desc" } },
      settings: true,
      syncLogs: { orderBy: { createdAt: "desc" }, take: 20 }
    }
  });

  if (!device) return NextResponse.json({ error: "Not found" }, { status: 404 });
  return NextResponse.json({ device });
}
