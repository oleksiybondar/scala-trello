package io.github.oleksiybondar.api.domain.auth

import cats.data.EitherT
import io.github.oleksiybondar.api.domain.auth.password.PasswordStrengthError
import io.github.oleksiybondar.api.domain.user.{UserId, Username}

/** Input required to create a new account through the authentication flow. */
final case class RegisterUserCommand(
    /** Email used as the primary login identifier for the new user. */
    email: String,
    /** Raw password supplied during registration before hashing. */
    password: String,
    /** User-facing first name stored on the created profile. */
    firstName: String,
    /** User-facing last name stored on the created profile. */
    lastName: String,
    /** Optional username that can be used as an alternative login identifier. */
    username: Option[Username]
)

/** Public authentication use cases exposed to transport layers. */
trait AuthService[F[_]] {

  /** Creates a new account and immediately starts an authenticated session.
    *
    * @param command
    *   Registration payload after transport-level decoding.
    * @return
    *   Either a registration-specific auth error or the issued access/refresh token pair.
    */
  def register(command: RegisterUserCommand): EitherT[F, AuthError, AuthTokens]

  /** Authenticates an existing user by username or email plus password.
    *
    * @param login
    *   Username or email used to locate the user.
    * @param password
    *   Raw password to verify against the stored password hash.
    * @return
    *   Either invalid-credential style errors or the issued access/refresh token pair.
    */
  def login(login: String, password: String): EitherT[F, AuthError, AuthTokens]

  /** Rotates an existing refresh token and issues a fresh token pair.
    *
    * @param refreshToken
    *   Refresh token identifying the active auth session.
    * @return
    *   Either an invalid-refresh-token error or the next access/refresh token pair.
    */
  def refresh(refreshToken: RefreshToken): EitherT[F, AuthError, AuthTokens]

  /** Revokes the session associated with the provided refresh token.
    *
    * @param refreshToken
    *   Refresh token belonging to the session that should be invalidated.
    */
  def logout(refreshToken: RefreshToken): F[Unit]

  /** Verifies an access token and extracts the authenticated user id when valid.
    *
    * @param accessToken
    *   Bearer token received from the client.
    * @return
    *   The authenticated user id when the token is valid and trusted, otherwise `None`.
    */
  def verifyToken(accessToken: AccessToken): F[Option[UserId]]
}

/** Domain errors produced by authentication and registration flows. */
sealed trait AuthError extends Product with Serializable

object AuthError {

  /** Returned when the provided login identifier or password is not accepted. */
  case object InvalidCredentials extends AuthError

  /** Returned when a refresh token is missing, expired, revoked, or cannot be rotated. */
  case object InvalidRefreshToken extends AuthError

  /** Returned when registration is attempted without an email value. */
  case object EmailRequired extends AuthError

  /** Returned when registration receives an email that does not satisfy the expected format. */
  case object InvalidEmail extends AuthError

  /** Returned when registration tries to reuse an email already assigned to another user. */
  case object EmailAlreadyUsed extends AuthError

  /** Returned when a password violates one or more configured password-strength rules. */
  final case class WeakPassword(errors: List[PasswordStrengthError]) extends AuthError
}
