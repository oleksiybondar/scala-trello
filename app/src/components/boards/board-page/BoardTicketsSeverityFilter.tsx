import type { ReactElement } from "react";

import {
  buildSeveritySelectOptions
} from "@components/tickets/metadataSelectOptions";
import { TicketMetadataOptionMenuLayout } from "@components/tickets/TicketMetadataOptionLayout";
import {
  BoardTicketsMultiSelectFilter
} from "@components/boards/board-page/BoardTicketsMultiSelectFilter";
import type { DictionarySeverity } from "../../../domain/dictionaries/graphql";

interface BoardTicketsSeverityFilterProps {
  severities: DictionarySeverity[];
  selectedSeverityIds: string[];
  setSelectedSeverityIds: (value: string[]) => void;
}

export const BoardTicketsSeverityFilter = ({
  severities,
  selectedSeverityIds,
  setSelectedSeverityIds
}: BoardTicketsSeverityFilterProps): ReactElement => {
  const options = buildSeveritySelectOptions(severities);
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
      label="Severity"
      labelId="board-ticket-severity-filter-label"
      onChange={setSelectedSeverityIds}
      options={mappedOptions}
      value={selectedSeverityIds}
    />
  );
};
