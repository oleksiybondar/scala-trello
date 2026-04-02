export interface AuthCurrentUserResponse {
  id: string;
  username: string | null;
  email: string | null;
  first_name: string;
  last_name: string;
  avatar_url: string | null;
  created_at: string;
}

export interface CurrentUserProfileResponse {
  user: {
    firstName: string | null;
    lastName: string | null;
  } | null;
}
