import type { ReactElement } from "react";

import { Person } from "@components/avatar/Person";
import { BoardTicketsMultiSelectFilter } from "@components/boards/board-page/BoardTicketsMultiSelectFilter";
import type { BoardMember } from "../../../domain/board/graphql";

interface BoardTicketsAssigneeFilterProps {
  members: BoardMember[];
  selectedUserIds: string[];
  setSelectedUserIds: (value: string[]) => void;
}

export const BoardTicketsAssigneeFilter = ({
  members,
  selectedUserIds,
  setSelectedUserIds
}: BoardTicketsAssigneeFilterProps): ReactElement => {
  const options = members.map(member => {
    const label =
      member.user === null
        ? member.userId
        : `${member.user.firstName} ${member.user.lastName}`;

    return {
      label,
      menuContent: <Person fallbackLabel={member.userId} person={member.user} />,
      value: member.userId
    };
  });

  return (
    <BoardTicketsMultiSelectFilter
      emptyLabel="Everyone"
      label="Assignees"
      labelId="board-ticket-assignee-filter-label"
      onChange={setSelectedUserIds}
      options={options}
      sxMinWidth={240}
      value={selectedUserIds}
    />
  );
};
