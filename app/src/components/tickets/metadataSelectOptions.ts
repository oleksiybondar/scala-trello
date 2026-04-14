import type { SvgIconComponent } from "@mui/icons-material";

import {
  PRIORITY_OPTIONS,
  getSeverityMeta
} from "@components/tickets/ticketMetadata";
import type { DictionarySeverity } from "../../domain/dictionaries/graphql";
import type { Ticket } from "../../domain/ticket/graphql";
import type { TicketMetadataTone } from "./ticketMetadata";

export interface TicketMetadataSelectOption<TValue extends string | number> {
  description: string;
  icon: SvgIconComponent;
  label: string;
  tone: TicketMetadataTone;
  value: TValue;
}

export const toSeverityLabel = (name: string): string => {
  return name.charAt(0).toUpperCase() + name.slice(1);
};

export const buildPrioritySelectOptions = (): TicketMetadataSelectOption<number>[] => {
  return PRIORITY_OPTIONS.map(option => {
    return {
      description: option.description,
      icon: option.icon,
      label: option.label,
      tone: option.tone,
      value: option.key
    };
  });
};

const toSeverityOption = (
  severityId: string,
  severityName: string,
  description: string
): TicketMetadataSelectOption<string> | null => {
  const severityMeta = getSeverityMeta(severityName);

  if (severityMeta === null) {
    return null;
  }

  return {
    description,
    icon: severityMeta.icon,
    label: toSeverityLabel(severityName),
    tone: severityMeta.tone,
    value: severityId
  };
};

export const buildSeveritySelectOptions = (
  severities: DictionarySeverity[]
): TicketMetadataSelectOption<string>[] => {
  return severities.flatMap(severity => {
    const option = toSeverityOption(
      severity.severityId,
      severity.name,
      severity.description ?? "No description"
    );

    return option === null ? [] : [option];
  });
};

export const buildSeveritySelectOptionsFromTickets = (
  tickets: Pick<Ticket, "severityId" | "severityName">[]
): TicketMetadataSelectOption<string>[] => {
  const optionsById: Record<string, TicketMetadataSelectOption<string>> = {};

  tickets.forEach(ticket => {
    if (ticket.severityId === null || ticket.severityName === null) {
      return;
    }

    const option = toSeverityOption(ticket.severityId, ticket.severityName, "No description");

    if (option !== null) {
      optionsById[option.value] = option;
    }
  });

  return Object.values(optionsById);
};

export const findMetadataOption = <TValue extends string | number>(
  options: TicketMetadataSelectOption<TValue>[],
  value: TValue
): TicketMetadataSelectOption<TValue> | null => {
  return options.find(option => option.value === value) ?? null;
};
