import type { ReactElement } from "react";
import { useEffect, useMemo, useState } from "react";

import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Chip from "@mui/material/Chip";
import Link from "@mui/material/Link";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { alpha, useTheme } from "@mui/material/styles";
import type { Theme } from "@mui/material/styles";
import { Link as RouterLink } from "react-router-dom";

import { TimeInput } from "@components/time-tracking/TimeInput";
import { TimeTrackingActivitySelect } from "@components/time-tracking/TimeTrackingActivitySelect";
import { useActivities } from "@hooks/useActivities";
import { useAuth } from "@hooks/useAuth";
import { formatMinutesToTimeTrackingDuration } from "@helpers/timeTrackingConversions";
import { resolveActivityThemeColorToken } from "@helpers/timeTrackingActivities";
import type { TimeTrackingEntry } from "../../../domain/time-tracking/graphql";
import type { UpdateTimeTrackingEntryInput } from "../../../domain/time-tracking/useTimeTrackingEntryService";
import { useTimeTrackingEntryService } from "../../../domain/time-tracking/useTimeTrackingEntryService";

interface MyTimeEntriesTableProps {
  entries: TimeTrackingEntry[];
}

interface EditDraft {
  activityId: string;
  description: string;
  durationMinutes: number | null;
  loggedAt: string;
}

const padTimeSegment = (value: number): string => {
  return String(value).padStart(2, "0");
};

const toDateTimeLocalValue = (value: Date): string => {
  return [
    String(value.getFullYear()),
    padTimeSegment(value.getMonth() + 1),
    padTimeSegment(value.getDate())
  ].join("-")
    + "T"
    + [padTimeSegment(value.getHours()), padTimeSegment(value.getMinutes())].join(":");
};

const parseDateTimeLocalToIso = (value: string): string | null => {
  const parsedDate = new Date(value);

  return Number.isNaN(parsedDate.getTime()) ? null : parsedDate.toISOString();
};

const toSafeDateTimeLocalValue = (value: string): string => {
  const parsedDate = new Date(value);

  if (Number.isNaN(parsedDate.getTime())) {
    return toDateTimeLocalValue(new Date());
  }

  return toDateTimeLocalValue(parsedDate);
};

const formatLoggedAt = (value: string): string => {
  const parsedDate = new Date(value);

  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleString();
};

const resolveThemeColorByCode = (theme: Theme, code: string | null): string => {
  if (code === null) {
    return theme.palette.grey[500];
  }

  const token = resolveActivityThemeColorToken(code);

  switch (token) {
    case "error.main":
      return theme.palette.error.main;
    case "info.main":
      return theme.palette.info.main;
    case "primary.main":
      return theme.palette.primary.main;
    case "secondary.main":
      return theme.palette.secondary.main;
    case "success.main":
      return theme.palette.success.main;
    case "warning.main":
      return theme.palette.warning.main;
    default:
      return theme.palette.grey[500];
  }
};

