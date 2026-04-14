import { useAuth } from "@hooks/useAuth";
import { requestGraphQL } from "@helpers/requestGraphQL";
import {
  buildChangeAvatarMutation,
  buildChangeEmailMutation,
  buildChangePasswordMutation,
  buildChangeUsernameMutation,
  buildUpdateProfileMutation,
  mapGraphQLCurrentUserResponseToCurrentUser
} from "./graphql";
import type {
  ChangePasswordMutationResponse,
  CurrentUser,
  GraphQLCurrentUserResponse,
  UserMutationResponse
} from "./graphql";

interface UseUserServiceParams {
  refreshCurrentUser: () => Promise<void>;
  setCurrentUser: (user: CurrentUser | null) => void;
}

interface UserService {
  changeEmail: (email: string) => Promise<void>;
  changePassword: (currentPassword: string, password: string) => Promise<void>;
  changeUsername: (username: string) => Promise<void>;
  updateAvatar: (avatarUrl: string | null) => Promise<void>;
  updateProfile: (firstName: string, lastName: string) => Promise<void>;
}

const requireUpdatedUser = (
  response: UserMutationResponse,
  key: keyof UserMutationResponse,
  errorMessage: string
): GraphQLCurrentUserResponse => {
  const user = response[key];

  if (user === undefined) {
    throw new Error(errorMessage);
  }

  return user;
};

export const useUserService = ({
  refreshCurrentUser,
  setCurrentUser
}: UseUserServiceParams): UserService => {
  const { accessToken } = useAuth();

  const applyUpdatedUser = (response: GraphQLCurrentUserResponse): void => {
    setCurrentUser(mapGraphQLCurrentUserResponseToCurrentUser(response));
  };

  return {
    changeEmail: async (email: string) => {
      const response = await requestGraphQL<UserMutationResponse>({
        accessToken,
        document: buildChangeEmailMutation(email),
      });

      applyUpdatedUser(
        requireUpdatedUser(
          response,
          "changeEmail",
          "GraphQL response did not include the updated user."
        )
      );
    },
    changePassword: async (currentPassword: string, password: string) => {
      const response = await requestGraphQL<ChangePasswordMutationResponse>({
        accessToken,
        document: buildChangePasswordMutation(currentPassword, password),
      });

      if (!response.changePassword) {
        throw new Error("Password update was rejected.");
      }

      await refreshCurrentUser();
    },
    changeUsername: async (username: string) => {
      const response = await requestGraphQL<UserMutationResponse>({
        accessToken,
        document: buildChangeUsernameMutation(username),
      });

      applyUpdatedUser(
        requireUpdatedUser(
          response,
          "changeUsername",
          "GraphQL response did not include the updated user."
        )
      );
    },
    updateAvatar: async (avatarUrl: string | null) => {
      const response = await requestGraphQL<UserMutationResponse>({
        accessToken,
        document: buildChangeAvatarMutation(avatarUrl),
      });

      applyUpdatedUser(
        requireUpdatedUser(
          response,
          "changeAvatar",
          "GraphQL response did not include the updated user."
        )
      );
    },
    updateProfile: async (firstName: string, lastName: string) => {
      const response = await requestGraphQL<UserMutationResponse>({
        accessToken,
        document: buildUpdateProfileMutation(firstName, lastName),
      });

      applyUpdatedUser(
        requireUpdatedUser(
          response,
          "updateProfile",
          "GraphQL response did not include the updated user."
        )
      );
    }
  };
};
