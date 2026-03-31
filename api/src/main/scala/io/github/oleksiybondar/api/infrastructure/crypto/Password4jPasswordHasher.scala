package io.github.oleksiybondar.api.infrastructure.crypto

import cats.effect.kernel.Sync
import com.password4j.Password
import io.github.oleksiybondar.api.config.PasswordConfig
import io.github.oleksiybondar.api.domain.auth.password.PasswordHasher
import io.github.oleksiybondar.api.domain.user.PasswordHash

final class Password4jPasswordHasher[F[_]: Sync](
    config: PasswordConfig
) extends PasswordHasher[F] {

  override def hash(password: String): F[PasswordHash] =
    Sync[F].delay {
      PasswordHash(
        Password
          .hash(password)
          .addPepper(config.pepper)
          .withArgon2()
          .getResult
      )
    }

  override def verify(password: String, hash: PasswordHash): F[Boolean] =
    Sync[F].delay {
      Password
        .check(password, hash.value)
        .addPepper(config.pepper)
        .withArgon2()
    }
}
