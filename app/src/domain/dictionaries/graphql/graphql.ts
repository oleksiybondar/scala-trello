export const buildDictionariesQuery = (): string => {
  return /* GraphQL */ `
    query {
      ticketSeverities {
        id
        name
        description
      }
      timeTrackingActivities {
        id
        code
        name
        description
      }
    }
  `;
};
