import type { ChangeEvent, ReactElement } from "react";
import { useRef, useState } from "react";

import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import PersonOutlineIcon from "@mui/icons-material/PersonOutline";
import Avatar from "@mui/material/Avatar";
import Button from "@mui/material/Button";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import { readFileAsDataUrl } from "@helpers/readFileAsDataUrl";

const MAX_AVATAR_FILE_SIZE_BYTES = 1_500_000;

interface AvatarInputProps {
  disabled?: boolean;
  helperText?: string;
  label?: string;
  onChange?: (nextValue: string) => void;
  value: string;
}

export const AvatarInput = ({
  disabled = false,
  helperText,
  label = "Avatar",
  onChange,
  value
}: AvatarInputProps): ReactElement => {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const hasAvatar = value.trim().length > 0;

  const handleFileChange = async (
    event: ChangeEvent<HTMLInputElement>
  ): Promise<void> => {
    const selectedFile = event.target.files?.[0];

    event.target.value = "";

    if (selectedFile === undefined) {
      return;
    }

    if (!selectedFile.type.startsWith("image/")) {
      setErrorMessage("Please select an image file.");
      return;
    }

    if (selectedFile.size > MAX_AVATAR_FILE_SIZE_BYTES) {
      setErrorMessage("Avatar image must be 1.5 MB or smaller.");
      return;
    }

    try {
      const nextValue = await readFileAsDataUrl(selectedFile);

      setErrorMessage(null);
      onChange?.(nextValue);
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Failed to process the selected image."
      );
    }
  };

  return (
    <Stack spacing={2}>
      <Typography variant="subtitle2">{label}</Typography>

      <Stack alignItems="center" direction={{ xs: "column", sm: "row" }} spacing={2}>
        <Avatar
          alt="Avatar preview"
          src={hasAvatar ? value : undefined}
          sx={{
            bgcolor: hasAvatar ? undefined : "action.selected",
            height: 96,
            width: 96
          }}
        >
          {hasAvatar ? null : <PersonOutlineIcon fontSize="large" />}
        </Avatar>

        <Stack direction={{ xs: "column", sm: "row" }} spacing={1.5}>
          <Button
            disabled={disabled}
            onClick={() => {
              fileInputRef.current?.click();
            }}
            startIcon={<EditOutlinedIcon />}
            variant="outlined"
          >
            Edit
          </Button>

          <Button
            disabled={disabled || !hasAvatar}
            onClick={() => {
              setErrorMessage(null);
              onChange?.("");
            }}
            startIcon={<DeleteOutlineIcon />}
            variant="outlined"
          >
            Remove
          </Button>
        </Stack>
      </Stack>

      <Typography color={errorMessage === null ? "textSecondary" : "error"} variant="body2">
        {errorMessage ??
          helperText ??
          "Choose an image file up to 1.5 MB. The selected file is stored locally as a data URL."}
      </Typography>

      <input
        accept="image/*"
        hidden
        onChange={event => {
          void handleFileChange(event);
        }}
        ref={fileInputRef}
        type="file"
      />
    </Stack>
  );
};
