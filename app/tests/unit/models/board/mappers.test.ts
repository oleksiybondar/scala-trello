import {
  mapCreateBoardInputToRequest,
  mapDashboardResponseToBoard
} from "@models/board";

describe("board mappers", () => {
  test("maps a backend dashboard payload into the frontend board model", () => {
    expect(
      mapDashboardResponseToBoard({
        active: true,
        createdAt: "2026-04-08T10:00:00Z",
        createdBy: {
          avatarUrl: "https://cdn.example.com/users/2.png",
          firstName: "Taylor",
          id: "user-2",
          lastName: "Nguyen"
        },
        createdByUserId: "user-2",
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
        ownerUserId: "user-1"
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
      ownerUserId: "user-1"
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
