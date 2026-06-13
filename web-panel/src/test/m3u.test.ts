import { describe, expect, it } from "vitest";
import { groupM3uByCategory, parseM3u } from "@/lib/iptv/m3u";

describe("parseM3u", () => {
  it("parses EXTINF attributes and channel url", () => {
    const items = parseM3u(`#EXTM3U
#EXTINF:-1 tvg-id="canal-1" tvg-name="Canal 1" tvg-logo="https://img/logo.png" group-title="News" catchup="default",Canal 1 HD
https://example.com/live/canal1.m3u8
#EXTINF:-1 group-title="Movies",Filme
https://example.com/movie.mp4`);

    expect(items).toHaveLength(2);
    expect(items[0]).toMatchObject({
      name: "Canal 1 HD",
      tvgId: "canal-1",
      tvgName: "Canal 1",
      tvgLogo: "https://img/logo.png",
      groupTitle: "News",
      catchup: "default",
      url: "https://example.com/live/canal1.m3u8"
    });
    expect(groupM3uByCategory(items).News).toHaveLength(1);
  });
});
