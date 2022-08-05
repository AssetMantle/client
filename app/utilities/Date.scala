package utilities

import exceptions.BaseException
import play.api.Logger
import play.api.libs.json._

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time._
import java.time.format.DateTimeFormatter
import java.util.{Date => javaDate}

object Date {

  private implicit val module: String = constants.Module.UTILITIES_DATE

  private implicit val logger: Logger = Logger(this.getClass)

  private val ddMMDateFormat = new SimpleDateFormat("dd/MM")

  private val YYYYMMDDDateFormat = new SimpleDateFormat("YYYY/MM/dd")

  def utilDateToSQLDate(utilDate: java.util.Date): java.sql.Date = new java.sql.Date(utilDate.getTime)

  def sqlDateToUtilDate(sqlDate: java.sql.Date): java.util.Date = new java.util.Date(sqlDate.getTime)

  def getTimeFromSqlTimestamp(sqlTime: java.sql.Timestamp): String = ZonedDateTime.parse(sqlTime.toInstant.toString).format(DateTimeFormatter.ofPattern("HH:mm:ss"))

  def stringDateToTimeStamp(stringDate: String): Timestamp = try {
    Timestamp.valueOf(stringDate)
  } catch {
    case _: Exception => new Timestamp(System.currentTimeMillis())
  }

  def getEpoch(time: String): Long = {
    time.split("").last match {
      case "s" => time.dropRight(1).toLong
      case "m" => 60 * time.dropRight(1).toLong
      case "h" => 60 * 60 * time.dropRight(1).toLong
      case _ => throw new BaseException(constants.Response.DATE_FORMAT_ERROR)
    }
  }

  def epochToDDMMString(value: Long): String = ddMMDateFormat.format(javaDate.from(Instant.ofEpochSecond(value)))

  def epochToYYYYMMDDString(value: Long): String = YYYYMMDDDateFormat.format(javaDate.from(Instant.ofEpochSecond(value)))

  def epochToDate(value: Long): java.util.Date = new java.util.Date(value * 1000)

  def isValidRFC3339(x: String): Boolean = try {
    ZonedDateTime.parse(x)
    true
  } catch {
    case exception: Exception => logger.error(exception.getMessage)
      throw new BaseException(constants.Response.DATE_FORMAT_ERROR)
  }

  class RFC3339(timestamp: String) {

    def zonedDateTime: ZonedDateTime = ZonedDateTime.parse(this.timestamp)

    def unix: Long = zonedDateTime.toEpochSecond

    override def toString: String = this.timestamp

    def isAfter(that: RFC3339): Boolean = try {
      this.zonedDateTime.isAfter(that.zonedDateTime)
    } catch {
      case exception: Exception => logger.error(exception.getMessage)
        throw new BaseException(constants.Response.DATE_FORMAT_ERROR)
    }

    def isBefore(that: RFC3339): Boolean = try {
      this.zonedDateTime.isBefore(that.zonedDateTime)
    } catch {
      case exception: Exception => logger.error(exception.getMessage)
        throw new BaseException(constants.Response.DATE_FORMAT_ERROR)
    }

    def isEqual(that: RFC3339): Boolean = this.zonedDateTime.isEqual(that.zonedDateTime)

    def isAfterOrEqual(that: RFC3339): Boolean = try {
      val thisTime = this.zonedDateTime
      val thatTime = that.zonedDateTime
      thisTime.isEqual(thatTime) || thisTime.isAfter(thatTime)
    } catch {
      case exception: Exception => logger.error(exception.getMessage)
        throw new BaseException(constants.Response.DATE_FORMAT_ERROR)
    }

    def isBeforeOrEqual(that: RFC3339): Boolean = try {
      val thisTime = this.zonedDateTime
      val thatTime = that.zonedDateTime
      thisTime.isEqual(thatTime) || thisTime.isBefore(thatTime)
    } catch {
      case exception: Exception => logger.error(exception.getMessage)
        throw new BaseException(constants.Response.DATE_FORMAT_ERROR)
    }

    def addEpoch(epoch: Long): RFC3339 = try {
      RFC3339(ZonedDateTime.ofInstant(Instant.ofEpochSecond(this.unix + epoch), ZoneId.of("UTC")).format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
    } catch {
      case exception: Exception => logger.error(exception.getLocalizedMessage)
        throw new BaseException(constants.Response.INVALID_DATA_TYPE)
    }

    def add(that: RFC3339): RFC3339 = try {
      RFC3339(ZonedDateTime.ofInstant(Instant.ofEpochSecond(this.unix + that.unix), ZoneId.of("UTC")).format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
    } catch {
      case exception: Exception => logger.error(exception.getMessage)
        throw new BaseException(constants.Response.DATE_FORMAT_ERROR)
    }

    def difference(that: RFC3339): Duration = Duration.between(this.zonedDateTime, that.zonedDateTime)

  }

  object RFC3339 {

    def apply(value: String): RFC3339 = if (isValidRFC3339(value)) new RFC3339(value) else throw new BaseException(constants.Response.DATE_FORMAT_ERROR)

    implicit val rfc3339Writes: Writes[RFC3339] = (rfc3339: RFC3339) => Json.toJson(rfc3339.toString)

    implicit val rfc3339Reads: Reads[RFC3339] = (json: JsValue) => json.validate[String].map(x => RFC3339(x))
  }
}
