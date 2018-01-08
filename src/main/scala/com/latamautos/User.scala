package com.latamautos

import java.util.UUID

case class User(id: UUID, email: String, loyaltyPoints: Int) {
  def copyAndAddPoints(pointsToAdd: Int): User = {
    this.copy(loyaltyPoints = this.loyaltyPoints + pointsToAdd)
  }
}
