package utilities

object RandomString {

  def randomStringArray(length: Int) = {
    val r = new scala.util.Random
    val a = new Array[Char](length)
    val sb = new StringBuilder
    for (i <- 0 to length-1) {
      a(i) = r.nextPrintableChar
    }
    a.mkString
  }
}
