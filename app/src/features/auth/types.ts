/**
 * High-level authentication lifecycle state exposed to the UI.
 */
export type AuthStatus =
  | "anonymous"
  | "authenticating"
  | "authenticated"
  | "refreshing";

/**
 * User-provided credentials for the login endpoint.
 */
export interface LoginCredentials {
  /** Login identifier accepted by the backend. */
  login: string;
  /** Plain-text password submitted during authentication. */
  password: string;
}

/**
 * User-provided registration payload accepted by the backend.
 */
export interface RegisterCredentials {
  /** Email address used for account creation. */
  email: string;
  /** Current first name. */
  first_name: string;
  /** Current last name. */
  last_name: string;
  /** Plain-text password submitted during registration. */
  password: string;
}

/**
 * Raw token payload returned by the backend auth endpoints.
 */
export interface AuthTokenResponse {
  /** Short-lived token used to authorize API requests. */
  access_token: string;
  /** Long-lived token used to renew the access token. */
  refresh_token: string;
  /** Backend-declared token scheme, typically `Bearer`. */
  token_type: string;
  /** Access token lifetime in seconds. */
  expires_in: number;
}

/**
 * Raw current-user payload returned by the backend `GET /auth/me` endpoint.
 */
export interface AuthCurrentUserResponse {
  /** Stable user identifier. */
  id: string;
  /** Public username when one is assigned. */
  username: string | null;
  /** Primary email address when available. */
  email: string | null;
  /** Current first name. */
  first_name: string;
  /** Current last name. */
  last_name: string;
  /** Public avatar URL when configured. */
  avatar_url: string | null;
  /** ISO-8601 creation timestamp. */
  created_at: string;
}

/**
 * Normalized client-side representation of the active auth session.
 */
export interface AuthSession {
  /** Current access token value ready for request authorization. */
  accessToken: string;
  /** Absolute Unix timestamp in milliseconds when the access token expires. */
  expiresAt: number;
  /** Access token lifetime in seconds as returned by the backend. */
  expiresIn: number;
  /** Current refresh token value associated with the session. */
  refreshToken: string;
  /** Token scheme used when sending the access token to the backend. */
  tokenType: string;
}
