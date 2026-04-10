import type { ReactElement } from "react";

import Button from "@mui/material/Button";
import Stack from "@mui/material/Stack";

interface FormActionButtonsProps {
  isDisabled: boolean;
  applyDisabled?: boolean;
  onCancel: () => void;
  onApply: () => void;
}

export const FormActionButtons = ({
  isDisabled,
  applyDisabled = false,
  onCancel,
  onApply
}: FormActionButtonsProps): ReactElement => (
  <Stack
    direction={{ xs: "column-reverse", sm: "row" }}
    justifyContent="flex-end"
    spacing={1.5}
  >
    <Button disabled={isDisabled} onClick={onCancel} variant="outlined">
      Cancel
    </Button>
    <Button disabled={isDisabled || applyDisabled} onClick={onApply} variant="contained">
      Apply
    </Button>
  </Stack>
);
