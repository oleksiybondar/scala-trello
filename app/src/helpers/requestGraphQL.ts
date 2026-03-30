interface GraphQLErrorPayload {
  message?: string;
}

interface GraphQLResponse<TData> {
  data?: TData;
  errors?: GraphQLErrorPayload[];
}

interface RequestGraphQLParams<TVariables extends object> {
  accessToken: string | null;
  document: string;
  tokenType?: string;
  variables?: TVariables;
}

const GRAPHQL_ENDPOINT = "/graphql";

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
