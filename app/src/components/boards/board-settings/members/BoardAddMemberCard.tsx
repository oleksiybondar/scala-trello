import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import MenuItem from "@mui/material/MenuItem";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { useInviteBoardMemberMutation } from "@features/board/useInviteBoardMemberMutation";
import { useBoard } from "@hooks/useBoard";
import { useRoles } from "@hooks/useRoles";
import { formatRoleLabel } from "./formatRoleLabel";

export const BoardAddMemberCard = (): ReactElement => {
  const { boardId = "" } = useParams();
  const { boardPermissionAccess } = useBoard();
  const { isLoadingRoles, roles } = useRoles();
  const inviteBoardMemberMutation = useInviteBoardMemberMutation();
  const [userQuery, setUserQuery] = useState("");
  const [selectedRoleId, setSelectedRoleId] = useState("");
  const [isTouched, setIsTouched] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (selectedRoleId.length === 0 && roles.length > 0) {
      setSelectedRoleId(roles[0]?.roleId ?? "");
    }
  }, [roles, selectedRoleId]);

  const trimmedUserQuery = userQuery.trim();
  const isUserError = isTouched && trimmedUserQuery.length === 0;
  const isRoleError = isTouched && selectedRoleId.length === 0;
  const isDisabled =
    boardId.length === 0 ||
    inviteBoardMemberMutation.isPending ||
    isLoadingRoles ||
    !boardPermissionAccess.canCreate;

  if (!boardPermissionAccess.canCreate) {
    return <></>;
  }

  const handleCancel = (): void => {
    setUserQuery("");
    setIsTouched(false);
    setErrorMessage(null);
    setSelectedRoleId(roles[0]?.roleId ?? "");
  };

  const handleInvite = async (): Promise<void> => {
    setIsTouched(true);

    if (isDisabled || trimmedUserQuery.length === 0 || selectedRoleId.length === 0) {
      return;
    }

    setErrorMessage(null);

    try {
      await inviteBoardMemberMutation.mutateAsync({
        boardId,
        roleId: selectedRoleId,
        user: trimmedUserQuery
      });
      handleCancel();
    } catch (error: unknown) {
      setErrorMessage(
        error instanceof Error ? error.message : "Failed to invite the board member."
      );
    }
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack padding={3} spacing={3}>
          {errorMessage !== null ? <Alert severity="error">{errorMessage}</Alert> : null}
          <Stack spacing={1}>
            <Typography variant="h5">Add New Member</Typography>
            <Typography color="textSecondary" variant="body2">
              Invite a board member by username or email and assign their role.
            </Typography>
          </Stack>

          <TextField
            disabled={isDisabled}
            error={isUserError}
            fullWidth
            helperText={isUserError ? "Username or email is required." : " "}
            label="Username or email"
            onChange={(event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
              setIsTouched(true);
              setUserQuery(event.target.value);
            }}
            required
            value={userQuery}
          />

          <TextField
            disabled={isDisabled}
            error={isRoleError}
            fullWidth
            helperText={
              isLoadingRoles
                ? "Loading roles..."
                : isRoleError
                  ? "Role is required."
                  : " "
            }
            label="Role"
            onChange={(event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
              setIsTouched(true);
              setSelectedRoleId(event.target.value);
            }}
            required
            select
            value={selectedRoleId}
          >
            {roles.map(role => (
              <MenuItem key={role.roleId} value={role.roleId}>
                {formatRoleLabel(role.roleName)}
              </MenuItem>
            ))}
          </TextField>

          <Stack
            direction={{ xs: "column-reverse", sm: "row" }}
            justifyContent="flex-end"
            spacing={1.5}
          >
            <Button disabled={isDisabled} onClick={handleCancel} variant="outlined">
              Cancel
            </Button>
            <Button
              disabled={isDisabled || trimmedUserQuery.length === 0 || selectedRoleId.length === 0}
              onClick={() => {
                void handleInvite();
              }}
              variant="contained"
            >
              Invite member
            </Button>
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  );
};
