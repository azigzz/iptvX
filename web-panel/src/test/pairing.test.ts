import { describe, expect, it } from "vitest";
import { hashPairingCode } from "@/lib/crypto";
import { pairDeviceByCode, type PairingDb } from "@/lib/services/pairing";

describe("pairDeviceByCode", () => {
  it("pairs when virtual mac and code are valid", async () => {
    const updates: unknown[] = [];
    const logs: unknown[] = [];
    const db: PairingDb = {
      device: {
        findUnique: async () => ({
          id: "device-row-id",
          virtualMac: "00:1A:79:AA:BB:CC",
          pairingCodeHash: hashPairingCode("ABC123"),
          pairingExpiresAt: new Date(Date.now() + 60_000),
          pairedAt: null
        }),
        update: async (args) => {
          updates.push(args);
          return {};
        }
      },
      syncLog: {
        create: async (args) => {
          logs.push(args);
          return {};
        }
      }
    };

    const result = await pairDeviceByCode(db, "00:1a:79:aa:bb:cc", "abc123");

    expect(result.ok).toBe(true);
    expect(updates).toHaveLength(1);
    expect(logs).toHaveLength(1);
  });

  it("rejects expired codes", async () => {
    const db: PairingDb = {
      device: {
        findUnique: async () => ({
          id: "device-row-id",
          virtualMac: "00:1A:79:AA:BB:CC",
          pairingCodeHash: hashPairingCode("ABC123"),
          pairingExpiresAt: new Date(Date.now() - 1_000),
          pairedAt: null
        }),
        update: async () => ({})
      },
      syncLog: {
        create: async () => ({})
      }
    };

    const result = await pairDeviceByCode(db, "00:1A:79:AA:BB:CC", "ABC123");
    expect(result).toMatchObject({ ok: false, status: 400 });
  });
});
