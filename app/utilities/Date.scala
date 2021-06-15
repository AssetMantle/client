package utilities

import java.text.{DateFormat, ParseException, SimpleDateFormat}
import java.sql.Timestamp

object Date {
  def utilDateToSQLDate(utilDate: java.util.Date): java.sql.Date = new java.sql.Date(utilDate.getTime)

  def sqlDateToUtilDate(sqlDate: java.sql.Date): java.util.Date = new java.util.Date(sqlDate.getTime)

  def stringDateToTimeStamp(stringDate: String): Timestamp =
    try {
      Timestamp.valueOf(stringDate)
    } catch {
      case _: Exception => new Timestamp(System.currentTimeMillis())
    }

  def parseStringToDate(date: String): java.util.Date = new SimpleDateFormat(constants.External.Wallex.DATE_FORMAT).parse(date)

  def formatDate(date: java.util.Date): String  = new SimpleDateFormat(constants.External.Wallex.DATE_FORMAT).format(date)
}
