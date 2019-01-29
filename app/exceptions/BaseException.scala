package exceptions

class BaseException(code: String)(implicit currentModule: String) extends Exception {
  val module: String = currentModule
  val message: String = module + "." + code
}
