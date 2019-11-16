package exceptions

class BaseException(val failure: constants.Response.Failure)(implicit currentModule: String) extends Exception {
  val module: String = currentModule

}
