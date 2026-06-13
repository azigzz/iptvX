import { describe, expect, it } from "vitest";
import { XtreamClient, XtreamClientError } from "@/lib/iptv/xtream";

describe("XtreamClient", () => {
  it("calls player_api.php with credentials", async () => {
    const calls: string[] = [];
    const client = new XtreamClient(
      { serverUrl: "https://xtream.example", username: "u", password: "p" },
      async (input) => {
        calls.push(String(input));
        return new Response(JSON.stringify([{ category_id: "1", category_name: "Live" }]));
      }
    );

    const payload = await client.liveCategories();

    expect(payload).toEqual([{ category_id: "1", category_name: "Live" }]);
    expect(calls[0]).toContain("player_api.php");
    expect(calls[0]).toContain("username=u");
    expect(calls[0]).toContain("password=p");
    expect(calls[0]).toContain("action=get_live_categories");
  });

  it("maps invalid account status", async () => {
    const client = new XtreamClient(
      { serverUrl: "https://xtream.example", username: "u", password: "bad" },
      async () => new Response(JSON.stringify({ user_info: { auth: 0 } }))
    );

    await expect(client.accountInfo()).rejects.toMatchObject<XtreamClientError>({
      code: "INVALID_CREDENTIALS"
    });
  });
});
