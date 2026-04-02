import type { AuthCurrentUserResponse } from "@models/user/dto";
import type { CurrentUser } from "@models/user/types";

export const mapAuthCurrentUserResponseToCurrentUser = (
  response: AuthCurrentUserResponse
): CurrentUser => {
  return {
    avatarUrl: response.avatar_url,
    createdAt: response.created_at,
    email: response.email,
    firstName: response.first_name,
    lastName: response.last_name,
    userId: response.id,
    username: response.username
  };
};

export const getCurrentUserDisplayName = (
  firstName: string | null,
  lastName: string | null
): string => {
  const fullName = [firstName, lastName]
    .filter((part): part is string => part !== null && part.trim().length > 0)
    .join(" ")
    .trim();

  return fullName.length > 0 ? fullName : "Unnamed user";
};
