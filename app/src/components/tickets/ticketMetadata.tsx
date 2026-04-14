import ErrorOutlineOutlinedIcon from "@mui/icons-material/ErrorOutlineOutlined";
import KeyboardDoubleArrowDownOutlinedIcon from "@mui/icons-material/KeyboardDoubleArrowDownOutlined";
import LocalFireDepartmentOutlinedIcon from "@mui/icons-material/LocalFireDepartmentOutlined";
import OutlinedFlagIcon from "@mui/icons-material/OutlinedFlag";
import RemoveOutlinedIcon from "@mui/icons-material/RemoveOutlined";
import ReportProblemOutlinedIcon from "@mui/icons-material/ReportProblemOutlined";
import type { SvgIconComponent } from "@mui/icons-material";
import type { Theme } from "@mui/material/styles";

export type TicketMetadataTone = "error" | "grey" | "info" | "warning";

export interface TicketPriorityMeta {
  description: string;
  icon: SvgIconComponent;
  key: number;
  label: string;
  tone: TicketMetadataTone;
}

export interface TicketSeverityMeta {
  icon: SvgIconComponent;
  label: string;
  tone: TicketMetadataTone;
}

export const PRIORITY_OPTIONS: TicketPriorityMeta[] = [
  {
    description: "Drop everything",
    icon: LocalFireDepartmentOutlinedIcon,
    key: 0,
    label: "0 - Critical now",
    tone: "warning"
  },
  {
    description: "Needs action today",
    icon: LocalFireDepartmentOutlinedIcon,
    key: 1,
    label: "1 - Immediate",
    tone: "warning"
  },
  {
    description: "Very important",
    icon: LocalFireDepartmentOutlinedIcon,
    key: 2,
    label: "2 - Urgent",
    tone: "warning"
  },
  {
    description: "High value next",
    icon: OutlinedFlagIcon,
    key: 3,
    label: "3 - High",
    tone: "info"
  },
  {
    description: "Above normal",
    icon: OutlinedFlagIcon,
    key: 4,
    label: "4 - Medium-high",
    tone: "info"
  },
  {
    description: "Default planning lane",
    icon: OutlinedFlagIcon,
    key: 5,
    label: "5 - Normal",
    tone: "info"
  },
  {
    description: "Can wait a bit",
    icon: OutlinedFlagIcon,
    key: 6,
    label: "6 - Planned",
    tone: "info"
  },
  {
    description: "Later in the cycle",
    icon: OutlinedFlagIcon,
    key: 7,
    label: "7 - Later",
    tone: "info"
  },
  {
    description: "Low urgency",
    icon: KeyboardDoubleArrowDownOutlinedIcon,
    key: 8,
    label: "8 - Low",
    tone: "grey"
  },
  {
    description: "Backlog material",
    icon: KeyboardDoubleArrowDownOutlinedIcon,
    key: 9,
    label: "9 - Backlog",
    tone: "grey"
  }
];

export const DEFAULT_PRIORITY_OPTION: TicketPriorityMeta = {
  description: "Default planning lane",
  icon: OutlinedFlagIcon,
  key: 5,
  label: "5 - Normal",
  tone: "info"
};

export const resolveMetadataToneColor = (
  tone: TicketMetadataTone,
  palette: Theme["palette"]
): string => {
  if (tone === "error") {
    return palette.error.main;
  }

  if (tone === "warning") {
    return palette.warning.main;
  }

  if (tone === "info") {
    return palette.info.main;
  }

  return palette.grey[500];
};

export const getPriorityMeta = (priority: number | null): TicketPriorityMeta | null => {
  if (priority === null) {
    return null;
  }

  return PRIORITY_OPTIONS.find(option => option.key === priority) ?? DEFAULT_PRIORITY_OPTION;
};

export const getSeverityMeta = (severityName: string | null): TicketSeverityMeta | null => {
  if (severityName === null || severityName.trim().length === 0) {
    return null;
  }

  const normalizedName = severityName.trim().toLowerCase();

  if (normalizedName === "major" || normalizedName === "critical" || normalizedName === "blocker") {
    return {
      icon: ReportProblemOutlinedIcon,
      label: "Major",
      tone: "error"
    };
  }

  if (normalizedName === "minor" || normalizedName === "low" || normalizedName === "trivial") {
    return {
      icon: RemoveOutlinedIcon,
      label: normalizedName.charAt(0).toUpperCase() + normalizedName.slice(1),
      tone: "grey"
    };
  }

  return {
    icon: ErrorOutlineOutlinedIcon,
    label: normalizedName.charAt(0).toUpperCase() + normalizedName.slice(1),
    tone: "info"
  };
};
