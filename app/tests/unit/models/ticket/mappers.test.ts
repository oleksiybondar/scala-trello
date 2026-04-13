import { mapTicketResponseToTicket } from "../../../../src/domain/ticket/graphql";

describe("ticket mappers", () => {
  test("maps a backend ticket payload into the frontend ticket model", () => {
    expect(
      mapTicketResponseToTicket({
        acceptanceCriteria: "Merged and deployed",
        assignedTo: {
          avatarUrl: "https://cdn.example.com/users/2.png",
          firstName: "Taylor",
          id: "user-2",
          lastName: "Nguyen"
        },
        assignedToUserId: "user-2",
        board: {
          active: true,
          id: "board-1",
          name: "Platform Board"
        },
        boardId: "board-1",
        comments: [
          {
            authorUserId: "user-3",
            createdAt: "2026-04-10T08:15:00Z",
            id: "comment-1",
            message: "Looks good",
            modifiedAt: "2026-04-10T08:20:00Z",
            relatedCommentId: null,
            ticket: {
              boardId: "board-1",
              id: "ticket-1",
              title: "GraphQL work"
            },
            ticketId: "ticket-1",
            user: {
              avatarUrl: null,
              firstName: "Robin",
              id: "user-3",
              lastName: "Kim"
            }
          }
        ],
        commentsCount: 1,
        createdAt: "2026-04-10T08:00:00Z",
        createdBy: {
          avatarUrl: null,
          firstName: "Alex",
          id: "user-1",
          lastName: "Morgan"
        },
        createdByUserId: "user-1",
        description: "Implement queries and mutations",
        estimatedMinutes: 180,
        id: "ticket-1",
        lastModifiedBy: {
          avatarUrl: null,
          firstName: "Robin",
          id: "user-3",
          lastName: "Kim"
        },
        lastModifiedByUserId: "user-3",
        modifiedAt: "2026-04-10T09:00:00Z",
        name: "GraphQL work",
        priority: 2,
        severityId: "3",
        severityName: "major",
        status: "in progress",
        timeEntries: [
          {
            activityCode: "DEV",
            activityId: "1",
            activityName: "Development",
            description: "Wired frontend models",
            durationMinutes: 90,
            id: "entry-1",
            loggedAt: "2026-04-10T08:45:00Z",
            ticket: {
              description: "Implement queries and mutations",
              id: "ticket-1",
              title: "GraphQL work"
            },
            ticketId: "ticket-1",
            user: {
              avatarUrl: null,
              firstName: "Taylor",
              id: "user-2",
              lastName: "Nguyen"
            },
            userId: "user-2"
          }
        ],
        trackedMinutes: 90
      })
    ).toEqual({
      acceptanceCriteria: "Merged and deployed",
      assignedTo: {
        avatarUrl: "https://cdn.example.com/users/2.png",
        firstName: "Taylor",
        lastName: "Nguyen",
        userId: "user-2"
      },
      assignedToUserId: "user-2",
      board: {
        active: true,
        boardId: "board-1",
        name: "Platform Board"
      },
      boardId: "board-1",
      comments: [
        {
          authorUserId: "user-3",
          commentId: "comment-1",
          createdAt: "2026-04-10T08:15:00Z",
          message: "Looks good",
          modifiedAt: "2026-04-10T08:20:00Z",
          relatedCommentId: null,
          ticket: {
            boardId: "board-1",
            ticketId: "ticket-1",
            title: "GraphQL work"
          },
          ticketId: "ticket-1",
          user: {
            avatarUrl: null,
            firstName: "Robin",
            lastName: "Kim",
            userId: "user-3"
          }
        }
      ],
      commentsCount: 1,
      createdAt: "2026-04-10T08:00:00Z",
      createdBy: {
        avatarUrl: null,
        firstName: "Alex",
        lastName: "Morgan",
        userId: "user-1"
      },
      createdByUserId: "user-1",
      description: "Implement queries and mutations",
      estimatedMinutes: 180,
      lastModifiedBy: {
        avatarUrl: null,
        firstName: "Robin",
        lastName: "Kim",
        userId: "user-3"
      },
      lastModifiedByUserId: "user-3",
      modifiedAt: "2026-04-10T09:00:00Z",
      name: "GraphQL work",
      priority: 2,
      severityId: "3",
      severityName: "major",
      status: "in progress",
      ticketId: "ticket-1",
      timeEntries: [
        {
          activityCode: "DEV",
          activityId: "1",
          activityName: "Development",
          description: "Wired frontend models",
          durationMinutes: 90,
          entryId: "entry-1",
          loggedAt: "2026-04-10T08:45:00Z",
          ticket: {
            description: "Implement queries and mutations",
            ticketId: "ticket-1",
            title: "GraphQL work"
          },
          ticketId: "ticket-1",
          user: {
            avatarUrl: null,
            firstName: "Taylor",
            lastName: "Nguyen",
            userId: "user-2"
          },
          userId: "user-2"
        }
      ],
      trackedMinutes: 90
    });
  });
});
