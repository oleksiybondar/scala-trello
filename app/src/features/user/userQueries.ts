const escapeGraphQLString = (value: string): string => {
  return value.replace(/\\/g, "\\\\").replace(/"/g, '\\"');
};

export const buildCurrentUserProfileQuery = (userId: string): string => {
  const escapedUserId = escapeGraphQLString(userId);

  return /* GraphQL */ `
    query {
      user(id: "${escapedUserId}") {
        id
        username
        email
        firstName
        lastName
        avatarUrl
        createdAt
      }
    }
  `;
};
