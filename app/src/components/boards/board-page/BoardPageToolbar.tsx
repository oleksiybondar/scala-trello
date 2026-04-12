import type { ReactElement } from "react";
import { useState } from "react";

import AddOutlinedIcon from "@mui/icons-material/AddOutlined";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { CreateTicketDialog } from "@components/tickets/CreateTicketDialog";
import { useTickets } from "@hooks/useTickets";

export const BoardPageToolbar = (): ReactElement => {
  const [isCreateTicketDialogOpen, setIsCreateTicketDialogOpen] = useState(false);
  const { ticketsCount } = useTickets();

  return (
    <>
      <Paper
        sx={{
          minHeight: 56,
          px: 2,
          py: 1.5
        }}
        variant="outlined"
      >
        <Stack
          alignItems={{ md: "center", xs: "flex-start" }}
          direction={{ md: "row", xs: "column" }}
          justifyContent="space-between"
          spacing={1.5}
        >
          <Stack
            alignItems={{ md: "center", xs: "flex-start" }}
            direction="row"
            spacing={1}
          >
            <Typography variant="subtitle2">Board tickets</Typography>
            <Chip
              color="primary"
              label={`${String(ticketsCount)} tickets`}
              size="small"
              variant="outlined"
            />
          </Stack>

          <Button
            onClick={() => {
              setIsCreateTicketDialogOpen(true);
            }}
            startIcon={<AddOutlinedIcon />}
            variant="contained"
          >
            New ticket
          </Button>
        </Stack>
      </Paper>

      <CreateTicketDialog
        onClose={() => {
          setIsCreateTicketDialogOpen(false);
        }}
        open={isCreateTicketDialogOpen}
      />
    </>
  );
};
