package com.latamautos

import cats.instances.string._
import cats.syntax.semigroup._

object Main extends App {
  println("Hello " |+| "Cats!")

  val f: Either[String, Int] = for {
    foo <- Right[String,Int](1).right
    bar <- Left[String,Int]("nope").right
  } yield foo + bar
}
