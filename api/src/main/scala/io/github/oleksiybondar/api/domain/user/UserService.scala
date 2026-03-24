package io.github.oleksiybondar.api.domain.user

trait UserService[F[_]] {
  def createUser(user: User): F[Unit]

  def getUser(id: UserId): F[Option[User]]
  def getByUsername(username: Username): F[Option[User]]
  def getByEmail(email: Email): F[Option[User]]

  def listUsers: F[List[User]]

  def updateUser(user: User): F[Boolean]
  def deleteUser(id: UserId): F[Boolean]
}