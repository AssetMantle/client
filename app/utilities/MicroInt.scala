package utilities

class MicroInt(val value:Long){

  def this(a:String)= this((a.toDouble * 1000000).toLong)

  def this(b:Int)= this((b*1000000).toLong)

  def this(c:Double)=this((c * 1000000).toLong)

  def string=(value.toDouble/1000000).toString

  def double=value.toDouble/1000000

}
