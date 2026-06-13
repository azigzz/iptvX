import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";
import { redirect } from "next/navigation";
import { constantTimeTextEqual, createSessionSignature, sha256Hex } from "@/lib/crypto";

const SESSION_COOKIE = "iptvx_admin";
const SESSION_TTL_SECONDS = 60 * 60 * 8;

type SessionPayload = {
  email: string;
  exp: number;
};

export function getAdminEmail() {
  return process.env.ADMIN_EMAIL || "admin@example.com";
}

export function isAdminLoginDisabled() {
  return process.env.DISABLE_ADMIN_LOGIN === "true";
}

function encodePayload(payload: SessionPayload) {
  return Buffer.from(JSON.stringify(payload), "utf8").toString("base64url");
}

function decodePayload(raw: string): SessionPayload | null {
  try {
    return JSON.parse(Buffer.from(raw, "base64url").toString("utf8")) as SessionPayload;
  } catch {
    return null;
  }
}

export function createAdminSession(email: string) {
  const payload = encodePayload({
    email,
    exp: Math.floor(Date.now() / 1000) + SESSION_TTL_SECONDS
  });
  return `${payload}.${createSessionSignature(payload)}`;
}

export function verifyAdminSession(token?: string | null) {
  if (!token) return null;
  const [payloadRaw, signature] = token.split(".");
  if (!payloadRaw || !signature) return null;
  const expected = createSessionSignature(payloadRaw);
  if (!constantTimeTextEqual(signature, expected)) return null;
  const payload = decodePayload(payloadRaw);
  if (!payload || payload.exp < Math.floor(Date.now() / 1000)) return null;
  if (payload.email !== getAdminEmail()) return null;
  return payload;
}

export async function isAdminSignedIn() {
  if (isAdminLoginDisabled()) return true;
  const cookieStore = await cookies();
  return Boolean(verifyAdminSession(cookieStore.get(SESSION_COOKIE)?.value));
}

export async function requireAdminPage() {
  if (isAdminLoginDisabled()) return;
  if (!(await isAdminSignedIn())) {
    redirect("/login");
  }
}

export function requireAdminRequest(request: NextRequest) {
  if (isAdminLoginDisabled()) return null;
  const session = verifyAdminSession(request.cookies.get(SESSION_COOKIE)?.value);
  if (!session) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
  return null;
}

export function setAdminCookie(response: NextResponse, email: string) {
  response.cookies.set(SESSION_COOKIE, createAdminSession(email), {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    maxAge: SESSION_TTL_SECONDS,
    path: "/"
  });
}

export function clearAdminCookie(response: NextResponse) {
  response.cookies.set(SESSION_COOKIE, "", {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    maxAge: 0,
    path: "/"
  });
}

export function verifyAdminPassword(password: string) {
  const configuredHash = process.env.ADMIN_PASSWORD_HASH?.trim();
  if (configuredHash) {
    if (configuredHash.startsWith("sha256:")) {
      return constantTimeTextEqual(configuredHash, `sha256:${sha256Hex(password)}`);
    }
    return constantTimeTextEqual(configuredHash, password);
  }

  const configuredPassword = process.env.ADMIN_PASSWORD || "change-me-now";
  return constantTimeTextEqual(configuredPassword, password);
}
