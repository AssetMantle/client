package utilities

import java.sql.Timestamp
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.{LocalDateTime, ZonedDateTime}

import exceptions.BaseException
import play.api.Logger

object Date {

  private implicit val module: String = constants.Module.UTILITIES_DATE

  private implicit val logger: Logger = Logger(this.getClass)

  private val dateTimeFormat: LocalDateTime = LocalDateTime.of(2016, 8, 23, 13, 12, 45)

  def utilDateToSQLDate(utilDate: java.util.Date): java.sql.Date = new java.sql.Date(utilDate.getTime)

  def sqlDateToUtilDate(sqlDate: java.sql.Date): java.util.Date = new java.util.Date(sqlDate.getTime)

  def stringDateToTimeStamp(stringDate: String): Timestamp =
    try {
      Timestamp.valueOf(stringDate)
    } catch {
      case _: Exception => new Timestamp(System.currentTimeMillis())
    }

  def bcTimestampToZonedDateTime(timestamp: String): ZonedDateTime = {
    try {
      ZonedDateTime.parse(timestamp)
    } catch {
      case exception: Exception => logger.error(exception.getLocalizedMessage)
        ZonedDateTime.now()
    }
  }

  def bcTimestampToString(timestamp: String): String = {
    try {
      ZonedDateTime.parse(timestamp).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)).format(dateTimeFormat)
    } catch {
      case exception: Exception => logger.error(exception.getLocalizedMessage)
        ""
    }
  }

  def isMature(initialTimestamp: String, finalTimeStamp: String): Boolean = {
    try {
      val initialTime = ZonedDateTime.parse(initialTimestamp)
      val finalTime = ZonedDateTime.parse(finalTimeStamp)
      finalTime.isEqual(initialTime) || finalTime.isAfter(initialTime)
    } catch {
      case exception: Exception => logger.error(exception.getMessage)
        throw new BaseException(constants.Response.DATE_FORMAT_ERROR)
    }
  }

}
