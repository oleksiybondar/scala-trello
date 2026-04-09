import type { ReactElement } from "react";

import AddIcon from "@mui/icons-material/Add";
import Button from "@mui/material/Button";

interface CreateBoardToolbarButtonProps {
  onClick: () => void;
}

export const CreateBoardToolbarButton = ({
  onClick
}: CreateBoardToolbarButtonProps): ReactElement => {
  return (
    <Button
      onClick={onClick}
      startIcon={<AddIcon />}
      variant="contained"
    >
      New board
    </Button>
  );
};
