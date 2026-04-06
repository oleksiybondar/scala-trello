import { buildApiUrl } from "@configs/apiConfig";

/**
 * GraphQL error entry returned by the backend when a request cannot be fulfilled.
 */
interface GraphQLErrorPayload {
  message?: string;
}

/**
 * GraphQL transport envelope returned by the backend.
 */
interface GraphQLResponse<TData> {
  data?: TData;
  errors?: GraphQLErrorPayload[];
}

/**
 * Request parameters accepted by the GraphQL helper.
 */
interface RequestGraphQLParams<TVariables extends object> {
  accessToken: string | null;
  document: string;
  tokenType?: string;
  variables?: TVariables;
}

const GRAPHQL_ENDPOINT = buildApiUrl("/graphql");

/**
 * Executes a GraphQL request and returns the typed `data` payload.
 *
 * @typeParam TData GraphQL `data` payload shape.
 * @typeParam TVariables GraphQL variables object shape.
 * @param params Request document, optional variables, and auth context.
 * @returns Parsed GraphQL `data` payload.
 * @throws Error When the HTTP request fails, the GraphQL response contains errors,
 * or the response omits `data`.
 */
export const requestGraphQL = async <
  TData,
  TVariables extends object = Record<string, never>
>({
  accessToken,
  document,
  tokenType,
  variables
}: RequestGraphQLParams<TVariables>): Promise<TData> => {
  const headers: HeadersInit = {
    "Content-Type": "application/json"
  };

  if (accessToken !== null && tokenType !== undefined) {
    headers.Authorization = `${tokenType} ${accessToken}`;
  }

  const response = await fetch(GRAPHQL_ENDPOINT, {
    body: JSON.stringify({
      query: document,
      variables
    }),
    credentials: "include",
    headers,
    method: "POST"
  });

  if (!response.ok) {
    throw new Error(
      `GraphQL request failed with status ${String(response.status)}.`
    );
  }

  const payload = (await response.json()) as GraphQLResponse<TData>;

  if (payload.errors !== undefined && payload.errors.length > 0) {
    throw new Error(payload.errors[0]?.message ?? "GraphQL request failed.");
  }

  if (payload.data === undefined) {
    throw new Error("GraphQL response did not include data.");
  }

  return payload.data;
};
