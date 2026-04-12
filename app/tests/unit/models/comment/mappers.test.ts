import { mapCommentResponseToComment } from "../../../../src/domain/comment/graphql";

describe("comment mappers", () => {
  test("maps a backend comment payload into the frontend model", () => {
    expect(
      mapCommentResponseToComment({
        authorUserId: "user-1",
        createdAt: "2026-04-10T09:00:00Z",
        id: "comment-1",
        message: "Please review the GraphQL query.",
        modifiedAt: "2026-04-10T09:30:00Z",
        relatedCommentId: null,
        ticket: {
          boardId: "board-1",
          id: "ticket-1",
          title: "GraphQL query"
        },
        ticketId: "ticket-1",
        user: {
          avatarUrl: null,
          firstName: "Alex",
          id: "user-1",
          lastName: "Morgan"
        }
      })
    ).toEqual({
      authorUserId: "user-1",
      commentId: "comment-1",
      createdAt: "2026-04-10T09:00:00Z",
      message: "Please review the GraphQL query.",
      modifiedAt: "2026-04-10T09:30:00Z",
      relatedCommentId: null,
      ticket: {
        boardId: "board-1",
        ticketId: "ticket-1",
        title: "GraphQL query"
      },
      ticketId: "ticket-1",
      user: {
        avatarUrl: null,
        firstName: "Alex",
        lastName: "Morgan",
        userId: "user-1"
      }
    });
  });
});
