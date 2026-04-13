import type { ReactElement } from "react";

import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import ListItemText from "@mui/material/ListItemText";
import MenuItem from "@mui/material/MenuItem";
import Select from "@mui/material/Select";

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
  return (
    <FormControl size="small" sx={{ minWidth: { md: 240, xs: "100%" } }}>
      <InputLabel id="board-ticket-assignee-filter-label">Assignees</InputLabel>
      <Select
        label="Assignees"
        labelId="board-ticket-assignee-filter-label"
        multiple
        onChange={event => {
          const value = event.target.value;

          setSelectedUserIds(typeof value === "string" ? value.split(",").filter(Boolean) : value);
        }}
        renderValue={selected => {
          if (selected.length === 0) {
            return "Everyone";
          }

          return `${String(selected.length)} selected`;
        }}
        value={selectedUserIds}
      >
        {members.map(member => (
          <MenuItem key={member.userId} value={member.userId}>
            <ListItemText
              primary={
                member.user === null
                  ? member.userId
                  : `${member.user.firstName} ${member.user.lastName}`
              }
            />
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};
