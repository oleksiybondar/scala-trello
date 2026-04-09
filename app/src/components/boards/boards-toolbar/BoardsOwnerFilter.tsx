import type { ReactElement } from "react";

import Autocomplete from "@mui/material/Autocomplete";
import TextField from "@mui/material/TextField";

import type { BoardsToolbarOwnerOption } from "@components/boards/boards-toolbar/types";

interface BoardsOwnerFilterProps {
  onChange: (value: string | undefined) => void;
  options: BoardsToolbarOwnerOption[];
  value: string | undefined;
}

export const BoardsOwnerFilter = ({
  onChange,
  options,
  value
}: BoardsOwnerFilterProps): ReactElement => {
  const selectedOption =
    options.find(option => option.ownerUserId === value) ??
    options.find(option => option.ownerUserId === undefined);

  return (
    <Autocomplete
      disableClearable
      getOptionLabel={option => option.label}
      onChange={(_event, nextValue) => {
        onChange(nextValue.ownerUserId);
      }}
      options={options}
      renderInput={params => (
        <TextField
          {...params}
          label="Owned by"
          size="small"
        />
      )}
      size="small"
      sx={{ minWidth: 240 }}
      value={selectedOption}
    />
  );
};
