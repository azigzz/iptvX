-- CreateEnum
CREATE TYPE "PlaylistType" AS ENUM ('XTREAM', 'M3U');

-- CreateEnum
CREATE TYPE "ContentType" AS ENUM ('LIVE', 'MOVIE', 'SERIES');

-- CreateEnum
CREATE TYPE "ThemeMode" AS ENUM ('DARK', 'SYSTEM');

-- CreateEnum
CREATE TYPE "BufferMode" AS ENUM ('LOW', 'MEDIUM', 'HIGH');

-- CreateEnum
CREATE TYPE "SyncStatus" AS ENUM ('OK', 'ERROR', 'INFO');

-- CreateTable
CREATE TABLE "User" (
    "id" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "passwordHash" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "User_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Device" (
    "id" TEXT NOT NULL,
    "deviceId" TEXT NOT NULL,
    "virtualMac" TEXT NOT NULL,
    "pairingCodeHash" TEXT,
    "pairingExpiresAt" TIMESTAMP(3),
    "pairedAt" TIMESTAMP(3),
    "deviceTokenHash" TEXT,
    "lastSeenAt" TIMESTAMP(3),
    "lastSyncAt" TIMESTAMP(3),
    "model" TEXT,
    "manufacturer" TEXT,
    "androidVersion" TEXT,
    "appVersion" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Device_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Playlist" (
    "id" TEXT NOT NULL,
    "deviceId" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "type" "PlaylistType" NOT NULL,
    "serverUrl" TEXT,
    "usernameEncrypted" TEXT,
    "passwordEncrypted" TEXT,
    "m3uUrlEncrypted" TEXT,
    "epgUrlEncrypted" TEXT,
    "enabled" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Playlist_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "DeviceSetting" (
    "deviceId" TEXT NOT NULL,
    "parentalPinHash" TEXT,
    "theme" "ThemeMode" NOT NULL DEFAULT 'DARK',
    "bufferMode" "BufferMode" NOT NULL DEFAULT 'MEDIUM',
    "performanceMode" BOOLEAN NOT NULL DEFAULT true,

    CONSTRAINT "DeviceSetting_pkey" PRIMARY KEY ("deviceId")
);

-- CreateTable
CREATE TABLE "Favorite" (
    "id" TEXT NOT NULL,
    "deviceId" TEXT NOT NULL,
    "playlistId" TEXT NOT NULL,
    "contentType" "ContentType" NOT NULL,
    "contentId" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "Favorite_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "WatchHistory" (
    "id" TEXT NOT NULL,
    "deviceId" TEXT NOT NULL,
    "playlistId" TEXT NOT NULL,
    "contentType" "ContentType" NOT NULL,
    "contentId" TEXT NOT NULL,
    "positionMs" INTEGER NOT NULL DEFAULT 0,
    "durationMs" INTEGER NOT NULL DEFAULT 0,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "WatchHistory_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "SyncLog" (
    "id" TEXT NOT NULL,
    "deviceId" TEXT NOT NULL,
    "status" "SyncStatus" NOT NULL,
    "message" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "SyncLog_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "User_email_key" ON "User"("email");

-- CreateIndex
CREATE UNIQUE INDEX "Device_deviceId_key" ON "Device"("deviceId");

-- CreateIndex
CREATE UNIQUE INDEX "Device_virtualMac_key" ON "Device"("virtualMac");

-- CreateIndex
CREATE INDEX "Device_virtualMac_idx" ON "Device"("virtualMac");

-- CreateIndex
CREATE INDEX "Device_lastSeenAt_idx" ON "Device"("lastSeenAt");

-- CreateIndex
CREATE INDEX "Playlist_deviceId_idx" ON "Playlist"("deviceId");

-- CreateIndex
CREATE INDEX "Playlist_enabled_idx" ON "Playlist"("enabled");

-- CreateIndex
CREATE INDEX "Favorite_deviceId_idx" ON "Favorite"("deviceId");

-- CreateIndex
CREATE UNIQUE INDEX "Favorite_deviceId_playlistId_contentType_contentId_key" ON "Favorite"("deviceId", "playlistId", "contentType", "contentId");

-- CreateIndex
CREATE INDEX "WatchHistory_deviceId_updatedAt_idx" ON "WatchHistory"("deviceId", "updatedAt");

-- CreateIndex
CREATE UNIQUE INDEX "WatchHistory_deviceId_playlistId_contentType_contentId_key" ON "WatchHistory"("deviceId", "playlistId", "contentType", "contentId");

-- CreateIndex
CREATE INDEX "SyncLog_deviceId_createdAt_idx" ON "SyncLog"("deviceId", "createdAt");

-- AddForeignKey
ALTER TABLE "Playlist" ADD CONSTRAINT "Playlist_deviceId_fkey" FOREIGN KEY ("deviceId") REFERENCES "Device"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "DeviceSetting" ADD CONSTRAINT "DeviceSetting_deviceId_fkey" FOREIGN KEY ("deviceId") REFERENCES "Device"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Favorite" ADD CONSTRAINT "Favorite_deviceId_fkey" FOREIGN KEY ("deviceId") REFERENCES "Device"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Favorite" ADD CONSTRAINT "Favorite_playlistId_fkey" FOREIGN KEY ("playlistId") REFERENCES "Playlist"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "WatchHistory" ADD CONSTRAINT "WatchHistory_deviceId_fkey" FOREIGN KEY ("deviceId") REFERENCES "Device"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "WatchHistory" ADD CONSTRAINT "WatchHistory_playlistId_fkey" FOREIGN KEY ("playlistId") REFERENCES "Playlist"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "SyncLog" ADD CONSTRAINT "SyncLog_deviceId_fkey" FOREIGN KEY ("deviceId") REFERENCES "Device"("id") ON DELETE CASCADE ON UPDATE CASCADE;

