package com.latamautos.combined.cats

import java.util.UUID

import cats.data.EitherK
import cats.free.Free
import cats.implicits._
import cats.{InjectK, ~>}
import com.latamautos.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FreeMonad  {

  // User Repository Algebra
  sealed trait UserRepositoryAlg[T]
  // User Repository Algebra DSL
  case class FindUser(id: UUID) extends UserRepositoryAlg[Option[User]]
  case class UpdateUser(u: User) extends UserRepositoryAlg[Unit]

  // Coproduct injection boilerplate code for User Algebra
  class Users[F[_]](implicit i: InjectK[UserRepositoryAlg, F]) {
    def findUser(id: UUID): Free[F, Option[User]] = Free.inject(FindUser(id))
    def updateUser(u: User): Free[F, Unit] = Free.inject(UpdateUser(u))
  }
  object Users {
    implicit def users[F[_]](implicit i: InjectK[UserRepositoryAlg, F]): Users[F] = new Users
  }

  // Email Algebra
  sealed trait EmailAlg[T]
  // Email Algebra DSL
  case class SendEmail(email: String, subject: String, body: String) extends EmailAlg[Unit]

  // Coproduct injection boilerplate code for Email Algebra
  class Emails[F[_]](implicit i: InjectK[EmailAlg, F]) {
    def sendEmail(email: String, subject: String, body: String): Free[F, Unit] = Free.inject(SendEmail(email, subject, body))
  }
  object Emails {
    implicit def emails[F[_]](implicit i: InjectK[EmailAlg, F]): Emails[F] = new Emails
  }

  // High Order Coproduct of Email and User Algebra

  type UserAndEmailAlg[T] = EitherK[UserRepositoryAlg, EmailAlg, T]

  // Consumer
  def addPoints(userId: UUID, pointsToAdd: Int)(
    // Injecting algebras
    implicit ur: Users[UserAndEmailAlg], es: Emails[UserAndEmailAlg]): Free[UserAndEmailAlg, Either[String, Unit]] = {

    ur.findUser(userId).flatMap {
      case None => Free.pure(Left("User not found"))
      case Some(user) =>
        val updated = user.copy(loyaltyPoints = user.loyaltyPoints + pointsToAdd)

        for {
          _ <- ur.updateUser(updated)
          _ <- es.sendEmail(user.email, "Points added!", s"You now have ${updated.loyaltyPoints}")
        } yield Right(())
    }
  }

  // Algebras interpreters
  val futureUserInterpreter = new (UserRepositoryAlg ~> Future) {
    override def apply[A](fa: UserRepositoryAlg[A]): Future[A] = fa match {
      case FindUser(id) => /* go and talk to a database */ Future.successful(None)
      case UpdateUser(u) => /* as above */ Future.successful(())
    }
  }

  val futureEmailInterpreter = new (EmailAlg ~> Future) {
    override def apply[A](fa: EmailAlg[A]): Future[A] = fa match {
      case SendEmail(email, subject, body) => /* use smtp */ Future.successful(())
    }
  }

  // Example
  val futureUserOrEmailInterpreter = futureUserInterpreter or futureEmailInterpreter

  val result: Future[Either[String, Unit]] =
    addPoints(UUID.randomUUID(), 10).foldMap(futureUserOrEmailInterpreter)
}
