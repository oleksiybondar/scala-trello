export interface DictionarySeverityResponse {
  description: string | null;
  id: string;
  name: string;
}

export interface DictionaryTimeTrackingActivityResponse {
  code: string;
  description: string | null;
  id: string;
  name: string;
}

export interface DictionariesQueryResponse {
  ticketSeverities: DictionarySeverityResponse[];
  timeTrackingActivities: DictionaryTimeTrackingActivityResponse[];
}
