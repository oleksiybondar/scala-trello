import type { ReactElement } from "react";

import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Divider from "@mui/material/Divider";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { useCurrentUserProfileQuery } from "@features/user/useCurrentUserProfileQuery";
import { useAuth } from "@hooks/useAuth";
import { useCurrentUser } from "@hooks/useCurrentUser";

export const UserProfileSettingsPanel = (): ReactElement => {
  const { accessToken } = useAuth();
  const { userId } = useCurrentUser();
  const currentUserProfileQuery = useCurrentUserProfileQuery();
  const canLoadProfile = userId !== null && accessToken !== null;
  const helperText = !canLoadProfile
    ? "Sign in to load the current user profile."
    : currentUserProfileQuery.isPending
      ? "Loading display name from GraphQL."
      : currentUserProfileQuery.isError
        ? "Unable to load the user profile right now."
        : "Loaded from the current user GraphQL profile.";

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack spacing={3}>
          <Stack spacing={1}>
            <Typography color="primary" variant="overline">
              User Settings
            </Typography>
            <Typography variant="h3">Profile</Typography>
            <Typography color="textSecondary" variant="body1">
              Keep identity fields here so profile editing can grow around real
              user data without changing the page shell.
            </Typography>
          </Stack>

          <Divider />

          <Stack spacing={2}>
            <TextField
              disabled
              fullWidth
              helperText={helperText}
              label="Display name"
              slotProps={{
                input: {
                  readOnly: true
                }
              }}
              value={currentUserProfileQuery.data?.displayName ?? ""}
            />
            <Typography color="textSecondary" variant="body2">
              This section is ready for GraphQL-backed profile forms and profile
              metadata fields.
            </Typography>
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  );
};
