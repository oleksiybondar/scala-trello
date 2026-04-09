import type { ReactElement } from "react";
import { useParams } from "react-router-dom";

import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Chip from "@mui/material/Chip";
import Divider from "@mui/material/Divider";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { Person } from "@components/avatar/Person";
import { useBoardMembersQuery } from "@features/board/useBoardMembersQuery";

export const BoardMembersListCard = (): ReactElement => {
  const { boardId = "" } = useParams();
  const { data: members = [], isLoading } = useBoardMembersQuery(boardId);

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack padding={3} spacing={3}>
          <Stack spacing={1}>
            <Typography variant="h5">Members</Typography>
            <Typography color="textSecondary" variant="body2">
              Review current board members and their assigned roles.
            </Typography>
          </Stack>

          {isLoading ? (
            <Typography color="text.secondary" variant="body2">
              Loading members...
            </Typography>
          ) : null}

          {!isLoading && members.length === 0 ? (
            <Typography color="text.secondary" variant="body2">
              No members found for this board.
            </Typography>
          ) : null}

          {!isLoading && members.length > 0 ? (
            <Stack divider={<Divider />} spacing={2}>
              {members.map(member => (
                <Stack
                  alignItems={{ sm: "center" }}
                  direction={{ xs: "column", sm: "row" }}
                  justifyContent="space-between"
                  key={member.userId}
                  spacing={2}
                >
                  <Person fallbackLabel="Unknown member" person={member.user} />
                  <Chip label={member.role.roleName} size="small" variant="outlined" />
                </Stack>
              ))}
            </Stack>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  );
};
