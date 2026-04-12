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
          ownerUserId: "user-1",
          tickets: [
            {
              acceptanceCriteria: "API merged",
              assignedTo: null,
              assignedToUserId: null,
              boardId: "board-1",
              commentsCount: 1,
              createdAt: "2026-04-05T08:30:00Z",
              createdBy: null,
              createdByUserId: "user-2",
              description: "Create GraphQL schema",
              estimatedMinutes: 120,
              lastModifiedBy: null,
              lastModifiedByUserId: "user-2",
              modifiedAt: "2026-04-05T09:00:00Z",
              name: "Schema",
              status: "new",
              ticketId: "ticket-1",
              timeEntries: [
                {
                  activityCode: "DEV",
                  activityId: "1",
                  activityName: "Development",
                  description: "Initial work",
                  durationMinutes: 90,
                  entryId: "entry-1",
                  loggedAt: "2026-04-05T09:00:00Z",
                  ticket: null,
                  ticketId: "ticket-1",
                  user: null,
                  userId: "user-2"
                }
              ],
              trackedMinutes: 90
            },
            {
              acceptanceCriteria: null,
              assignedTo: null,
              assignedToUserId: null,
              boardId: "board-1",
              commentsCount: 0,
              createdAt: "2026-04-05T10:00:00Z",
              createdBy: null,
              createdByUserId: "user-2",
              description: null,
              estimatedMinutes: 60,
              lastModifiedBy: null,
              lastModifiedByUserId: "user-2",
              modifiedAt: "2026-04-05T10:30:00Z",
              name: "Resolver",
              status: "in progress",
              ticketId: "ticket-2",
              timeEntries: [
                {
                  activityCode: "DEV",
                  activityId: "1",
                  activityName: "Development",
                  description: null,
                  durationMinutes: 45,
                  entryId: "entry-2",
                  loggedAt: "2026-04-05T10:30:00Z",
                  ticket: null,
                  ticketId: "ticket-2",
                  user: null,
                  userId: "user-2"
                }
              ],
              trackedMinutes: 45
            },
            {
              acceptanceCriteria: null,
              assignedTo: null,
              assignedToUserId: null,
              boardId: "board-1",
              commentsCount: 0,
              createdAt: "2026-04-05T11:00:00Z",
              createdBy: null,
              createdByUserId: "user-2",
              description: null,
              estimatedMinutes: 30,
              lastModifiedBy: null,
              lastModifiedByUserId: "user-2",
              modifiedAt: "2026-04-05T11:30:00Z",
              name: "Review",
              status: "code review",
              ticketId: "ticket-3",
              timeEntries: [],
              trackedMinutes: 0
            },
            {
              acceptanceCriteria: null,
              assignedTo: null,
              assignedToUserId: null,
              boardId: "board-1",
              commentsCount: 0,
              createdAt: "2026-04-05T12:00:00Z",
              createdBy: null,
              createdByUserId: "user-2",
              description: null,
              estimatedMinutes: 15,
              lastModifiedBy: null,
              lastModifiedByUserId: "user-2",
              modifiedAt: "2026-04-05T12:30:00Z",
              name: "Testing",
              status: "in testing",
              ticketId: "ticket-4",
              timeEntries: [],
              trackedMinutes: 0
            },
            {
              acceptanceCriteria: null,
              assignedTo: null,
              assignedToUserId: null,
              boardId: "board-1",
              commentsCount: 0,
              createdAt: "2026-04-05T13:00:00Z",
              createdBy: null,
              createdByUserId: "user-2",
              description: null,
              estimatedMinutes: 30,
              lastModifiedBy: null,
              lastModifiedByUserId: "user-2",
              modifiedAt: "2026-04-05T13:30:00Z",
              name: "Done",
              status: "done",
              ticketId: "ticket-5",
              timeEntries: [],
              trackedMinutes: 0
            }
          ]
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

    expect(screen.getByText("New: 1")).toBeInTheDocument();
    expect(screen.getByText("In progress: 1")).toBeInTheDocument();
    expect(screen.getByText("Code review: 1")).toBeInTheDocument();
    expect(screen.getByText("In Testing: 1")).toBeInTheDocument();
    expect(screen.getByText("Done: 1")).toBeInTheDocument();

    expect(screen.getByText("Time tracking")).toBeInTheDocument();
    expect(screen.getByText(/^Es:/)).toBeInTheDocument();
    expect(screen.getByText(/^Act:/)).toBeInTheDocument();
    expect(screen.getByLabelText("Time tracking line chart")).toBeInTheDocument();
  });
});
