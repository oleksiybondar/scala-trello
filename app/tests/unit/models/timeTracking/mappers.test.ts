import { mapTimeTrackingEntryResponseToTimeTrackingEntry } from "../../../../src/domain/time-tracking/graphql";

describe("time tracking mappers", () => {
  test("maps a backend time tracking payload into the frontend model", () => {
    expect(
      mapTimeTrackingEntryResponseToTimeTrackingEntry({
        activityCode: "DEV",
        activityId: "1",
        activityName: "Development",
        description: "Implemented forms",
        durationMinutes: 45,
        id: "entry-1",
        loggedAt: "2026-04-10T09:00:00Z",
        ticket: {
          description: "Create ticket forms",
          id: "ticket-1",
          title: "Ticket UI"
        },
        ticketId: "ticket-1",
        user: {
          avatarUrl: null,
          firstName: "Jamie",
          id: "user-2",
          lastName: "Lee"
        },
        userId: "user-2"
      })
    ).toEqual({
      activityCode: "DEV",
      activityId: "1",
      activityName: "Development",
      description: "Implemented forms",
      durationMinutes: 45,
      entryId: "entry-1",
      loggedAt: "2026-04-10T09:00:00Z",
      ticket: {
        description: "Create ticket forms",
        ticketId: "ticket-1",
        title: "Ticket UI"
      },
      ticketId: "ticket-1",
      user: {
        avatarUrl: null,
        firstName: "Jamie",
        lastName: "Lee",
        userId: "user-2"
      },
      userId: "user-2"
    });
  });
});
