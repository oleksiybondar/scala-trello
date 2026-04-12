import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { CreateTicketForm } from "@components/tickets/CreateTicketForm";
import { renderApp } from "@tests/setup/render";

describe("CreateTicketForm", () => {
  test("submits estimated time as minutes when HH:MM is entered", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    renderApp(
      <CreateTicketForm
        onCancel={vi.fn()}
        onSubmit={onSubmit}
      />
    );

    await user.type(screen.getByRole("textbox", { name: "Ticket title" }), "GraphQL task");
    await user.type(screen.getByRole("textbox", { name: "Estimate (HH:MM)" }), "02:30");
    await user.click(screen.getByRole("button", { name: "Create ticket" }));

    expect(onSubmit).toHaveBeenCalledWith({
      acceptanceCriteria: "",
      description: "",
      estimatedMinutes: 150,
      title: "GraphQL task"
    });
  });

  test("treats a duration without a colon as hours only", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    renderApp(
      <CreateTicketForm
        onCancel={vi.fn()}
        onSubmit={onSubmit}
      />
    );

    await user.type(screen.getByRole("textbox", { name: "Ticket title" }), "Board cleanup");
    await user.type(screen.getByRole("textbox", { name: "Estimate (HH:MM)" }), "2");
    await user.click(screen.getByRole("button", { name: "Create ticket" }));

    expect(onSubmit).toHaveBeenCalledWith({
      acceptanceCriteria: "",
      description: "",
      estimatedMinutes: 120,
      title: "Board cleanup"
    });
  });

  test("adjusts the estimate by 15-minute steps", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    renderApp(
      <CreateTicketForm
        onCancel={vi.fn()}
        onSubmit={onSubmit}
      />
    );

    await user.type(screen.getByRole("textbox", { name: "Ticket title" }), "Ticket draft");
    await user.click(screen.getByRole("button", { name: "Increase estimate by 15 minutes" }));
    await user.click(screen.getByRole("button", { name: "Increase estimate by 15 minutes" }));
    await user.click(screen.getByRole("button", { name: "Create ticket" }));

    expect(onSubmit).toHaveBeenCalledWith({
      acceptanceCriteria: "",
      description: "",
      estimatedMinutes: 30,
      title: "Ticket draft"
    });
  });
});
