package io.github.oleksiybondar.api.domain.user

import cats.data.EitherT
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.github.oleksiybondar.api.domain.auth.password.{
  PasswordHasher,
  PasswordHistory,
  PasswordStrengthValidator
}
import io.github.oleksiybondar.api.domain.user.UserMutationError.{
  EmailAlreadyUsed,
  EmailRequired,
  InvalidCurrentPassword,
  InvalidEmail,
  PasswordAlreadyUsed,
  UserNotFound,
  UsernameAlreadyUsed,
  UsernameRequired,
  WeakPassword
}
import io.github.oleksiybondar.api.infrastructure.db.user.UserRepo
import io.github.oleksiybondar.api.validation.InputValidation

/** Default `UserService` implementation backed by `UserRepo` and password policy components. */
final class UserServiceLive[F[_]: Sync](
    userRepo: UserRepo[F],
    passwordHasher: PasswordHasher[F],
    passwordStrengthValidator: PasswordStrengthValidator,
    passwordHistory: PasswordHistory[F]
) extends UserService[F] {

  override def createUser(user: User): F[Unit] =
    userRepo.create(user)

  override def getUser(id: UserId): F[Option[User]] =
    userRepo.findById(id)

  override def getByUsername(username: Username): F[Option[User]] =
    userRepo.findByUsername(username)

  override def getByEmail(email: Email): F[Option[User]] =
    userRepo.findByEmail(email)

  override def listUsers: F[List[User]] =
    userRepo.list

  override def listUsersPage(offset: Int, limit: Int): F[List[User]] =
    userRepo.listPage(offset, limit)

  override def updateProfile(
      userId: UserId,
      firstName: String,
      lastName: String
  ): EitherT[F, UserMutationError, User] =
    for {
      user   <- loadUser(userId)
      updated = user.copy(
                  firstName = FirstName(firstName),
                  lastName = LastName(lastName)
                )
      _      <- persist(updated)
    } yield updated

  override def changeAvatar(
      userId: UserId,
      avatarUrl: Option[String]
  ): EitherT[F, UserMutationError, User] =
    for {
      user   <- loadUser(userId)
      updated = user.copy(
                  avatarUrl = normalizeOptionalString(avatarUrl).map(AvatarUrl(_))
                )
      _      <- persist(updated)
    } yield updated

  override def changeUsername(
      userId: UserId,
      username: String
  ): EitherT[F, UserMutationError, User] =
    for {
      normalized <- EitherT.fromEither[F](normalizeUsername(username))
      user       <- loadUser(userId)
      _          <- ensureUsernameAvailable(userId, normalized)
      updated     = user.copy(username = Some(normalized))
      _          <- persist(updated)
    } yield updated

  override def changeEmail(userId: UserId, email: String): EitherT[F, UserMutationError, User] =
    for {
      normalized <- EitherT.fromEither[F](normalizeEmail(email))
      _          <- ensureEmailAvailable(userId, normalized)
      user       <- loadUser(userId)
      updated     = user.copy(email = Some(normalized))
      _          <- persist(updated)
    } yield updated

  override def changePassword(
      userId: UserId,
      currentPassword: String,
      newPassword: String
  ): EitherT[F, UserMutationError, Boolean] =
    for {
      user         <- loadUser(userId)
      currentValid <- EitherT.liftF(passwordHasher.verify(currentPassword, user.passwordHash))
      _            <- EitherT.cond[F](currentValid, (), InvalidCurrentPassword)
      _            <- EitherT.fromEither[F](
                        passwordStrengthValidator
                          .validate(newPassword)
                          .leftMap(errors => WeakPassword(errors.toChain.toList))
                          .toEither
                      )
      usedBefore   <- EitherT.liftF(passwordHistory.wasUsedBefore(userId, newPassword))
      _            <- EitherT.cond[F](!usedBefore, (), PasswordAlreadyUsed)
      newHash      <- EitherT.liftF(passwordHasher.hash(newPassword))
      updated       = user.copy(passwordHash = newHash)
      _            <- persist(updated)
      _            <- EitherT.liftF(passwordHistory.record(userId, newHash))
    } yield true

  override def updateUser(user: User): F[Boolean] =
    userRepo.update(user)

  override def deleteUser(id: UserId): F[Boolean] =
    userRepo.delete(id)

  private def loadUser(userId: UserId): EitherT[F, UserMutationError, User] =
    EitherT.fromOptionF(userRepo.findById(userId), UserNotFound)

  private def persist(user: User): EitherT[F, UserMutationError, Unit] =
    EitherT(
      userRepo.update(user).map {
        case true  => Right(())
        case false => Left(UserNotFound)
      }
    )

  private def ensureUsernameAvailable(
      currentUserId: UserId,
      username: Username
  ): EitherT[F, UserMutationError, Unit] =
    EitherT(
      userRepo.findByUsername(username).map {
        case Some(existingUser) if existingUser.id != currentUserId => Left(UsernameAlreadyUsed)
        case _                                                      => Right(())
      }
    )

  private def ensureEmailAvailable(
      currentUserId: UserId,
      email: Email
  ): EitherT[F, UserMutationError, Unit] =
    EitherT(
      userRepo.findByEmail(email).map {
        case Some(existingUser) if existingUser.id != currentUserId => Left(EmailAlreadyUsed)
        case _                                                      => Right(())
      }
    )

  private def normalizeUsername(rawUsername: String): Either[UserMutationError, Username] = {
    InputValidation.normalizeRequired(rawUsername) match {
      case None             => Left(UsernameRequired)
      case Some(normalized) => Right(Username(normalized))
    }
  }

  private def normalizeEmail(rawEmail: String): Either[UserMutationError, Email] =
    InputValidation.normalizeEmail(rawEmail) match {
      case None                                                          => Left(EmailRequired)
      case Some(normalized) if !InputValidation.isValidEmail(normalized) =>
        Left(InvalidEmail)
      case Some(normalized)                                              => Right(Email(normalized))
    }

  private def normalizeOptionalString(rawValue: Option[String]): Option[String] =
    rawValue.flatMap(InputValidation.normalizeRequired)
}
