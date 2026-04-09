import { screen } from "@testing-library/react";

import { BoardCard } from "@components/boards/BoardCard";
import { renderApp } from "@tests/setup/render";

describe("BoardCard", () => {
  test("renders board header, info, tickets, and time tracking sections", () => {
    renderApp(
      <BoardCard
        board={{
          active: true,
          boardId: "board-1",
          createdAt: "2026-04-05T08:00:00Z",
          createdBy: {
            avatarUrl: null,
            firstName: "Taylor",
            lastName: "Nguyen",
            userId: "user-2"
          },
          createdByUserId: "user-2",
          description: "Planning board for platform work.",
          lastModifiedByUserId: "user-3",
          membersCount: 2,
          modifiedAt: "2026-04-06T09:00:00Z",
          name: "Platform Core",
          owner: {
            avatarUrl: null,
            firstName: "Alexandra",
            lastName: "Montgomery",
            userId: "user-1"
          },
          ownerUserId: "user-1"
        }}
      />
    );

    expect(screen.getByRole("heading", { name: "Platform Core" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Board settings" })).toBeInTheDocument();

    expect(screen.getByText("Owned by")).toBeInTheDocument();
    expect(screen.getByText("Alexandra")).toBeInTheDocument();
    expect(screen.getByText("Montgomery")).toBeInTheDocument();
    expect(screen.getByText("Created by")).toBeInTheDocument();
    expect(screen.getByText("Taylor")).toBeInTheDocument();
    expect(screen.getByText("Nguyen")).toBeInTheDocument();
    expect(screen.getByText("Members")).toBeInTheDocument();
    expect(screen.getByText("2")).toBeInTheDocument();
    expect(screen.getByText("Active")).toBeInTheDocument();

    expect(screen.getByText("New: 5")).toBeInTheDocument();
    expect(screen.getByText("In progress: 3")).toBeInTheDocument();
    expect(screen.getByText("Code review: 2")).toBeInTheDocument();
    expect(screen.getByText("In Testing: 2")).toBeInTheDocument();
    expect(screen.getByText("Done: 4")).toBeInTheDocument();

    expect(screen.getByText("Time tracking")).toBeInTheDocument();
    expect(screen.getByText(/^Es:/)).toBeInTheDocument();
    expect(screen.getByText(/^Act:/)).toBeInTheDocument();
    expect(screen.getByText(/^Overdue:/)).toBeInTheDocument();
    expect(screen.getByLabelText("Time tracking line chart")).toBeInTheDocument();
  });
});
