package io.github.oleksiybondar.api.domain.user

import io.github.oleksiybondar.api.infrastructure.db.user.UserRepo

final class UserServiceLive[F[_]](
    userRepo: UserRepo[F]
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

  override def updateUser(user: User): F[Boolean] =
    userRepo.update(user)

  override def deleteUser(id: UserId): F[Boolean] =
    userRepo.delete(id)
}
