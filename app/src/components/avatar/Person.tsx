import type { ReactElement } from "react";

import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

import { AppAvatar } from "@components/avatar/AppAvatar";
import type { BoardUserSummary } from "../../domain/board/graphql";

interface BoardPersonProps {
  fallbackLabel: string;
  person: BoardUserSummary | null;
}

const getPersonNameParts = (
  person: BoardUserSummary | null,
  fallbackLabel: string
): { firstLine: string; secondLine: string } => {
  if (person === null) {
    return {
      firstLine: fallbackLabel,
      secondLine: ""
    };
  }

  return {
    firstLine: person.firstName,
    secondLine: person.lastName
  };
};

export const Person = ({
  fallbackLabel,
  person
}: BoardPersonProps): ReactElement => {
  const { firstLine, secondLine } = getPersonNameParts(person, fallbackLabel);
  const fullName = `${firstLine} ${secondLine}`.trim();

  return (
    <Stack alignItems="center" direction="row" spacing={1} sx={{ minWidth: 0 }}>
      <AppAvatar
        avatarUrl={person?.avatarUrl}
        firstName={person?.firstName}
        label={fullName}
        lastName={person?.lastName}
        size="small"
        tone="muted"
      />
      <Stack spacing={0} sx={{ minWidth: 0 }}>
        <Typography
          sx={{
            lineHeight: 1.2,
            overflow: "hidden",
            textOverflow: "ellipsis",
            whiteSpace: "nowrap"
          }}
          variant="body2"
        >
          {firstLine}
        </Typography>
        <Typography
          color="text.secondary"
          sx={{
            lineHeight: 1.2,
            overflow: "hidden",
            textOverflow: "ellipsis",
            whiteSpace: "nowrap"
          }}
          variant="caption"
        >
          {secondLine}
        </Typography>
      </Stack>
    </Stack>
  );
};
