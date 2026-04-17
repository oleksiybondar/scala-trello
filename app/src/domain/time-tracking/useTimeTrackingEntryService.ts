import { useMutation } from "@tanstack/react-query";

import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildCreateTimeTrackingEntryMutation,
  mapTimeTrackingEntryResponseToTimeTrackingEntry
} from "./graphql";
import type {
  CreateTimeTrackingEntryMutationResponse,
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
  isRegisteringTime: boolean;
  registerTimeEntry: (input: RegisterTimeTrackingEntryInput) => Promise<TimeTrackingEntry>;
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

  return {
    isRegisteringTime: registerTimeEntryMutation.isPending,
    registerTimeEntry: async (input: RegisterTimeTrackingEntryInput) => {
      return registerTimeEntryMutation.mutateAsync(input);
    }
  };
};
