package exceptions

class SerializationException(val failure: constants.Response.Failure)(implicit currentModule: String) extends Exception {
  val module: String = currentModule
}
