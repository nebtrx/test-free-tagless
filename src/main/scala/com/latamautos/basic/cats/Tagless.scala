package com.latamautos.basic.cats

import java.util.UUID

import cats.Monad
import cats.implicits._
import com.latamautos.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.higherKinds

object Tagless {

  // Algebra / Operations
  trait UserRepositoryAlg[F[_]] {
    def findUser(id: UUID): F[Option[User]]
    def updateUser(u: User): F[Unit]
  }

  // Consumer
  class LoyaltyPoints[F[_]: Monad](ur: UserRepositoryAlg[F]) {
    def addPoints(userId: UUID, pointsToAdd: Int): F[Either[String, Unit]] = {
      ur.findUser(userId).flatMap {
        case None => implicitly[Monad[F]].pure(Left("User not found"))
        case Some(user) =>
          val updated = user.copy(loyaltyPoints = user.loyaltyPoints + pointsToAdd)
          ur.updateUser(updated).map(_ => Right(()))
      }
    }
  }

  // Interpreters
  trait FutureInterpreter extends UserRepositoryAlg[Future] {
    override def findUser(id: UUID): Future[Option[User]] =
      Future.successful(None) /* go and talk to a database */

    override def updateUser(u: User): Future[Unit] =
      Future.successful(()) /* as above */
  }


  // Example
  val result: Future[Either[String, Unit]] =
    // Injection
    new LoyaltyPoints(new FutureInterpreter {}).addPoints(UUID.randomUUID(), 10)

}
