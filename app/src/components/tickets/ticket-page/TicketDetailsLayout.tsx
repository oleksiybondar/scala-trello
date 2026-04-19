import type { ChangeEvent, ReactElement } from "react";
import { useEffect, useState } from "react";

import AccessTimeRoundedIcon from "@mui/icons-material/AccessTimeRounded";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Grid from "@mui/material/Grid";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { Person } from "@components/avatar/Person";
import {
  TimeVelocityChart,
  type TimeVelocityData
} from "@components/charts/TimeVelocityChart";
import { TimeTrackingActivityPieChart } from "@components/time-tracking/TimeTrackingActivityPieChart";
import { TimeInput } from "@components/time-tracking/TimeInput";
import { TicketActivityEntryList } from "@components/tickets/ticket-page/TicketActivityEntryList";
import { TicketPrioritySelect } from "@components/tickets/TicketPrioritySelect";
import { TicketSeveritySelect } from "@components/tickets/TicketSeveritySelect";
import type { Ticket } from "../../../domain/ticket/graphql";
import { useActivities } from "@hooks/useActivities";
import { useTicket } from "@hooks/useTicket";
import { useTimeTrackingActivityChart } from "@hooks/useTimeTrackingActivityChart";
import { useTimeTracking } from "@hooks/useTimeTracking";

interface TicketDetailsLayoutProps {
  ticket: Ticket;
}

const getTimeVelocityData = (ticket: Ticket): TimeVelocityData => {
  const estimatedMinutes = ticket.estimatedMinutes ?? 0;
  const sortedEntries = [...ticket.timeEntries].sort((left, right) => {
    return (
      new Date(left.loggedAt).getTime() - new Date(right.loggedAt).getTime()
    );
  });
  const actualSeriesMinutes: number[] = [];
  let runningActualMinutes = 0;

  sortedEntries.forEach(entry => {
    runningActualMinutes += Math.max(0, entry.durationMinutes);
    actualSeriesMinutes.push(runningActualMinutes);
  });

  return {
    actualSeriesMinutes: actualSeriesMinutes.length > 0 ? actualSeriesMinutes : [0],
    estimatedMinutes
  };
};

const normalizeOptionalText = (value: string): string | null => {
  const normalized = value.trim();

  return normalized.length === 0 ? null : normalized;
};

