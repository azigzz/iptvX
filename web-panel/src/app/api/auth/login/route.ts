import { NextRequest, NextResponse } from "next/server";
import { getAdminEmail, isAdminLoginDisabled, setAdminCookie, verifyAdminPassword } from "@/lib/auth";
import { rateLimit } from "@/lib/rate-limit";
import { requestIp } from "@/lib/request-ip";

export async function POST(request: NextRequest) {
  if (isAdminLoginDisabled()) {
    return NextResponse.json({ ok: true, loginDisabled: true });
  }

  const ip = requestIp(request);
  if (!rateLimit(`login:${ip}`, 8, 60_000).ok) {
    return NextResponse.json({ error: "Muitas tentativas. Aguarde um minuto." }, { status: 429 });
  }

  const body = (await request.json()) as { email?: string; password?: string };
  if (body.email !== getAdminEmail() || !body.password || !verifyAdminPassword(body.password)) {
    return NextResponse.json({ error: "Login invalido" }, { status: 401 });
  }

  const response = NextResponse.json({ ok: true });
  setAdminCookie(response, body.email);
  return response;
}
