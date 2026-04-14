import type { ReactElement, ReactNode } from "react";

import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import MenuItem from "@mui/material/MenuItem";
import Select from "@mui/material/Select";

export interface BoardTicketsMultiSelectOption<TValue extends string | number> {
  label: string;
  menuContent?: ReactNode | undefined;
  value: TValue;
}

interface BoardTicketsMultiSelectFilterProps<TValue extends string | number> {
  emptyLabel: string;
  label: string;
  labelId: string;
  onChange: (value: TValue[]) => void;
  options: BoardTicketsMultiSelectOption<TValue>[];
  sxMinWidth?: number | undefined;
  value: TValue[];
}

export const BoardTicketsMultiSelectFilter = <TValue extends string | number>({
  emptyLabel,
  label,
  labelId,
  onChange,
  options,
  sxMinWidth = 180,
  value
}: BoardTicketsMultiSelectFilterProps<TValue>): ReactElement => {
  const optionByValue = new Map(options.map(option => [String(option.value), option]));

  return (
    <FormControl size="small" sx={{ minWidth: { md: sxMinWidth, xs: "100%" } }}>
      <InputLabel id={labelId}>{label}</InputLabel>
      <Select
        label={label}
        labelId={labelId}
        multiple
        onChange={event => {
          const rawValue = event.target.value;
          const rawItems = typeof rawValue === "string" ? rawValue.split(",").filter(Boolean) : rawValue;
          const nextValue = rawItems.flatMap(item => {
            const option = optionByValue.get(String(item));

            return option === undefined ? [] : [option.value];
          });

          onChange(nextValue);
        }}
        renderValue={selected => {
          if (selected.length === 0) {
            return emptyLabel;
          }

          if (selected.length === 1) {
            const selectedOption = optionByValue.get(String(selected[0]));

            return selectedOption?.label ?? emptyLabel;
          }

          return `${String(selected.length)} selected`;
        }}
        value={value}
      >
        {options.map(option => (
          <MenuItem key={String(option.value)} value={option.value}>
            {option.menuContent ?? option.label}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};
