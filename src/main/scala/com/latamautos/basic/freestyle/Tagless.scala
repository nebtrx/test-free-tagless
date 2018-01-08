package com.latamautos.basic.freestyle

import java.util.UUID

import cats.Monad
import com.latamautos.User
import freestyle.tagless.tagless
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits._

object Tagless {
  @tagless trait UserRepository {
    def findUser(id: UUID): FS[User]
    def updateUser(u: User): FS[User]
  }

  implicit val userRepositoryHandler: UserRepository.Handler[Future] {
    def updateUser(u: User): Future[User]

    def findUser(id: UUID): Future[User]
  } = new UserRepository.Handler[Future] {

    override def findUser(id: UUID): Future[User] = {
      Future.failed(new Exception)
    }

    override def updateUser(u: User): Future[User] = {
      Future.successful(u)
    }
  }

  def program[F[_]: Monad](userId: UUID, pointsToAdd: Int)(implicit repo: UserRepository[F]): F[User] = {
    for {
      user <- repo.findUser(userId)
      update <- repo.updateUser(user.copyAndAddPoints(pointsToAdd))
    } yield update
  }

  program[Future](UUID.randomUUID(), 10)
}
