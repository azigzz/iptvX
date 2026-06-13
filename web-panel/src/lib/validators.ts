import { z } from "zod";

export const playlistTypeSchema = z.enum(["XTREAM", "M3U"]);

export const safeHttpUrl = z
  .string()
  .trim()
  .url()
  .refine((value) => {
    try {
      const parsed = new URL(value);
      return parsed.protocol === "http:" || parsed.protocol === "https:";
    } catch {
      return false;
    }
  }, "Use uma URL http ou https valida");

export const deviceRegisterSchema = z.object({
  deviceId: z.string().min(8).max(128),
  model: z.string().max(120).optional(),
  manufacturer: z.string().max(120).optional(),
  androidVersion: z.string().max(40).optional(),
  appVersion: z.string().max(40).optional()
});

export const deviceAuthSchema = z.object({
  deviceId: z.string().min(8).max(128),
  token: z.string().min(20).max(256)
});

export const pairSchema = z.object({
  virtualMac: z.string().trim().min(8).max(32),
  deviceId: z.string().trim().regex(/^\d{1,5}$/, "ID deve ter ate 5 digitos numericos")
});

const playlistPayloadBaseSchema = z.object({
    name: z.string().trim().min(1).max(80),
    type: playlistTypeSchema,
    serverUrl: safeHttpUrl.optional().or(z.literal("")),
    username: z.string().max(256).optional().or(z.literal("")),
    password: z.string().max(256).optional().or(z.literal("")),
    m3uUrl: safeHttpUrl.optional().or(z.literal("")),
    epgUrl: safeHttpUrl.optional().or(z.literal("")),
    enabled: z.boolean().optional()
  });

export const playlistPayloadSchema = playlistPayloadBaseSchema
  .superRefine((payload, ctx) => {
    if (payload.type === "XTREAM") {
      if (!payload.serverUrl) {
        ctx.addIssue({ code: "custom", path: ["serverUrl"], message: "serverUrl obrigatoria" });
      }
      if (!payload.username) {
        ctx.addIssue({ code: "custom", path: ["username"], message: "username obrigatorio" });
      }
      if (!payload.password) {
        ctx.addIssue({ code: "custom", path: ["password"], message: "password obrigatorio" });
      }
    }
    if (payload.type === "M3U" && !payload.m3uUrl) {
      ctx.addIssue({ code: "custom", path: ["m3uUrl"], message: "m3uUrl obrigatoria" });
    }
  });

export const playlistPatchSchema = playlistPayloadBaseSchema.partial();

export const favoriteSchema = deviceAuthSchema.extend({
  playlistId: z.string().min(1),
  contentType: z.enum(["LIVE", "MOVIE", "SERIES"]),
  contentId: z.string().min(1),
  name: z.string().min(1).max(160)
});

export const historySchema = favoriteSchema.extend({
  positionMs: z.number().int().min(0),
  durationMs: z.number().int().min(0)
});
