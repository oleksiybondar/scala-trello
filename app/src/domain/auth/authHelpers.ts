import type { AuthSession, AuthTokenResponse } from "./types";

const REFRESH_BUFFER_MS = 60_000;

/**
 * Converts backend token payloads into the client-side auth session shape.
 *
 * @param tokenResponse Raw token payload returned by the backend.
 * @returns Normalized auth session data used by the client.
 */
export const toAuthSession = (
  tokenResponse: AuthTokenResponse
): AuthSession => {
  return {
    accessToken: tokenResponse.access_token,
    expiresAt: Date.now() + tokenResponse.expires_in * 1000,
    expiresIn: tokenResponse.expires_in,
    refreshToken: tokenResponse.refresh_token,
    tokenType: tokenResponse.token_type
  };
};

/**
 * Computes when proactive token refresh should happen for a session.
 *
 * @param session Active auth session.
 * @returns Delay in milliseconds before the next refresh should be attempted.
 */
export const getRefreshDelay = (session: AuthSession): number => {
  return Math.max(session.expiresAt - Date.now() - REFRESH_BUFFER_MS, 0);
};
