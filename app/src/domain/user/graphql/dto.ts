export interface AuthCurrentUserResponse {
  id: string;
  username: string | null;
  email: string | null;
  first_name: string;
  last_name: string;
  avatar_url: string | null;
  created_at: string;
}

export interface GraphQLCurrentUserResponse {
  id: string;
  username: string | null;
  email: string | null;
  firstName: string;
  lastName: string;
  avatarUrl: string | null;
  createdAt: string;
}

export interface CurrentUserProfileResponse {
  user: {
    firstName: string | null;
    lastName: string | null;
  } | null;
}

export interface UserMutationResponse {
  changeAvatar?: GraphQLCurrentUserResponse;
  changeEmail?: GraphQLCurrentUserResponse;
  changeUsername?: GraphQLCurrentUserResponse;
  updateProfile?: GraphQLCurrentUserResponse;
}

export interface ChangePasswordMutationResponse {
  changePassword: boolean;
}
