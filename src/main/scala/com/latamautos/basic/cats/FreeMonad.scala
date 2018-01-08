package com.latamautos.basic.cats

import java.util.UUID

import cats.free.Free
import cats.implicits._
import cats.~>
import com.latamautos.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FreeMonad {

  // Algebra
  sealed trait UserRepositoryAlg[T]

  // Algebra DSL
  case class FindUser(id: UUID) extends UserRepositoryAlg[Option[User]]
  case class UpdateUser(u: User) extends UserRepositoryAlg[Unit]

  // Free Monad Type
  type UserRepository[T] = Free[UserRepositoryAlg, T]

  // Algebra operations
  def findUser(id: UUID): UserRepository[Option[User]] = Free.liftF(FindUser(id))
  def updateUser(u: User): UserRepository[Unit] = Free.liftF(UpdateUser(u))


  // Consumer
  def addPoints(userId: UUID, pointsToAdd: Int): UserRepository[Either[String, Unit]] = {
    findUser(userId).flatMap {
      case None => Free.pure(Left("User not found"))
      case Some(user) =>
        val updated = user.copyAndAddPoints(pointsToAdd)
        updateUser(updated).map(_ => Right(()))
    }
  }

  // interpreters
  val futureInterpreter = new (UserRepositoryAlg ~> Future) {
    override def apply[A](fa: UserRepositoryAlg[A]): Future[A] = fa match {
      case FindUser(id) => /* go and talk to a database */ Future.successful(None)
      case UpdateUser(u) => /* as above */ Future.successful(())
    }
  }

  // Example
  val result: Future[Either[String, Unit]] =
    addPoints(UUID.randomUUID(), 10).foldMap(futureInterpreter)
}
