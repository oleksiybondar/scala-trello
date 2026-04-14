import type { ReactElement } from "react";

import { buildPrioritySelectOptions } from "@components/tickets/metadataSelectOptions";
import { TicketMetadataOptionMenuLayout } from "@components/tickets/TicketMetadataOptionLayout";
import {
  BoardTicketsMultiSelectFilter
} from "@components/boards/board-page/BoardTicketsMultiSelectFilter";

interface BoardTicketsPriorityFilterProps {
  selectedPriorities: number[];
  setSelectedPriorities: (value: number[]) => void;
}

export const BoardTicketsPriorityFilter = ({
  selectedPriorities,
  setSelectedPriorities
}: BoardTicketsPriorityFilterProps): ReactElement => {
  const options = buildPrioritySelectOptions();
  const mappedOptions = options.map(option => {
    return {
      label: option.label,
      menuContent: <TicketMetadataOptionMenuLayout option={option} showDescription={false} />,
      value: option.value
    };
  });

  return (
    <BoardTicketsMultiSelectFilter
      emptyLabel="Any"
      label="Priority"
      labelId="board-ticket-priority-filter-label"
      onChange={setSelectedPriorities}
      options={mappedOptions}
      value={selectedPriorities}
    />
  );
};
