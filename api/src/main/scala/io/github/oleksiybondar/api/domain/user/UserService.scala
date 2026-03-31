package io.github.oleksiybondar.api.domain.user

import cats.data.EitherT
import io.github.oleksiybondar.api.domain.auth.password.PasswordStrengthError

/** Domain errors produced by business-level user mutations. */
sealed trait UserMutationError extends Product with Serializable

object UserMutationError {

  /** Returned when a mutation targets a user that no longer exists. */
  case object UserNotFound extends UserMutationError

  /** Returned when username change is requested with an empty value. */
  case object UsernameRequired extends UserMutationError

  /** Returned when another user already owns the requested username. */
  case object UsernameAlreadyUsed extends UserMutationError

  /** Returned when email change is requested with an empty value. */
  case object EmailRequired extends UserMutationError

  /** Returned when the requested email does not satisfy the expected format. */
  case object InvalidEmail extends UserMutationError

  /** Returned when another user already owns the requested email. */
  case object EmailAlreadyUsed extends UserMutationError

  /** Returned when password change is attempted with the wrong current password. */
  case object InvalidCurrentPassword extends UserMutationError

  /** Returned when a new password matches one of the user's retained historical passwords. */
  case object PasswordAlreadyUsed extends UserMutationError

  /** Returned when a new password violates one or more configured strength rules. */
  final case class WeakPassword(errors: List[PasswordStrengthError]) extends UserMutationError
}

/** Application service responsible for user reads, lifecycle actions, and business mutations. */
trait UserService[F[_]] {

  /** Persists a new user instance as-is. */
  def createUser(user: User): F[Unit]

  /** Loads a user by id. */
  def getUser(id: UserId): F[Option[User]]

  /** Loads a user by username when present. */
  def getByUsername(username: Username): F[Option[User]]

  /** Loads a user by email when present. */
  def getByEmail(email: Email): F[Option[User]]

  /** Lists every persisted user. */
  def listUsers: F[List[User]]

  /** Lists a single page of users using offset/limit pagination. */
  def listUsersPage(offset: Int, limit: Int): F[List[User]]

  /** Updates first and last name for the target user. */
  def updateProfile(
      userId: UserId,
      firstName: String,
      lastName: String
  ): EitherT[F, UserMutationError, User]

  /** Replaces or removes the user's avatar URL. */
  def changeAvatar(userId: UserId, avatarUrl: Option[String]): EitherT[F, UserMutationError, User]

  /** Sets or changes the username while enforcing uniqueness. */
  def changeUsername(userId: UserId, username: String): EitherT[F, UserMutationError, User]

  /** Changes the email while enforcing format and uniqueness rules. */
  def changeEmail(userId: UserId, email: String): EitherT[F, UserMutationError, User]

  /** Changes the password after verifying the current password and password policy. */
  def changePassword(
      userId: UserId,
      currentPassword: String,
      newPassword: String
  ): EitherT[F, UserMutationError, Boolean]

  /** Replaces a full persisted user record. */
  def updateUser(user: User): F[Boolean]

  /** Deletes a user by id. */
  def deleteUser(id: UserId): F[Boolean]
}
