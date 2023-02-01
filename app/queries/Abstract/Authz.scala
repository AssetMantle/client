package queries.Abstract

import models.Abstract

object Authz {

  abstract class Authorization {
    def toSerializable: Abstract.Authorization
  }
}

