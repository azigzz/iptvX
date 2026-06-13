export type M3uItem = {
  name: string;
  url: string;
  tvgId?: string;
  tvgName?: string;
  tvgLogo?: string;
  groupTitle?: string;
  catchup?: string;
  rawAttributes: Record<string, string>;
};

const ATTR_PATTERN = /([a-zA-Z0-9_-]+)="([^"]*)"/g;

export function parseExtinf(line: string) {
  const attributes: Record<string, string> = {};
  const [, metadata = ""] = line.split("#EXTINF:");
  let match: RegExpExecArray | null;
  while ((match = ATTR_PATTERN.exec(metadata)) !== null) {
    attributes[match[1]] = match[2];
  }
  const commaIndex = metadata.lastIndexOf(",");
  const name = commaIndex >= 0 ? metadata.slice(commaIndex + 1).trim() : "";
  return { name, attributes };
}

export function parseM3u(content: string) {
  const items: M3uItem[] = [];
  const lines = content.replace(/^\uFEFF/, "").split(/\r?\n/);
  let pending: ReturnType<typeof parseExtinf> | null = null;

  for (const rawLine of lines) {
    const line = rawLine.trim();
    if (!line || line === "#EXTM3U") continue;

    if (line.startsWith("#EXTINF:")) {
      pending = parseExtinf(line);
      continue;
    }

    if (line.startsWith("#")) continue;

    if (pending) {
      const attrs = pending.attributes;
      items.push({
        name: pending.name || attrs["tvg-name"] || line,
        url: line,
        tvgId: attrs["tvg-id"],
        tvgName: attrs["tvg-name"],
        tvgLogo: attrs["tvg-logo"],
        groupTitle: attrs["group-title"],
        catchup: attrs.catchup,
        rawAttributes: attrs
      });
      pending = null;
    }
  }

  return items;
}

export function groupM3uByCategory(items: M3uItem[]) {
  return items.reduce<Record<string, M3uItem[]>>((groups, item) => {
    const key = item.groupTitle || "Sem categoria";
    groups[key] = groups[key] || [];
    groups[key].push(item);
    return groups;
  }, {});
}
