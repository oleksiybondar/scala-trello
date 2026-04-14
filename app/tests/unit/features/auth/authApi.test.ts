import {
  loginRequest,
  logoutRequest,
  refreshRequest
} from "../../../../src/domain/auth/authApi";

describe("authApi", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  test("posts login credentials and returns the token response", async () => {
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          access_token: "access-1",
          refresh_token: "refresh-1",
          token_type: "Bearer",
          expires_in: 3600
        }),
        { status: 200 }
      )
    );

    vi.stubGlobal("fetch", fetchMock);

    await expect(
      loginRequest({
        login: "demo",
        password: "secret"
      })
    ).resolves.toEqual({
      access_token: "access-1",
      refresh_token: "refresh-1",
      token_type: "Bearer",
      expires_in: 3600
    });

    expect(fetchMock).toHaveBeenCalledWith(
      "/auth/login",
      expect.objectContaining({
        body: JSON.stringify({
          login: "demo",
          password: "secret"
        }),
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        },
        method: "POST"
      })
    );
  });

  test("posts refresh token to refresh endpoint", async () => {
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          access_token: "access-2",
          refresh_token: "refresh-2",
          token_type: "Bearer",
          expires_in: 1800
        }),
        { status: 200 }
      )
    );

    vi.stubGlobal("fetch", fetchMock);

    await refreshRequest("refresh-1");

    expect(fetchMock).toHaveBeenCalledWith(
      "/auth/refresh",
      expect.objectContaining({
        body: JSON.stringify({
          refresh_token: "refresh-1"
        }),
        method: "POST"
      })
    );
  });

  test("treats logout 204 as success", async () => {
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValueOnce(
      new Response(null, { status: 204 })
    );

    vi.stubGlobal("fetch", fetchMock);

    await expect(logoutRequest("refresh-1")).resolves.toBeUndefined();
    expect(fetchMock).toHaveBeenCalledWith(
      "/auth/logout",
      expect.objectContaining({
        body: JSON.stringify({
          refresh_token: "refresh-1"
        })
      })
    );
  });

  test("throws on non-success responses", async () => {
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValueOnce(
      new Response(null, { status: 401 })
    );

    vi.stubGlobal("fetch", fetchMock);

    await expect(refreshRequest("refresh-1")).rejects.toThrow(
      "Request to /auth/refresh failed with status 401."
    );
  });
});
