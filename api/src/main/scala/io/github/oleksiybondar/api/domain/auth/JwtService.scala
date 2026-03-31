package io.github.oleksiybondar.api.domain.auth

/** Encodes and validates JWT access tokens used by the authentication flow. */
trait JwtService[F[_]] {

  /** Serializes access-token claims into a signed token value.
    *
    * @param claims
    *   Domain claims to embed in the token.
    * @return
    *   Signed access token ready to return to the client.
    */
  def encode(claims: AccessTokenClaims): F[AccessToken]

  /** Reads claims from a token without treating the token as trusted.
    *
    * @param token
    *   Access token to inspect.
    * @return
    *   Decoded claims when the payload can be parsed, otherwise `None`.
    */
  def decode(token: AccessToken): F[Option[AccessTokenClaims]]

  /** Verifies the token signature and returns trusted claims when valid.
    *
    * @param token
    *   Access token received from the client.
    * @return
    *   Verified claims when the token is valid for this application, otherwise `None`.
    */
  def verify(token: AccessToken): F[Option[AccessTokenClaims]]
}
