export const buildRolesQuery = (): string => {
  return /* GraphQL */ `
    query {
      roles {
        id
        name
        description
      }
    }
  `;
};
