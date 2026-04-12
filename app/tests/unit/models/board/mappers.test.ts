import {
  mapCreateBoardInputToRequest,
  mapBoardResponseToBoard
} from "../../../../src/domain/board/graphql";

describe("board mappers", () => {
  test("maps a backend dashboard payload into the frontend board model", () => {
    expect(
      mapBoardResponseToBoard({
        active: true,
        createdAt: "2026-04-08T10:00:00Z",
        createdBy: {
          avatarUrl: "https://cdn.example.com/users/2.png",
          firstName: "Taylor",
          id: "user-2",
          lastName: "Nguyen"
        },
        createdByUserId: "user-2",
        currentUserRole: {
          description: "Board administrator",
          id: "1",
          name: "admin",
          permissions: [
            {
              area: "dashboard",
              canCreate: true,
              canDelete: true,
              canModify: true,
              canRead: true,
              canReassign: true,
              id: "perm-1"
            }
          ]
        },
        description: "Sprint board for backend work.",
        id: "board-1",
        lastModifiedByUserId: "user-3",
        membersCount: 5,
        modifiedAt: "2026-04-08T12:00:00Z",
        name: "Backend API sprint",
        owner: {
          avatarUrl: "https://cdn.example.com/users/1.png",
          firstName: "Alex",
          id: "user-1",
          lastName: "Morgan"
        },
        ownerUserId: "user-1",
        tickets: [
          {
            acceptanceCriteria: "Merged and deployed",
            assignedTo: {
              avatarUrl: "https://cdn.example.com/users/4.png",
              firstName: "Jamie",
              id: "user-4",
              lastName: "Lee"
            },
            assignedToUserId: "user-4",
            boardId: "board-1",
            commentsCount: 3,
            createdAt: "2026-04-08T10:30:00Z",
            createdBy: {
              avatarUrl: "https://cdn.example.com/users/2.png",
              firstName: "Taylor",
              id: "user-2",
              lastName: "Nguyen"
            },
            createdByUserId: "user-2",
            description: "Implement GraphQL updates",
            estimatedMinutes: 120,
            id: "ticket-1",
            lastModifiedBy: {
              avatarUrl: "https://cdn.example.com/users/3.png",
              firstName: "Robin",
              id: "user-3",
              lastName: "Kim"
            },
            lastModifiedByUserId: "user-3",
            modifiedAt: "2026-04-08T11:30:00Z",
            name: "Board metrics",
            status: "in progress",
            timeEntries: [
              {
                activityCode: "DEV",
                activityId: "1",
                activityName: "Development",
                description: "Initial implementation",
                durationMinutes: 90,
                id: "entry-1",
                loggedAt: "2026-04-08T11:00:00Z",
                ticket: {
                  description: "Implement GraphQL updates",
                  id: "ticket-1",
                  title: "Board metrics"
                },
                ticketId: "ticket-1",
                user: {
                  avatarUrl: "https://cdn.example.com/users/4.png",
                  firstName: "Jamie",
                  id: "user-4",
                  lastName: "Lee"
                },
                userId: "user-4"
              }
            ],
            trackedMinutes: 90
          }
        ]
      })
    ).toEqual({
      active: true,
      boardId: "board-1",
      createdAt: "2026-04-08T10:00:00Z",
      createdBy: {
        avatarUrl: "https://cdn.example.com/users/2.png",
        firstName: "Taylor",
        lastName: "Nguyen",
        userId: "user-2"
      },
      createdByUserId: "user-2",
      currentUserRole: {
        description: "Board administrator",
        permissions: [
          {
            area: "dashboard",
            canCreate: true,
            canDelete: true,
            canModify: true,
            canRead: true,
            canReassign: true
          }
        ],
        roleId: "1",
        roleName: "admin"
      },
      description: "Sprint board for backend work.",
      lastModifiedByUserId: "user-3",
      membersCount: 5,
      modifiedAt: "2026-04-08T12:00:00Z",
      name: "Backend API sprint",
      owner: {
        avatarUrl: "https://cdn.example.com/users/1.png",
        firstName: "Alex",
        lastName: "Morgan",
        userId: "user-1"
      },
      ownerUserId: "user-1",
      tickets: [
        {
          acceptanceCriteria: "Merged and deployed",
          assignedTo: {
            avatarUrl: "https://cdn.example.com/users/4.png",
            firstName: "Jamie",
            lastName: "Lee",
            userId: "user-4"
          },
          assignedToUserId: "user-4",
          boardId: "board-1",
          commentsCount: 3,
          createdAt: "2026-04-08T10:30:00Z",
          createdBy: {
            avatarUrl: "https://cdn.example.com/users/2.png",
            firstName: "Taylor",
            lastName: "Nguyen",
            userId: "user-2"
          },
          createdByUserId: "user-2",
          description: "Implement GraphQL updates",
          estimatedMinutes: 120,
          lastModifiedBy: {
            avatarUrl: "https://cdn.example.com/users/3.png",
            firstName: "Robin",
            lastName: "Kim",
            userId: "user-3"
          },
          lastModifiedByUserId: "user-3",
          modifiedAt: "2026-04-08T11:30:00Z",
          name: "Board metrics",
          status: "in progress",
          ticketId: "ticket-1",
          timeEntries: [
            {
              activityCode: "DEV",
              activityId: "1",
              activityName: "Development",
              description: "Initial implementation",
              durationMinutes: 90,
              entryId: "entry-1",
              loggedAt: "2026-04-08T11:00:00Z",
              ticket: {
                description: "Implement GraphQL updates",
                ticketId: "ticket-1",
                title: "Board metrics"
              },
              ticketId: "ticket-1",
              user: {
                avatarUrl: "https://cdn.example.com/users/4.png",
                firstName: "Jamie",
                lastName: "Lee",
                userId: "user-4"
              },
              userId: "user-4"
            }
          ],
          trackedMinutes: 90
        }
      ]
    });
  });

  test("omits empty descriptions from the create board request", () => {
    expect(
      mapCreateBoardInputToRequest({
        description: "   ",
        name: "  Backend API sprint  "
      })
    ).toEqual({
      name: "Backend API sprint"
    });
  });

  test("keeps a non-empty description in the create board request", () => {
    expect(
      mapCreateBoardInputToRequest({
        description: "  Sprint scope and API tickets.  ",
        name: "Backend API sprint"
      })
    ).toEqual({
      description: "Sprint scope and API tickets.",
      name: "Backend API sprint"
    });
  });
});
