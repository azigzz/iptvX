import { verifyPairingCode } from "@/lib/crypto";

export type PairingDb = {
  device: {
    findUnique(args: {
      where: { virtualMac: string };
      select: {
        id: true;
        virtualMac: true;
        pairingCodeHash: true;
        pairingExpiresAt: true;
        pairedAt: true;
      };
    }): Promise<{
      id: string;
      virtualMac: string;
      pairingCodeHash: string | null;
      pairingExpiresAt: Date | null;
      pairedAt: Date | null;
    } | null>;
    update(args: {
      where: { id: string };
      data: { pairedAt: Date; pairingCodeHash: null; pairingExpiresAt: null };
    }): Promise<unknown>;
  };
  syncLog: {
    create(args: { data: { deviceId: string; status: "OK" | "ERROR" | "INFO"; message: string } }): Promise<unknown>;
  };
};

export async function pairDeviceByCode(db: PairingDb, virtualMac: string, pairingCode: string) {
  const normalizedMac = virtualMac.trim().toUpperCase();
  const device = await db.device.findUnique({
    where: { virtualMac: normalizedMac },
    select: {
      id: true,
      virtualMac: true,
      pairingCodeHash: true,
      pairingExpiresAt: true,
      pairedAt: true
    }
  });

  if (!device) {
    return { ok: false as const, status: 404, error: "Dispositivo nao encontrado" };
  }

  if (!device.pairingExpiresAt || device.pairingExpiresAt.getTime() < Date.now()) {
    await db.syncLog.create({
      data: { deviceId: device.id, status: "ERROR", message: "Codigo de pareamento expirado" }
    });
    return { ok: false as const, status: 400, error: "Codigo expirado" };
  }

  if (!verifyPairingCode(pairingCode, device.pairingCodeHash)) {
    await db.syncLog.create({
      data: { deviceId: device.id, status: "ERROR", message: "Tentativa de pareamento com codigo invalido" }
    });
    return { ok: false as const, status: 400, error: "Codigo invalido" };
  }

  await db.device.update({
    where: { id: device.id },
    data: { pairedAt: new Date(), pairingCodeHash: null, pairingExpiresAt: null }
  });
  await db.syncLog.create({
    data: { deviceId: device.id, status: "OK", message: "Dispositivo pareado pelo painel" }
  });

  return { ok: true as const, deviceId: device.id, virtualMac: device.virtualMac };
}
