package utilities

import exceptions.BaseException
import play.api.Logger

import java.sql.Timestamp
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.{Instant, LocalDateTime, ZoneId, ZonedDateTime}

object Date {

  private implicit val module: String = constants.Module.UTILITIES_DATE

  private implicit val logger: Logger = Logger(this.getClass)

  private val dateTimeFormat: LocalDateTime = LocalDateTime.of(2016, 8, 23, 13, 12, 45)

  def utilDateToSQLDate(utilDate: java.util.Date): java.sql.Date = new java.sql.Date(utilDate.getTime)

  def sqlDateToUtilDate(sqlDate: java.sql.Date): java.util.Date = new java.util.Date(sqlDate.getTime)

  def getTimeFromSqlTimestamp(sqlTime: java.sql.Timestamp): String = ZonedDateTime.parse(sqlTime.toInstant.toString).format(DateTimeFormatter.ofPattern("HH:mm:ss"))

  def stringDateToTimeStamp(stringDate: String): Timestamp = try {
    Timestamp.valueOf(stringDate)
  } catch {
    case _: Exception => new Timestamp(System.currentTimeMillis())
  }

  def bcTimestampToZonedDateTime(timestamp: String): ZonedDateTime = try {
    ZonedDateTime.parse(timestamp)
  } catch {
    case exception: Exception => logger.error(exception.getLocalizedMessage)
      ZonedDateTime.now()
  }

  def bcTimestampToString(timestamp: String): String = try {
    ZonedDateTime.parse(timestamp).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)).format(dateTimeFormat)
  } catch {
    case exception: Exception => logger.error(exception.getLocalizedMessage)
      timestamp
  }

  def isMature(completionTimestamp: String, currentTimeStamp: String): Boolean = try {
    val completionTime = ZonedDateTime.parse(completionTimestamp)
    val currentTime = ZonedDateTime.parse(currentTimeStamp)
    currentTime.isEqual(completionTime) || currentTime.isAfter(completionTime)
  } catch {
    case exception: Exception => logger.error(exception.getMessage)
      throw new BaseException(constants.Response.DATE_FORMAT_ERROR)
  }

  def isAfter(t1: String, t2: String): Boolean = try {
    ZonedDateTime.parse(t1).isAfter(ZonedDateTime.parse(t2))
  } catch {
    case exception: Exception => logger.error(exception.getMessage)
      throw new BaseException(constants.Response.DATE_FORMAT_ERROR)
  }

  def isBefore(t1: String, t2: String): Boolean = try {
    ZonedDateTime.parse(t1).isBefore(ZonedDateTime.parse(t2))
  } catch {
    case exception: Exception => logger.error(exception.getMessage)
      throw new BaseException(constants.Response.DATE_FORMAT_ERROR)
  }

  def addTime(timestamp: String, addEpochTime: Long): String = try {
    ZonedDateTime.ofInstant(Instant.ofEpochSecond(ZonedDateTime.parse(timestamp).toEpochSecond + addEpochTime), ZoneId.of("UTC")).format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
  } catch {
    case exception: Exception => logger.error(exception.getLocalizedMessage)
      throw new BaseException(constants.Response.INVALID_DATA_TYPE)
  }

  def addTime(t1: String, t2: String): String = try {
    ZonedDateTime.ofInstant(Instant.ofEpochSecond(ZonedDateTime.parse(t1).toEpochSecond + ZonedDateTime.parse(t2).toEpochSecond), ZoneId.of("UTC")).format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
  } catch {
    case exception: Exception => logger.error(exception.getMessage)
      throw new BaseException(constants.Response.DATE_FORMAT_ERROR)
  }

  def getEpoch(time: String): Long = {
    time.split("").last match {
      case "s" => time.dropRight(1).toLong
      case "m" => 60 * time.dropRight(1).toLong
      case "h" => 60 * 60 * time.dropRight(1).toLong
      case _ => throw new BaseException(constants.Response.DATE_FORMAT_ERROR)
    }
  }
}
