import {
  mapCreateBoardInputToRequest,
  mapDashboardResponseToBoard
} from "@models/board";

describe("board mappers", () => {
  test("maps a backend dashboard payload into the frontend board model", () => {
    expect(
      mapDashboardResponseToBoard({
        active: true,
        created_at: "2026-04-08T10:00:00Z",
        created_by_user_id: "user-2",
        description: "Sprint board for backend work.",
        id: "board-1",
        last_modified_by_user_id: "user-3",
        modified_at: "2026-04-08T12:00:00Z",
        name: "Backend API sprint",
        owner_user_id: "user-1"
      })
    ).toEqual({
      active: true,
      boardId: "board-1",
      createdAt: "2026-04-08T10:00:00Z",
      createdByUserId: "user-2",
      description: "Sprint board for backend work.",
      lastModifiedByUserId: "user-3",
      modifiedAt: "2026-04-08T12:00:00Z",
      name: "Backend API sprint",
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