export const TicketDetailsLayout = ({
  ticket
}: TicketDetailsLayoutProps): ReactElement => {
  const { activities } = useActivities();
  const { openLogTimeModal } = useTimeTracking();
  const {
    changeTicketAcceptanceCriteria,
    changeTicketDescription,
    changeTicketEstimatedTime,
    changeTicketPriority,
    changeTicketSeverity,
    changeTicketTitle,
    hasLoadedSeverities,
    isLoadingSeverities,
    isUpdatingTicketAcceptanceCriteria,
    isUpdatingTicketDescription,
    isUpdatingTicketEstimatedTime,
    isUpdatingTicketPriority,
    isUpdatingTicketSeverity,
    isUpdatingTicketTitle,
    severities
  } = useTicket();
  const [title, setTitle] = useState(ticket.name);
  const [priority, setPriority] = useState(ticket.priority ?? 5);
  const [severityId, setSeverityId] = useState(ticket.severityId);
  const [estimatedMinutes, setEstimatedMinutes] = useState(ticket.estimatedMinutes);
  const [description, setDescription] = useState(ticket.description ?? "");
  const [acceptanceCriteria, setAcceptanceCriteria] = useState(ticket.acceptanceCriteria ?? "");
  const [titleError, setTitleError] = useState<string | null>(null);
  const [prioritySeverityError, setPrioritySeverityError] = useState<string | null>(null);
  const [estimatedTimeError, setEstimatedTimeError] = useState<string | null>(null);
  const [descriptionError, setDescriptionError] = useState<string | null>(null);
  const [acceptanceCriteriaError, setAcceptanceCriteriaError] = useState<string | null>(null);

  useEffect(() => {
    setTitle(ticket.name);
    setPriority(ticket.priority ?? 5);
    setSeverityId(ticket.severityId);
    setEstimatedMinutes(ticket.estimatedMinutes);
    setDescription(ticket.description ?? "");
    setAcceptanceCriteria(ticket.acceptanceCriteria ?? "");
  }, [ticket]);

  const timeVelocityData = getTimeVelocityData(ticket);
  const activityNameById = Object.fromEntries(
    activities.map(activity => [activity.activityId, activity.name])
  );
  const { activityColorByName, activitySlices, totalActivityMinutes } = useTimeTrackingActivityChart(
    ticket.timeEntries,
    activityNameById
  );
  const hasTitleChanged = title.trim() !== ticket.name.trim();
  const hasPrioritySeverityChanged = priority !== (ticket.priority ?? 5) || severityId !== ticket.severityId;
  const hasEstimatedTimeChanged = estimatedMinutes !== ticket.estimatedMinutes;
  const hasDescriptionChanged = description.trim() !== (ticket.description ?? "").trim();
  const hasAcceptanceCriteriaChanged =
    acceptanceCriteria.trim() !== (ticket.acceptanceCriteria ?? "").trim();
  const canRegisterTime = ticket.board?.active === true;
  const isTitleDisabled = isUpdatingTicketTitle;
  const isPrioritySeverityDisabled = isUpdatingTicketPriority || isUpdatingTicketSeverity;
  const isEstimatedTimeDisabled = isUpdatingTicketEstimatedTime;
  const isDescriptionDisabled = isUpdatingTicketDescription;
  const isAcceptanceCriteriaDisabled = isUpdatingTicketAcceptanceCriteria;

  return (
    <Grid container spacing={3}>
      <Grid size={{ lg: 8, xs: 12 }}>
        <Stack spacing={2}>
          <Card variant="outlined">
            <CardContent>
              <Stack padding={3} spacing={2}>
                <Typography variant="h5">People</Typography>
                <Grid container spacing={2}>
                  <Grid size={{ md: 6, xs: 12 }}>
                    <Stack spacing={0.5}>
                      <Typography color="text.secondary" variant="caption">
                        Creator
                      </Typography>
                      <Person fallbackLabel="Unknown creator" person={ticket.createdBy} />
                    </Stack>
                  </Grid>
                  <Grid size={{ md: 6, xs: 12 }}>
                    <Stack spacing={0.5}>
                      <Typography color="text.secondary" variant="caption">
                        Assignee
                      </Typography>
                      <Person fallbackLabel="Unassigned" person={ticket.assignedTo} />
                    </Stack>
                  </Grid>
                </Grid>
              </Stack>
            </CardContent>
          </Card>

          <Card variant="outlined">
            <CardContent>
              <Stack padding={3} spacing={3}>
                {titleError !== null ? <Alert severity="error">{titleError}</Alert> : null}
                <Stack spacing={1}>
                  <Typography variant="h5">Title</Typography>
                  <Typography color="text.secondary" variant="body2">
                    Update the ticket title shown across the workspace.
                  </Typography>
                </Stack>
                <TextField
                  disabled={isTitleDisabled}
                  fullWidth
                  label="Ticket title"
                  onChange={(event: ChangeEvent<HTMLInputElement>) => {
                    setTitle(event.target.value);
                  }}
                  value={title}
                />
                {hasTitleChanged ? (
                  <Stack direction={{ sm: "row", xs: "column-reverse" }} justifyContent="flex-end" spacing={1.5}>
                    <Button
                      disabled={isTitleDisabled}
                      onClick={() => {
                        setTitle(ticket.name);
                        setTitleError(null);
                      }}
                      variant="outlined"
                    >
                      Cancel
                    </Button>
                    <Button
                      disabled={isTitleDisabled || title.trim().length === 0}
                      onClick={() => {
                        setTitleError(null);
                        void changeTicketTitle(title.trim()).catch((error: unknown) => {
                          setTitleError(
                            error instanceof Error
                              ? error.message
                              : "Failed to update ticket title."
                          );
                        });
                      }}
                      variant="contained"
                    >
                      Apply
                    </Button>
                  </Stack>
                ) : null}
              </Stack>
            </CardContent>
          </Card>

          <Card variant="outlined">
            <CardContent>
              <Stack padding={3} spacing={3}>
                {prioritySeverityError !== null ? <Alert severity="error">{prioritySeverityError}</Alert> : null}
                <Stack spacing={1}>
                  <Typography variant="h5">Priority and severity</Typography>
                  <Typography color="text.secondary" variant="body2">
                    Set urgency and impact using the same scale as board workflows.
                  </Typography>
                </Stack>
                <Grid container spacing={2}>
                  <Grid size={{ md: 6, xs: 12 }}>
                    <TicketPrioritySelect
                      disabled={isPrioritySeverityDisabled}
                      onChange={setPriority}
                      value={priority}
                    />
                  </Grid>
                  <Grid size={{ md: 6, xs: 12 }}>
                    <TicketSeveritySelect
                      disabled={isPrioritySeverityDisabled || (!hasLoadedSeverities && isLoadingSeverities)}
                      onChange={setSeverityId}
                      severities={severities}
                      value={severityId}
                    />
                  </Grid>
                </Grid>
                {hasPrioritySeverityChanged ? (
                  <Stack direction={{ sm: "row", xs: "column-reverse" }} justifyContent="flex-end" spacing={1.5}>
                    <Button
                      disabled={isPrioritySeverityDisabled}
                      onClick={() => {
                        setPriority(ticket.priority ?? 5);
                        setSeverityId(ticket.severityId);
                        setPrioritySeverityError(null);
                      }}
                      variant="outlined"
                    >
                      Cancel
                    </Button>
                    <Button
                      disabled={isPrioritySeverityDisabled}
                      onClick={() => {
                        setPrioritySeverityError(null);
                        void changeTicketPriority(priority)
                          .then(() => {
                            return changeTicketSeverity(severityId);
                          })
                          .catch((error: unknown) => {
                            setPrioritySeverityError(
                              error instanceof Error
                                ? error.message
                                : "Failed to update ticket priority or severity."
                            );
                          });
                      }}
                      variant="contained"
                    >
                      Apply
                    </Button>
                  </Stack>
                ) : null}
              </Stack>
            </CardContent>
          </Card>

          <Card variant="outlined">
            <CardContent>
              <Stack padding={3} spacing={3}>
                {estimatedTimeError !== null ? <Alert severity="error">{estimatedTimeError}</Alert> : null}
                <Stack spacing={1}>
                  <Typography variant="h5">Estimated time</Typography>
                  <Typography color="text.secondary" variant="body2">
                    Set how much time this ticket is expected to take.
                  </Typography>
                </Stack>
                <TimeInput
                  disabled={isEstimatedTimeDisabled}
                  label="Estimated time"
                  onChange={setEstimatedMinutes}
                  value={estimatedMinutes}
                />
                {hasEstimatedTimeChanged ? (
                  <Stack direction={{ sm: "row", xs: "column-reverse" }} justifyContent="flex-end" spacing={1.5}>
                    <Button
                      disabled={isEstimatedTimeDisabled}
                      onClick={() => {
                        setEstimatedMinutes(ticket.estimatedMinutes);
                        setEstimatedTimeError(null);
                      }}
                      variant="outlined"
                    >
                      Cancel
                    </Button>
                    <Button
                      disabled={isEstimatedTimeDisabled}
                      onClick={() => {
                        setEstimatedTimeError(null);
                        void changeTicketEstimatedTime(estimatedMinutes).catch((error: unknown) => {
                          setEstimatedTimeError(
                            error instanceof Error
                              ? error.message
                              : "Failed to update ticket estimated time."
                          );
                        });
                      }}
                      variant="contained"
                    >
                      Apply
                    </Button>
                  </Stack>
                ) : null}
              </Stack>
            </CardContent>
          </Card>

          <Card variant="outlined">
            <CardContent>
              <Stack padding={3} spacing={3}>
                {descriptionError !== null ? <Alert severity="error">{descriptionError}</Alert> : null}
                <Stack spacing={1}>
                  <Typography variant="h5">Description</Typography>
                  <Typography color="text.secondary" variant="body2">
                    Add context and implementation notes for the ticket.
                  </Typography>
                </Stack>
                <TextField
                  disabled={isDescriptionDisabled}
                  fullWidth
                  label="Description"
                  minRows={4}
                  multiline
                  onChange={event => {
                    setDescription(event.target.value);
                  }}
                  value={description}
                />
                {hasDescriptionChanged ? (
                  <Stack direction={{ sm: "row", xs: "column-reverse" }} justifyContent="flex-end" spacing={1.5}>
                    <Button
                      disabled={isDescriptionDisabled}
                      onClick={() => {
                        setDescription(ticket.description ?? "");
                        setDescriptionError(null);
                      }}
                      variant="outlined"
                    >
                      Cancel
                    </Button>
                    <Button
                      disabled={isDescriptionDisabled}
                      onClick={() => {
                        setDescriptionError(null);
                        void changeTicketDescription(normalizeOptionalText(description)).catch(
                          (error: unknown) => {
                            setDescriptionError(
                              error instanceof Error
                                ? error.message
                                : "Failed to update ticket description."
                            );
                          }
                        );
                      }}
                      variant="contained"
                    >
                      Apply
                    </Button>
                  </Stack>
                ) : null}
              </Stack>
            </CardContent>
          </Card>

          <Card variant="outlined">
            <CardContent>
              <Stack padding={3} spacing={3}>
                {acceptanceCriteriaError !== null ? (
                  <Alert severity="error">{acceptanceCriteriaError}</Alert>
                ) : null}
                <Stack spacing={1}>
                  <Typography variant="h5">Acceptance criteria</Typography>
                  <Typography color="text.secondary" variant="body2">
                    Define completion conditions and validation checkpoints.
                  </Typography>
                </Stack>
                <TextField
                  disabled={isAcceptanceCriteriaDisabled}
                  fullWidth
                  label="Acceptance criteria"
                  minRows={4}
                  multiline
                  onChange={event => {
                    setAcceptanceCriteria(event.target.value);
                  }}
                  value={acceptanceCriteria}
                />
                {hasAcceptanceCriteriaChanged ? (
                  <Stack direction={{ sm: "row", xs: "column-reverse" }} justifyContent="flex-end" spacing={1.5}>
                    <Button
                      disabled={isAcceptanceCriteriaDisabled}
                      onClick={() => {
                        setAcceptanceCriteria(ticket.acceptanceCriteria ?? "");
                        setAcceptanceCriteriaError(null);
                      }}
                      variant="outlined"
                    >
                      Cancel
                    </Button>
                    <Button
                      disabled={isAcceptanceCriteriaDisabled}
                      onClick={() => {
                        setAcceptanceCriteriaError(null);
                        void changeTicketAcceptanceCriteria(
                          normalizeOptionalText(acceptanceCriteria)
                        ).catch((error: unknown) => {
                          setAcceptanceCriteriaError(
                            error instanceof Error
                              ? error.message
                              : "Failed to update ticket acceptance criteria."
                          );
                        });
                      }}
                      variant="contained"
                    >
                      Apply
                    </Button>
                  </Stack>
                ) : null}
              </Stack>
            </CardContent>
          </Card>
        </Stack>
      </Grid>

      <Grid size={{ lg: 4, xs: 12 }}>
        <Stack spacing={2}>
          <Paper sx={{ minHeight: 210, p: 1.5 }} variant="outlined">
            <Stack spacing={1.25}>
              <Stack alignItems="center" direction="row" justifyContent="space-between" spacing={1}>
                <Typography variant="subtitle2">Time velocity</Typography>
              </Stack>
              <TimeVelocityChart chartHeight={96} data={timeVelocityData} />
            </Stack>
          </Paper>

          <Paper sx={{ p: 1.5 }} variant="outlined">
            <Stack spacing={1.5}>
              <Typography variant="subtitle2">Activity</Typography>
              <TimeTrackingActivityPieChart
                activitySlices={activitySlices}
                emptyLegendText="No activity tracked yet."
                totalActivityMinutes={totalActivityMinutes}
              />
            </Stack>
          </Paper>

          <Paper sx={{ p: 1.5 }} variant="outlined">
            <Stack spacing={0}>
              {canRegisterTime ? (
                <Button
                  onClick={() => {
                    openLogTimeModal(ticket.ticketId);
                  }}
                  size="small"
                  startIcon={<AccessTimeRoundedIcon fontSize="small" />}
                  variant="contained"
                >
                  Register time
                </Button>
              ) : (
                <Typography color="text.secondary" variant="caption">
                  Time tracking is unavailable because the board is closed.
                </Typography>
              )}
            </Stack>
          </Paper>

          <Paper sx={{ p: 1.5 }} variant="outlined">
            <Stack spacing={1.5}>
              <Typography variant="subtitle2">Activity log</Typography>
              <TicketActivityEntryList
                activityColorByName={activityColorByName}
                entries={ticket.timeEntries}
              />
            </Stack>
          </Paper>
        </Stack>
      </Grid>
    </Grid>
  );
};
