import { redirect } from "next/navigation";
import { isAdminLoginDisabled, isAdminSignedIn } from "@/lib/auth";

export default async function HomePage() {
  if (isAdminLoginDisabled()) redirect("/pair");
  redirect((await isAdminSignedIn()) ? "/dashboard" : "/login");
}
