package queries.Abstract

import models.Abstract

object FeeGrant {
  abstract class FeeAllowance {
    def toSerializable: Abstract.FeeGrant.FeeAllowance
  }
}