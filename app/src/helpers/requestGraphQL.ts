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

interface RequestGraphQLAuthContext {
  accessToken: string | null;
  tokenType?: string;
}

/**
 * Request parameters accepted by the GraphQL helper.
 */
interface RequestGraphQLParams<TVariables extends object>
  extends RequestGraphQLAuthContext {
  document: string;
  variables?: TVariables;
}

const GRAPHQL_ENDPOINT = buildApiUrl("/graphql");

const requireGraphQLAuth = (
  { accessToken, tokenType }: RequestGraphQLAuthContext,
  errorMessage = "Authentication context is required."
): string => {
  if (accessToken === null || tokenType === undefined) {
    throw new Error(errorMessage);
  }

  return `${tokenType} ${accessToken}`;
};

const assertGraphQLHttpOk = (response: Response): void => {
  if (!response.ok) {
    throw new Error(
      `GraphQL request failed with status ${String(response.status)}.`
    );
  }
};

const assertGraphQLResponseHasNoErrors = <TData>(
  payload: GraphQLResponse<TData>
): void => {
  if (payload.errors !== undefined && payload.errors.length > 0) {
    throw new Error(payload.errors[0]?.message ?? "GraphQL request failed.");
  }
};

function assertGraphQLResponseHasBody<TData>(
  payload: GraphQLResponse<TData>
): asserts payload is GraphQLResponse<TData> & { data: TData } {
  if (payload.data === undefined) {
    throw new Error("GraphQL response did not include data.");
  }
}

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
  tokenType = "Bearer",
  variables
}: RequestGraphQLParams<TVariables>): Promise<TData> => {
  const authorizationHeader = requireGraphQLAuth({
    accessToken,
    tokenType
  });
  const headers: HeadersInit = {
    "Content-Type": "application/json"
  };

  headers.Authorization = authorizationHeader;

  const response = await fetch(GRAPHQL_ENDPOINT, {
    body: JSON.stringify({
      query: document,
      variables
    }),
    credentials: "include",
    headers,
    method: "POST"
  });

  assertGraphQLHttpOk(response);

  const payload = (await response.json()) as GraphQLResponse<TData>;

  assertGraphQLResponseHasNoErrors(payload);
  assertGraphQLResponseHasBody(payload);

  return payload.data;
};
