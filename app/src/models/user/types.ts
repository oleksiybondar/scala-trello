export interface CurrentUser {
  userId: string;
  username: string | null;
  email: string | null;
  firstName: string;
  lastName: string;
  avatarUrl: string | null;
  createdAt: string;
}

export interface CurrentUserProfileViewModel {
  displayName: string;
}