export const MyTimeEntriesTable = ({ entries }: MyTimeEntriesTableProps): ReactElement => {
  const theme = useTheme();
  const [editingEntryId, setEditingEntryId] = useState<string | null>(null);
  const [updatingEntryId, setUpdatingEntryId] = useState<string | null>(null);
  const [deletingEntryId, setDeletingEntryId] = useState<string | null>(null);
  const [editDraft, setEditDraft] = useState<EditDraft | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [removedEntryIds, setRemovedEntryIds] = useState(new Set<string>());
  const [entryOverrides, setEntryOverrides] = useState<Record<string, TimeTrackingEntry>>({});
  const {
    activities,
    hasLoadedActivities,
    isLoadingActivities,
    loadActivities
  } = useActivities();
  const { isAuthenticated } = useAuth();
  const {
    deleteTimeEntry,
    isDeletingTimeEntry,
    isUpdatingTimeEntry,
    updateTimeEntry
  } = useTimeTrackingEntryService();

  useEffect(() => {
    if (isAuthenticated && !hasLoadedActivities && !isLoadingActivities) {
      void loadActivities();
    }
  }, [hasLoadedActivities, isAuthenticated, isLoadingActivities, loadActivities]);

  useEffect(() => {
    const availableEntryIds = new Set(entries.map(entry => entry.entryId));

    setRemovedEntryIds(currentState => {
      return new Set([...currentState].filter(entryId => availableEntryIds.has(entryId)));
    });
    setEntryOverrides(currentState => {
      return Object.fromEntries(
        Object.entries(currentState).filter(([entryId]) => availableEntryIds.has(entryId))
      );
    });
    setEditingEntryId(currentState => {
      if (currentState === null) {
        return null;
      }

      return availableEntryIds.has(currentState) ? currentState : null;
    });
    setEditDraft(currentState => {
      if (editingEntryId === null || currentState === null || !availableEntryIds.has(editingEntryId)) {
        return null;
      }

      return currentState;
    });
  }, [entries]);

  const visibleEntries = useMemo(() => {
    return entries
      .filter(entry => !removedEntryIds.has(entry.entryId))
      .map(entry => entryOverrides[entry.entryId] ?? entry)
      .sort((left, right) => {
        return right.loggedAt.localeCompare(left.loggedAt);
      });
  }, [entries, entryOverrides, removedEntryIds]);
  const activityById = useMemo(() => {
    return Object.fromEntries(activities.map(activity => [activity.activityId, activity]));
  }, [activities]);

  const handleSave = async (input: UpdateTimeTrackingEntryInput): Promise<void> => {
    setErrorMessage(null);
    setUpdatingEntryId(input.entryId);

    try {
      const updatedEntry = await updateTimeEntry(input);

      setEntryOverrides(currentState => {
        return {
          ...currentState,
          [updatedEntry.entryId]: updatedEntry
        };
      });
      setEditingEntryId(null);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Failed to update time entry.");
    } finally {
      setUpdatingEntryId(null);
    }
  };

  const handleDelete = async (entryId: string): Promise<void> => {
    setErrorMessage(null);
    setDeletingEntryId(entryId);

    try {
      await deleteTimeEntry(entryId);
      setRemovedEntryIds(currentState => {
        const nextState = new Set(currentState);

        nextState.add(entryId);
        return nextState;
      });
      if (editingEntryId === entryId) {
        setEditingEntryId(null);
      }
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Failed to delete time entry.");
    } finally {
      setDeletingEntryId(null);
    }
  };

  const startEdit = (entry: TimeTrackingEntry): void => {
    setEditingEntryId(entry.entryId);
    setEditDraft({
      activityId: entry.activityId,
      description: entry.description ?? "",
      durationMinutes: entry.durationMinutes,
      loggedAt: toSafeDateTimeLocalValue(entry.loggedAt)
    });
  };

  if (visibleEntries.length === 0) {
    return (
      <Paper sx={{ p: 2 }} variant="outlined">
        <Typography color="text.secondary" variant="body2">
          No time entries found.
        </Typography>
      </Paper>
    );
  }

  return (
    <Stack spacing={1.25}>
      {errorMessage !== null ? <Alert severity="error">{errorMessage}</Alert> : null}
      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Activity</TableCell>
              <TableCell>Time</TableCell>
              <TableCell>Logged At</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Ticket</TableCell>
              <TableCell>Board</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {visibleEntries.map(entry => {
              const isEditing = editingEntryId === entry.entryId;
              const currentDraft = isEditing ? editDraft : null;
              const isUpdating = updatingEntryId === entry.entryId && isUpdatingTimeEntry;
              const isDeleting = deletingEntryId === entry.entryId && isDeletingTimeEntry;
              const resolvedActivity = activityById[entry.activityId];
              const activityName =
                resolvedActivity?.name
                ?? entry.activityName
                ?? "Unknown activity";
              const activityCode = entry.activityCode ?? resolvedActivity?.code ?? null;
              const activityColor = resolveThemeColorByCode(theme, activityCode);
              const loggedAtIso = currentDraft === null ? null : parseDateTimeLocalToIso(currentDraft.loggedAt);
              const parsedActivityId =
                currentDraft === null ? null : Number.parseInt(currentDraft.activityId, 10);
              const canSave =
                currentDraft !== null
                && !isUpdating
                && parsedActivityId !== null
                && !Number.isNaN(parsedActivityId)
                && currentDraft.durationMinutes !== null
                && currentDraft.durationMinutes > 0
                && loggedAtIso !== null;

              return (
                <TableRow key={entry.entryId}>
                  <TableCell sx={{ minWidth: 180 }}>
                    {isEditing && currentDraft !== null ? (
                      <TimeTrackingActivitySelect
                        activities={activities}
                        disabled={isUpdating}
                        onChange={nextValue => {
                          setEditDraft(previousState => {
                            if (previousState === null) {
                              return previousState;
                            }

                            return {
                              ...previousState,
                              activityId: typeof nextValue === "string" ? nextValue : (nextValue[0] ?? "")
                            };
                          });
                        }}
                        value={currentDraft.activityId}
                      />
                    ) : (
                      <Chip
                        label={activityName}
                        size="small"
                        sx={{
                          backgroundColor: alpha(activityColor, 0.14),
                          border: `1px solid ${alpha(activityColor, 0.35)}`,
                          color: activityColor
                        }}
                        variant="outlined"
                      />
                    )}
                  </TableCell>
                  <TableCell sx={{ minWidth: 160 }}>
                    {isEditing && currentDraft !== null ? (
                      <TimeInput
                        compact
                        disabled={isUpdating}
                        onChange={value => {
                          setEditDraft(previousState => {
                            if (previousState === null) {
                              return previousState;
                            }

                            return {
                              ...previousState,
                              durationMinutes: value
                            };
                          });
                        }}
                        showFormatHint={false}
                        value={currentDraft.durationMinutes}
                      />
                    ) : (
                      formatMinutesToTimeTrackingDuration(Math.max(0, entry.durationMinutes))
                    )}
                  </TableCell>
                  <TableCell sx={{ minWidth: 160 }}>
                    {isEditing && currentDraft !== null ? (
                      <TextField
                        error={loggedAtIso === null}
                        fullWidth
                        onChange={event => {
                          const { value } = event.target;

                          setEditDraft(previousState => {
                            if (previousState === null) {
                              return previousState;
                            }

                            return {
                              ...previousState,
                              loggedAt: value
                            };
                          });
                        }}
                        size="small"
                        type="datetime-local"
                        value={currentDraft.loggedAt}
                      />
                    ) : (
                      <Typography variant="body2">{formatLoggedAt(entry.loggedAt)}</Typography>
                    )}
                  </TableCell>
                  <TableCell sx={{ minWidth: 220 }}>
                    {isEditing && currentDraft !== null ? (
                      <TextField
                        fullWidth
                        multiline
                        onChange={event => {
                          const { value } = event.target;

                          setEditDraft(previousState => {
                            if (previousState === null) {
                              return previousState;
                            }

                            return {
                              ...previousState,
                              description: value
                            };
                          });
                        }}
                        size="small"
                        value={currentDraft.description}
                      />
                    ) : (
                      <Typography color={entry.description === null ? "text.secondary" : "text.primary"} variant="body2">
                        {entry.description ?? "No description"}
                      </Typography>
                    )}
                  </TableCell>
                  <TableCell>
                    <Link
                      component={RouterLink}
                      to={`/tickets/${entry.ticketId}`}
                      underline="hover"
                      variant="body2"
                    >
                      {entry.ticket?.title ?? `Ticket #${entry.ticketId}`}
                    </Link>
                  </TableCell>
                  <TableCell>
                    {entry.ticket?.board !== null && entry.ticket?.board !== undefined ? (
                      <Link
                        color="text.secondary"
                        component={RouterLink}
                        to={`/boards/${entry.ticket.board.boardId}`}
                        underline="hover"
                        variant="body2"
                      >
                        {entry.ticket.board.title}
                      </Link>
                    ) : (
                      <Typography color="text.secondary" variant="body2">
                        -
                      </Typography>
                    )}
                  </TableCell>
                  <TableCell align="right">
                    <Stack direction="row" justifyContent="flex-end" spacing={1}>
                      {isEditing && currentDraft !== null ? (
                        <>
                          <Button
                            disabled={isUpdating}
                            onClick={() => {
                              setEditingEntryId(null);
                              setEditDraft(null);
                            }}
                            size="small"
                            variant="outlined"
                          >
                            Cancel
                          </Button>
                          <Button
                            disabled={!canSave}
                            onClick={() => {
                              if (!canSave) {
                                return;
                              }

                              const safeDurationMinutes = currentDraft.durationMinutes;

                              if (safeDurationMinutes === null) {
                                return;
                              }

                              void handleSave({
                                activityId: parsedActivityId,
                                description: currentDraft.description,
                                durationMinutes: safeDurationMinutes,
                                entryId: entry.entryId,
                                loggedAt: loggedAtIso
                              });
                            }}
                            size="small"
                            variant="contained"
                          >
                            {isUpdating ? "Saving..." : "Save"}
                          </Button>
                        </>
                      ) : (
                        <>
                          <Button
                            onClick={() => {
                              startEdit(entry);
                            }}
                            size="small"
                            variant="outlined"
                          >
                            Edit
                          </Button>
                          <Button
                            color="error"
                            disabled={isDeleting}
                            onClick={() => {
                              void handleDelete(entry.entryId);
                            }}
                            size="small"
                            variant="outlined"
                          >
                            {isDeleting ? "Deleting..." : "Delete"}
                          </Button>
                        </>
                      )}
                    </Stack>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>
    </Stack>
  );
};
