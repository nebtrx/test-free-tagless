package com.latamautos.basic.freestyle

import java.util.UUID

import cats.{Monad, ~>}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits._
import com.latamautos.User
import freestyle.free._

object FreeMonad {

  @free trait UserRepository {
    def findUser(id: UUID): FS[User]
    def updateUser(id: User): FS[User]
  }

//  @module trait Persistence {
//    val userRepo: UserRepository
//  }

  implicit val userRepositoryHandler: UserRepository.Handler[Future] = new UserRepository.Handler[Future] {
    override def findUser(id: UUID): Future[User] = Future.failed(new Exception)
    override def updateUser(u: User): Future[User] = Future.successful(u) // Future.successful { println(u); "This could have been user input 1" }
  }


//  implicit val tagRepositoryHandler: UserRepository ~> Future = new (UserRepository ~> Future) {
//    override def apply[A](fa: UserRepository[A]): Future[A] = Future.successful(fa.)
//    }
//  }


  class UserRepositoryHandler extends UserRepository.Handler[Future] {
    override def findUser(id: UUID): Future[User] = Future.failed(new Exception)
    override def updateUser(u: User): Future[User] = Future.successful(u) // Future.successful { println(u); "This could have been user input 1" }
  }

  def program[F[_]:Monad](userId: UUID, pointsToAdd: Int)(implicit persistence: UserRepository[F]): FreeS[F, User] = {
    import persistence._
    
    for {
      u <- findUser(userId)
      updated <- updateUser(u)
    } yield updated
  }

//  program[Future](UUID.fromString("1"), 10)
}


