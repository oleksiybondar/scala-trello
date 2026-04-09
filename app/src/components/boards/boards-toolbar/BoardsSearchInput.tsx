import type { ChangeEvent, ReactElement } from "react";

import TextField from "@mui/material/TextField";

interface BoardsSearchInputProps {
  onChange: (value: string) => void;
  value: string;
}

export const BoardsSearchInput = ({
  onChange,
  value
}: BoardsSearchInputProps): ReactElement => {
  const handleChange = (event: ChangeEvent<HTMLInputElement>): void => {
    onChange(event.target.value);
  };

  return (
    <TextField
      fullWidth
      label="Search boards"
      onChange={handleChange}
      size="small"
      value={value}
    />
  );
};
