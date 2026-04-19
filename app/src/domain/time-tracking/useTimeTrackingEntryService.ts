import { useMutation } from "@tanstack/react-query";

import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildCreateTimeTrackingEntryMutation,
  buildDeleteTimeTrackingEntryMutation,
  buildUpdateTimeTrackingActivityMutation,
  buildUpdateTimeTrackingDescriptionMutation,
  buildUpdateTimeTrackingTimeMutation,
  mapTimeTrackingEntryResponseToTimeTrackingEntry
} from "./graphql";
import type {
  CreateTimeTrackingEntryMutationResponse,
  DeleteTimeTrackingEntryMutationResponse,
  UpdateTimeTrackingActivityMutationResponse,
  UpdateTimeTrackingDescriptionMutationResponse,
  UpdateTimeTrackingTimeMutationResponse,
  TimeTrackingEntry
} from "./graphql";
import { useAuth } from "@hooks/useAuth";

export interface RegisterTimeTrackingEntryInput {
  activityId: number;
  description?: string | null;
  durationMinutes: number;
  loggedAt: string;
  ticketId: string;
}

interface UseTimeTrackingEntryServiceValue {
  deleteTimeEntry: (entryId: string) => Promise<boolean>;
  isDeletingTimeEntry: boolean;
  isRegisteringTime: boolean;
  isUpdatingTimeEntry: boolean;
  registerTimeEntry: (input: RegisterTimeTrackingEntryInput) => Promise<TimeTrackingEntry>;
  updateTimeEntry: (input: UpdateTimeTrackingEntryInput) => Promise<TimeTrackingEntry>;
}

export interface UpdateTimeTrackingEntryInput {
  activityId: number;
  description?: string | null;
  durationMinutes: number;
  entryId: string;
  loggedAt: string;
}

const normalizeDescription = (value: string | null | undefined): string | null => {
  if (value === null || value === undefined) {
    return null;
  }

  const trimmedValue = value.trim();

  return trimmedValue.length > 0 ? trimmedValue : null;
};

export const useTimeTrackingEntryService = (): UseTimeTrackingEntryServiceValue => {
  const { accessToken } = useAuth();

  const registerTimeEntryMutation = useMutation<
    TimeTrackingEntry,
    Error,
    RegisterTimeTrackingEntryInput
  >({
    mutationFn: async (input: RegisterTimeTrackingEntryInput): Promise<TimeTrackingEntry> => {
      const response = await requestGraphQL<CreateTimeTrackingEntryMutationResponse>({
        accessToken,
        document: buildCreateTimeTrackingEntryMutation({
          activityId: input.activityId,
          description: normalizeDescription(input.description),
          durationMinutes: input.durationMinutes,
          loggedAt: input.loggedAt,
          ticketId: input.ticketId
        })
      });

      return mapTimeTrackingEntryResponseToTimeTrackingEntry(response.createTimeTrackingEntry);
    }
  });
  const updateTimeTrackingActivityMutation = useMutation<
    TimeTrackingEntry,
    Error,
    Pick<UpdateTimeTrackingEntryInput, "activityId" | "entryId">
  >({
    mutationFn: async ({ activityId, entryId }) => {
      const response = await requestGraphQL<UpdateTimeTrackingActivityMutationResponse>({
        accessToken,
        document: buildUpdateTimeTrackingActivityMutation(entryId, activityId)
      });

      return mapTimeTrackingEntryResponseToTimeTrackingEntry(response.updateTimeTrackingActivity);
    }
  });
  const updateTimeTrackingTimeMutation = useMutation<
    TimeTrackingEntry,
    Error,
    Pick<UpdateTimeTrackingEntryInput, "durationMinutes" | "entryId" | "loggedAt">
  >({
    mutationFn: async ({ durationMinutes, entryId, loggedAt }) => {
      const response = await requestGraphQL<UpdateTimeTrackingTimeMutationResponse>({
        accessToken,
        document: buildUpdateTimeTrackingTimeMutation(entryId, durationMinutes, loggedAt)
      });

      return mapTimeTrackingEntryResponseToTimeTrackingEntry(response.updateTimeTrackingTime);
    }
  });
  const updateTimeTrackingDescriptionMutation = useMutation<
    TimeTrackingEntry,
    Error,
    Pick<UpdateTimeTrackingEntryInput, "description" | "entryId">
  >({
    mutationFn: async ({ description, entryId }) => {
      const response = await requestGraphQL<UpdateTimeTrackingDescriptionMutationResponse>({
        accessToken,
        document: buildUpdateTimeTrackingDescriptionMutation(entryId, normalizeDescription(description))
      });

      return mapTimeTrackingEntryResponseToTimeTrackingEntry(response.updateTimeTrackingDescription);
    }
  });
  const deleteTimeTrackingEntryMutation = useMutation<boolean, Error, string>({
    mutationFn: async (entryId: string) => {
      const response = await requestGraphQL<DeleteTimeTrackingEntryMutationResponse>({
        accessToken,
        document: buildDeleteTimeTrackingEntryMutation(entryId)
      });

      return response.deleteTimeTrackingEntry;
    }
  });

  return {
    deleteTimeEntry: async (entryId: string) => {
      return deleteTimeTrackingEntryMutation.mutateAsync(entryId);
    },
    isDeletingTimeEntry: deleteTimeTrackingEntryMutation.isPending,
    isRegisteringTime: registerTimeEntryMutation.isPending,
    isUpdatingTimeEntry:
      updateTimeTrackingActivityMutation.isPending
      || updateTimeTrackingTimeMutation.isPending
      || updateTimeTrackingDescriptionMutation.isPending,
    registerTimeEntry: async (input: RegisterTimeTrackingEntryInput) => {
      return registerTimeEntryMutation.mutateAsync(input);
    },
    updateTimeEntry: async (input: UpdateTimeTrackingEntryInput) => {
      await updateTimeTrackingActivityMutation.mutateAsync({
        activityId: input.activityId,
        entryId: input.entryId
      });
      await updateTimeTrackingTimeMutation.mutateAsync({
        durationMinutes: input.durationMinutes,
        entryId: input.entryId,
        loggedAt: input.loggedAt
      });

      return updateTimeTrackingDescriptionMutation.mutateAsync({
        description: normalizeDescription(input.description),
        entryId: input.entryId
      });
    }
  };
};
