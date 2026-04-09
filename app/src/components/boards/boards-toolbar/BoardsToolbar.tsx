import type { ReactElement } from "react";

import Stack from "@mui/material/Stack";

import { BoardsOwnerFilter } from "@components/boards/boards-toolbar/BoardsOwnerFilter";
import { BoardsSearchInput } from "@components/boards/boards-toolbar/BoardsSearchInput";
import { CreateBoardToolbarButton } from "@components/boards/boards-toolbar/CreateBoardToolbarButton";
import type { BoardsToolbarOwnerOption } from "@components/boards/boards-toolbar/types";
import { useBoards } from "@hooks/useBoards";
import { useCurrentUser } from "@hooks/useCurrentUser";

interface BoardsToolbarProps {
  onCreateBoard: () => void;
}

export const BoardsToolbar = ({ onCreateBoard }: BoardsToolbarProps): ReactElement => {
  const { currentParams, ownerOptions, queryBoards } = useBoards();
  const { userId } = useCurrentUser();
  const toolbarOwnerOptions: BoardsToolbarOwnerOption[] = [
    {
      label: "Any"
    },
    ...(userId === null
      ? []
      : [
          {
            label: "Me",
            ownerUserId: userId
          }
        ]),
    ...ownerOptions
      .filter(option => option.ownerUserId !== userId)
      .map(option => ({
        label: option.label,
        ownerUserId: option.ownerUserId
      }))
  ];

  return (
    <Stack
      alignItems={{ md: "center", xs: "stretch" }}
      direction={{ md: "row", xs: "column" }}
      justifyContent="space-between"
      spacing={1.5}
    >
      <Stack
        direction={{ sm: "row", xs: "column" }}
        spacing={1.5}
        sx={{ flex: 1 }}
      >
        <BoardsSearchInput
          onChange={value => {
            queryBoards({
              keyword: value.length > 0 ? value : undefined,
              page: 1
            });
          }}
          value={currentParams.keyword ?? ""}
        />
        <BoardsOwnerFilter
          onChange={value => {
            queryBoards({
              owner: value,
              page: 1
            });
          }}
          options={toolbarOwnerOptions}
          value={currentParams.owner}
        />
      </Stack>
      <CreateBoardToolbarButton onClick={onCreateBoard} />
    </Stack>
  );
};
