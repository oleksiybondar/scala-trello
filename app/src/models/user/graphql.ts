const USER_FIELDS = `
  id
  username
  email
  firstName
  lastName
  avatarUrl
  createdAt
`;

const escapeGraphQLString = (value: string): string => {
  return value.replace(/\\/g, "\\\\").replace(/"/g, '\\"');
};

const serializeGraphQLString = (value: string): string => {
  return `"${escapeGraphQLString(value)}"`;
};

const serializeNullableGraphQLString = (value: string | null): string => {
  return value === null ? "null" : serializeGraphQLString(value);
};

export const buildCurrentUserProfileQuery = (userId: string): string => {
  return /* GraphQL */ `
    query {
      user(id: ${serializeGraphQLString(userId)}) {
        ${USER_FIELDS}
      }
    }
  `;
};

export const buildUpdateProfileMutation = (
  firstName: string,
  lastName: string
): string => {
  return /* GraphQL */ `
    mutation {
      updateProfile(
        firstName: ${serializeGraphQLString(firstName)}
        lastName: ${serializeGraphQLString(lastName)}
      ) {
        ${USER_FIELDS}
      }
    }
  `;
};

export const buildChangeAvatarMutation = (avatarUrl: string | null): string => {
  return /* GraphQL */ `
    mutation {
      changeAvatar(avatarUrl: ${serializeNullableGraphQLString(avatarUrl)}) {
        ${USER_FIELDS}
      }
    }
  `;
};

export const buildChangeUsernameMutation = (username: string): string => {
  return /* GraphQL */ `
    mutation {
      changeUsername(username: ${serializeGraphQLString(username)}) {
        ${USER_FIELDS}
      }
    }
  `;
};

export const buildChangeEmailMutation = (email: string): string => {
  return /* GraphQL */ `
    mutation {
      changeEmail(email: ${serializeGraphQLString(email)}) {
        ${USER_FIELDS}
      }
    }
  `;
};

export const buildChangePasswordMutation = (
  currentPassword: string,
  newPassword: string
): string => {
  return /* GraphQL */ `
    mutation {
      changePassword(
        currentPassword: ${serializeGraphQLString(currentPassword)}
        newPassword: ${serializeGraphQLString(newPassword)}
      )
    }
  `;
};
