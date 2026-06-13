export function formatDateTime(value?: Date | string | null) {
  if (!value) return "-";
  const date = typeof value === "string" ? new Date(value) : value;
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short"
  }).format(date);
}

export function onlineState(value?: Date | string | null) {
  if (!value) return { online: false, label: "Offline" };
  const date = typeof value === "string" ? new Date(value) : value;
  const minutes = (Date.now() - date.getTime()) / 60_000;
  return minutes <= 5
    ? { online: true, label: "Online" }
    : { online: false, label: `Visto ha ${Math.round(minutes)} min` };
}
