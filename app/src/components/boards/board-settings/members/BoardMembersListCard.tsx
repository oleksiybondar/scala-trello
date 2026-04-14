import type { ChangeEvent, ReactElement } from "react";
import { useMemo, useState } from "react";

import Alert from "@mui/material/Alert";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Chip from "@mui/material/Chip";
import Divider from "@mui/material/Divider";
import IconButton from "@mui/material/IconButton";
import MenuItem from "@mui/material/MenuItem";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import SaveOutlinedIcon from "@mui/icons-material/SaveOutlined";

import { Person } from "@components/avatar/Person";
import { useBoard } from "@hooks/useBoard";
import { useCurrentUser } from "@hooks/useCurrentUser";
import { useRoles } from "@hooks/useRoles";
import { formatRoleLabel } from "./formatRoleLabel";

export const BoardMembersListCard = (): ReactElement => {
  const {
    boardPermissionAccess,
    changeBoardMemberRole,
    isLoadingMembers,
    isRemovingBoardMember,
    isUpdatingBoardMemberRole,
    members,
    membersError,
    removeBoardMember
  } = useBoard();
  const { userId: currentUserId } = useCurrentUser();
  const { roles } = useRoles();
  const [editingUserId, setEditingUserId] = useState<string | null>(null);
  const [draftRoleId, setDraftRoleId] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const roleOptions = useMemo(() => {
    return roles.map(role => ({
      label: formatRoleLabel(role.roleName),
      roleId: role.roleId
    }));
  }, [roles]);

  const handleStartEditing = (userId: string, roleId: string): void => {
    setEditingUserId(userId);
    setDraftRoleId(roleId);
    setErrorMessage(null);
  };

  const handleRoleDraftChange = (
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    setDraftRoleId(event.target.value);
  };

  const handleSaveRole = async (userId: string, roleId: string): Promise<void> => {
    if (roleId.length === 0 || !boardPermissionAccess.canModify) {
      return;
    }

    setErrorMessage(null);

    try {
      await changeBoardMemberRole(userId, roleId);
      setEditingUserId(null);
      setDraftRoleId("");
    } catch (error: unknown) {
      setErrorMessage(
        error instanceof Error ? error.message : "Failed to update the member role."
      );
    }
  };

  const handleRemoveMember = async (userId: string): Promise<void> => {
    if (!boardPermissionAccess.canDelete) {
      setErrorMessage("You do not have permission to remove board members.");
      return;
    }

    if (currentUserId === userId) {
      setErrorMessage("You cannot remove yourself from the board.");
      return;
    }

    if (members.length <= 1) {
      setErrorMessage("The last board member cannot be removed.");
      return;
    }

    setErrorMessage(null);

    try {
      await removeBoardMember(userId);
    } catch (error: unknown) {
      setErrorMessage(
        error instanceof Error ? error.message : "Failed to remove the board member."
      );
    }
  };

  if (!boardPermissionAccess.canRead) {
    return <></>;
  }

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack padding={3} spacing={3}>
          {errorMessage !== null ? <Alert severity="error">{errorMessage}</Alert> : null}
          {membersError !== null ? <Alert severity="error">{membersError.message}</Alert> : null}
          <Stack spacing={1}>
            <Typography variant="h5">Members</Typography>
            <Typography color="textSecondary" variant="body2">
              Review current board members and their assigned roles.
            </Typography>
          </Stack>

          {isLoadingMembers ? (
            <Typography color="text.secondary" variant="body2">
              Loading members...
            </Typography>
          ) : null}

          {!isLoadingMembers && members.length === 0 ? (
            <Typography color="text.secondary" variant="body2">
              No members found for this board.
            </Typography>
          ) : null}

          {!isLoadingMembers && members.length > 0 ? (
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
                  <Stack alignItems="center" direction="row" spacing={1}>
                    {editingUserId === member.userId ? (
                      <TextField
                        onChange={handleRoleDraftChange}
                        select
                        size="small"
                        sx={{ minWidth: 160 }}
                        value={draftRoleId}
                      >
                        {roleOptions.map(role => (
                          <MenuItem key={role.roleId} value={role.roleId}>
                            {role.label}
                          </MenuItem>
                        ))}
                      </TextField>
                    ) : (
                      <Chip
                        label={formatRoleLabel(member.role.roleName)}
                        size="small"
                        variant="outlined"
                      />
                    )}
                    <IconButton
                      aria-label={
                        editingUserId === member.userId ? "Save member role" : "Edit member role"
                      }
                      disabled={
                        !boardPermissionAccess.canModify ||
                        isUpdatingBoardMemberRole ||
                        (editingUserId === member.userId && draftRoleId.length === 0)
                      }
                      onClick={() => {
                        if (editingUserId === member.userId) {
                          void handleSaveRole(member.userId, draftRoleId);
                        } else {
                          handleStartEditing(member.userId, member.role.roleId);
                        }
                      }}
                      size="small"
                    >
                      {editingUserId === member.userId ? (
                        <SaveOutlinedIcon fontSize="small" />
                      ) : (
                        <EditOutlinedIcon fontSize="small" />
                      )}
                    </IconButton>
                    <IconButton
                      aria-label="Remove member"
                      disabled={!boardPermissionAccess.canDelete || isRemovingBoardMember}
                      onClick={() => {
                        void handleRemoveMember(member.userId);
                      }}
                      size="small"
                    >
                      <DeleteOutlineIcon fontSize="small" />
                    </IconButton>
                  </Stack>
                </Stack>
              ))}
            </Stack>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  );
};
