import { useState } from "react";
import type { MouseEvent, ReactElement } from "react";

import ButtonBase from "@mui/material/ButtonBase";
import Menu from "@mui/material/Menu";
import MenuItem from "@mui/material/MenuItem";
import Box from "@mui/material/Box";

import { Person } from "@components/avatar/Person";

import type { BoardMember } from "../../../domain/board/graphql";

interface TicketAssigneeMenuProps {
  assignedTo: BoardMember["user"] | null;
  assignedToUserId: string | null;
  members: BoardMember[];
  onReassign: (assignedToUserId: string | null) => void;
}

export const TicketAssigneeMenu = ({
  assignedTo,
  assignedToUserId,
  members,
  onReassign
}: TicketAssigneeMenuProps): ReactElement => {
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);

  const handleOpen = (event: MouseEvent<HTMLButtonElement>): void => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = (): void => {
    setAnchorEl(null);
  };

  const handleReassign = (nextAssignedToUserId: string | null): void => {
    onReassign(nextAssignedToUserId);
    handleClose();
  };

  return (
    <>
      <ButtonBase
        onClick={handleOpen}
        sx={{
          borderRadius: 1,
          display: "block",
          flex: 1,
          minWidth: 0,
          textAlign: "left"
        }}
      >
        <Box sx={{ minWidth: 0 }}>
          <Person fallbackLabel="Unassigned" person={assignedTo} />
        </Box>
      </ButtonBase>
      <Menu anchorEl={anchorEl} onClose={handleClose} open={anchorEl !== null}>
        <MenuItem
          disabled={assignedToUserId === null}
          onClick={() => {
            handleReassign(null);
          }}
        >
          Unassigned
        </MenuItem>
        {members.map(member => (
          <MenuItem
            disabled={member.userId === assignedToUserId}
            key={member.userId}
            onClick={() => {
              handleReassign(member.userId);
            }}
          >
            {member.user === null
              ? member.userId
              : `${member.user.firstName} ${member.user.lastName}`}
          </MenuItem>
        ))}
      </Menu>
    </>
  );
};
