import type { DictionarySeverity, DictionaryTimeTrackingActivity } from "./types";
import type {
  DictionarySeverityResponse,
  DictionaryTimeTrackingActivityResponse
} from "./dto";

export const mapDictionarySeverityResponse = (
  response: DictionarySeverityResponse
): DictionarySeverity => {
  return {
    description: response.description,
    name: response.name,
    severityId: response.id
  };
};

export const mapDictionaryTimeTrackingActivityResponse = (
  response: DictionaryTimeTrackingActivityResponse
): DictionaryTimeTrackingActivity => {
  return {
    activityId: response.id,
    code: response.code,
    description: response.description,
    name: response.name
  };
};
