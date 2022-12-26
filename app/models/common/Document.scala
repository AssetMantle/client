package models.common

import models.common.BaseID.ClassificationID
import models.common.Qualified._

case class Document(classificationID: ClassificationID, immutables: Immutables, mutables: Mutables)
