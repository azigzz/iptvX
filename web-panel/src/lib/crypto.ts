import {
  createCipheriv,
  createDecipheriv,
  createHash,
  createHmac,
  randomBytes,
  timingSafeEqual
} from "node:crypto";

const PAIRING_TTL_MINUTES = 15;
const PAIRING_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

function envSecret(name: string, fallback: string) {
  return process.env[name] || fallback;
}

function secretKey() {
  return createHash("sha256")
    .update(envSecret("SECRET_KEY", "dev-secret-key-change-me"))
    .digest();
}

function tokenSecret() {
  return envSecret("DEVICE_TOKEN_SECRET", envSecret("SECRET_KEY", "dev-token-secret-change-me"));
}

export function sha256Hex(value: string) {
  return createHash("sha256").update(value).digest("hex");
}

export function encryptSecret(value?: string | null) {
  if (!value) return null;
  const iv = randomBytes(12);
  const cipher = createCipheriv("aes-256-gcm", secretKey(), iv);
  const encrypted = Buffer.concat([cipher.update(value, "utf8"), cipher.final()]);
  const tag = cipher.getAuthTag();
  return [
    "v1",
    iv.toString("base64url"),
    tag.toString("base64url"),
    encrypted.toString("base64url")
  ].join(":");
}

export function decryptSecret(value?: string | null) {
  if (!value) return null;
  const [version, ivRaw, tagRaw, encryptedRaw] = value.split(":");
  if (version !== "v1" || !ivRaw || !tagRaw || !encryptedRaw) {
    throw new Error("Invalid encrypted secret format");
  }
  const decipher = createDecipheriv(
    "aes-256-gcm",
    secretKey(),
    Buffer.from(ivRaw, "base64url")
  );
  decipher.setAuthTag(Buffer.from(tagRaw, "base64url"));
  const decrypted = Buffer.concat([
    decipher.update(Buffer.from(encryptedRaw, "base64url")),
    decipher.final()
  ]);
  return decrypted.toString("utf8");
}

export function generatePairingCode(length = 6) {
  let code = "";
  for (let index = 0; index < length; index += 1) {
    code += PAIRING_ALPHABET[randomBytes(1)[0] % PAIRING_ALPHABET.length];
  }
  return code;
}

export function pairingExpiresAt() {
  return new Date(Date.now() + PAIRING_TTL_MINUTES * 60 * 1000);
}

export function normalizePairingCode(code: string) {
  return code.trim().toUpperCase().replace(/[^A-Z0-9]/g, "");
}

export function hashPairingCode(code: string) {
  return createHmac("sha256", envSecret("SECRET_KEY", "dev-secret-key-change-me"))
    .update(normalizePairingCode(code))
    .digest("hex");
}

export function verifyPairingCode(code: string, expectedHash?: string | null) {
  if (!expectedHash) return false;
  const actual = Buffer.from(hashPairingCode(code), "hex");
  const expected = Buffer.from(expectedHash, "hex");
  return actual.length === expected.length && timingSafeEqual(actual, expected);
}

export function generateDeviceToken() {
  return randomBytes(32).toString("base64url");
}

export function hashDeviceToken(token: string) {
  return createHmac("sha256", tokenSecret()).update(token).digest("hex");
}

export function verifyDeviceToken(token: string, expectedHash?: string | null) {
  if (!expectedHash) return false;
  const actual = Buffer.from(hashDeviceToken(token), "hex");
  const expected = Buffer.from(expectedHash, "hex");
  return actual.length === expected.length && timingSafeEqual(actual, expected);
}

export function virtualMacFromDeviceId(deviceId: string) {
  const bytes = createHash("sha256").update(deviceId).digest();
  const tail = Array.from(bytes.subarray(0, 3)).map((byte) =>
    byte.toString(16).padStart(2, "0").toUpperCase()
  );
  // Virtual app identifier. It is not the device hardware MAC address.
  return ["00", "1A", "79", ...tail].join(":");
}

export function createSessionSignature(payload: string) {
  return createHmac(
    "sha256",
    envSecret("ADMIN_SESSION_SECRET", envSecret("SECRET_KEY", "dev-admin-session-secret"))
  )
    .update(payload)
    .digest("base64url");
}

export function constantTimeTextEqual(a: string, b: string) {
  const left = Buffer.from(a);
  const right = Buffer.from(b);
  return left.length === right.length && timingSafeEqual(left, right);
}
