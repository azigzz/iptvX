import { NextRequest, NextResponse } from "next/server";
import { findAuthorizedDevice } from "@/lib/device-auth";
import { deviceAuthSchema } from "@/lib/validators";

export async function POST(request: NextRequest) {
  const parsed = deviceAuthSchema.safeParse(await request.json());
  if (!parsed.success) {
    return NextResponse.json({ error: parsed.error.flatten() }, { status: 400 });
  }
  const device = await findAuthorizedDevice(parsed.data.deviceId, parsed.data.token);
  if (!device) return NextResponse.json({ error: "Unauthorized device" }, { status: 401 });

  return NextResponse.json({
    programmes: [],
    message: "EPG pode ser baixado pelo app diretamente a partir da epgUrl de cada playlist."
  });
}
