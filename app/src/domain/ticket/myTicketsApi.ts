import type { QueryMyTicketsParams } from "@contexts/my-tickets-context";
import type { Ticket } from "./graphql";

export const MY_TICKETS_PER_PAGE = 30;

const filterBySearch = (ticket: Ticket, keyword: string | undefined): boolean => {
  if (keyword === undefined || keyword.trim().length === 0) {
    return true;
  }

  const normalizedKeyword = keyword.trim().toLowerCase();

  return [ticket.name, ticket.description, ticket.acceptanceCriteria].some(value => {
    return value?.toLowerCase().includes(normalizedKeyword) ?? false;
  });
};

const filterByPriorities = (ticket: Ticket, priorities: number[] | undefined): boolean => {
  if (priorities === undefined || priorities.length === 0) {
    return true;
  }

  return ticket.priority !== null && priorities.includes(ticket.priority);
};

const filterBySeverities = (ticket: Ticket, severityIds: string[] | undefined): boolean => {
  if (severityIds === undefined || severityIds.length === 0) {
    return true;
  }

  return ticket.severityId !== null && severityIds.includes(ticket.severityId);
};

export const filterMyTickets = (
  tickets: Ticket[],
  params: QueryMyTicketsParams
): Ticket[] => {
  return tickets
    .filter(ticket => filterBySearch(ticket, params.keyword))
    .filter(ticket => filterByPriorities(ticket, params.priorities))
    .filter(ticket => filterBySeverities(ticket, params.severityIds))
    .sort((left, right) => right.modifiedAt.localeCompare(left.modifiedAt));
};

export const countMyTickets = (
  tickets: Ticket[],
  params: QueryMyTicketsParams
): number => {
  return tickets
    .filter(ticket => filterBySearch(ticket, params.keyword))
    .filter(ticket => filterByPriorities(ticket, params.priorities))
    .filter(ticket => filterBySeverities(ticket, params.severityIds)).length;
};
