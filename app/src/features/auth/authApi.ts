import type {
  AuthCurrentUserResponse,
  RegisterCredentials,
  AuthTokenResponse,
  LoginCredentials
} from "@features/auth/types";

const AUTH_LOGIN_PATH = "/auth/login";
const AUTH_LOGOUT_PATH = "/auth/logout";
const AUTH_ME_PATH = "/auth/me";
const AUTH_REGISTER_PATH = "/auth/register";
const AUTH_REFRESH_PATH = "/auth/refresh";

/**
 * Executes a JSON POST request against the backend auth endpoints.
 *
 * @typeParam TResponse Response payload type expected from the backend.
 * @param path Backend endpoint path relative to the frontend origin.
 * @param body JSON-serializable request body.
 * @returns Parsed response payload or `undefined` for `204 No Content`.
 */
const postJson = async <TResponse>(
  path: string,
  body: unknown
): Promise<TResponse | undefined> => {
  const response = await fetch(path, {
    body: JSON.stringify(body),
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    method: "POST"
  });

  if (!response.ok) {
    throw new Error(
      `Request to ${path} failed with status ${String(response.status)}.`
    );
  }

  if (response.status === 204) {
    return undefined;
  }

  return (await response.json()) as TResponse;
};

/**
 * Executes a JSON POST request that must return a response payload.
 *
 * @typeParam TResponse Response payload type expected from the backend.
 * @param path Backend endpoint path relative to the frontend origin.
 * @param body JSON-serializable request body.
 * @returns Parsed response payload.
 */
const postRequiredJson = async <TResponse>(
  path: string,
  body: unknown
): Promise<TResponse> => {
  const response = await postJson<TResponse>(path, body);

  if (response === undefined) {
    throw new Error(`Request to ${path} returned an empty response body.`);
  }

  return response;
};

/**
 * Loads the currently authenticated user from the backend.
 *
 * @param accessToken Access token used for bearer authorization.
 * @param tokenType Backend-declared token type, typically `Bearer`.
 * @returns The current authenticated user payload.
 */
export const meRequest = async (
  accessToken: string,
  tokenType: string
): Promise<AuthCurrentUserResponse> => {
  const response = await fetch(AUTH_ME_PATH, {
    credentials: "include",
    headers: {
      Authorization: `${tokenType} ${accessToken}`
    },
    method: "GET"
  });

  if (!response.ok) {
    throw new Error(
      `Request to ${AUTH_ME_PATH} failed with status ${String(response.status)}.`
    );
  }

  return (await response.json()) as AuthCurrentUserResponse;
};

/**
 * Exchanges user credentials for a new access and refresh token pair.
 *
 * @param credentials Login credentials entered by the user.
 * @returns The token payload returned by the backend.
 */
export const loginRequest = async (
  credentials: LoginCredentials
): Promise<AuthTokenResponse> => {
  return postRequiredJson<AuthTokenResponse>(AUTH_LOGIN_PATH, credentials);
};

/**
 * Registers a new user and returns the initial authenticated session.
 *
 * @param credentials Registration payload entered by the user.
 * @returns The token payload returned by the backend.
 */
export const registerRequest = async (
  credentials: RegisterCredentials
): Promise<AuthTokenResponse> => {
  return postRequiredJson<AuthTokenResponse>(AUTH_REGISTER_PATH, credentials);
};

/**
 * Renews the current token pair using the active refresh token.
 *
 * @param refreshToken Refresh token associated with the active session.
 * @returns The refreshed token payload returned by the backend.
 */
export const refreshRequest = async (
  refreshToken: string
): Promise<AuthTokenResponse> => {
  return postRequiredJson<AuthTokenResponse>(
    AUTH_REFRESH_PATH,
    {
      refresh_token: refreshToken
    }
  );
};

/**
 * Invalidates the active refresh token on the backend.
 *
 * @param refreshToken Refresh token that should be revoked.
 * @returns A promise that resolves when the backend acknowledges logout.
 */
export const logoutRequest = async (refreshToken: string): Promise<void> => {
  await postJson(AUTH_LOGOUT_PATH, {
    refresh_token: refreshToken
  });
};
