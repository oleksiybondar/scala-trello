import {
  getRefreshDelay,
  toAuthSession
} from "../../../../src/domain/auth/authHelpers";

describe("authHelpers", () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  test("maps token responses into auth sessions", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-03-30T12:00:00.000Z"));

    expect(
      toAuthSession({
        access_token: "access-1",
        refresh_token: "refresh-1",
        token_type: "Bearer",
        expires_in: 120
      })
    ).toEqual({
      accessToken: "access-1",
      expiresAt: Date.parse("2026-03-30T12:02:00.000Z"),
      expiresIn: 120,
      refreshToken: "refresh-1",
      tokenType: "Bearer"
    });
  });

  test("returns a buffered refresh delay", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-03-30T12:00:00.000Z"));

    expect(
      getRefreshDelay({
        accessToken: "access-1",
        expiresAt: Date.parse("2026-03-30T12:05:00.000Z"),
        expiresIn: 300,
        refreshToken: "refresh-1",
        tokenType: "Bearer"
      })
    ).toBe(240_000);
  });

  test("never returns a negative refresh delay", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-03-30T12:00:00.000Z"));

    expect(
      getRefreshDelay({
        accessToken: "access-1",
        expiresAt: Date.parse("2026-03-30T12:00:30.000Z"),
        expiresIn: 30,
        refreshToken: "refresh-1",
        tokenType: "Bearer"
      })
    ).toBe(0);
  });
});
