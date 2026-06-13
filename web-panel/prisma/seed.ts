import { PrismaClient } from "@prisma/client";
import { createHash } from "node:crypto";

const prisma = new PrismaClient();

async function main() {
  const email = process.env.ADMIN_EMAIL || "admin@example.com";
  const rawPassword = process.env.ADMIN_PASSWORD || "change-me-now";
  const passwordHash = `sha256:${createHash("sha256").update(rawPassword).digest("hex")}`;

  await prisma.user.upsert({
    where: { email },
    update: { passwordHash },
    create: { email, passwordHash }
  });

  console.log(`Admin seed ready for ${email}`);
}

main()
  .finally(async () => {
    await prisma.$disconnect();
  })
  .catch(async (error) => {
    console.error(error);
    process.exit(1);
  });
