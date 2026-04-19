import { screen } from "@testing-library/react";

import { TimeVelocityChart } from "@components/charts/TimeVelocityChart";
import { renderApp } from "@tests/setup/render";

describe("TimeVelocityChart", () => {
  test("shows overdue legend when actual subtotal exceeds estimate", () => {
    renderApp(
      <TimeVelocityChart
        data={{
          actualSeriesMinutes: [30, 90, 150],
          estimatedMinutes: 120
        }}
      />
    );

    expect(screen.getByText("Es: 2h:00m")).toBeInTheDocument();
    expect(screen.getByText("Act: 2h:30m")).toBeInTheDocument();
    expect(screen.getByText("Ovd: 0h:30m")).toBeInTheDocument();
  });

  test("hides overdue legend when actual subtotal is within estimate", () => {
    renderApp(
      <TimeVelocityChart
        data={{
          actualSeriesMinutes: [20, 45, 60],
          estimatedMinutes: 120
        }}
      />
    );

    expect(screen.getByText("Es: 2h:00m")).toBeInTheDocument();
    expect(screen.getByText("Act: 1h:00m")).toBeInTheDocument();
    expect(screen.queryByText(/^Ovd:/)).not.toBeInTheDocument();
  });

  test("renders non-empty lines for a single actual point and starts from baseline", () => {
    const { container } = renderApp(
      <TimeVelocityChart
        data={{
          actualSeriesMinutes: [60],
          estimatedMinutes: 120
        }}
      />
    );

    const chartPaths = container.querySelectorAll('svg path');

    expect(chartPaths.length).toBeGreaterThanOrEqual(2);
    expect(screen.getByText("Es: 2h:00m")).toBeInTheDocument();
    expect(screen.getByText("Act: 1h:00m")).toBeInTheDocument();
  });
});
