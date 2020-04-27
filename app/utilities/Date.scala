package utilities

import java.text.{DateFormat, ParseException, SimpleDateFormat}
import java.sql.Timestamp

object Date {
  def utilDateToSQLDate(utilDate: java.util.Date): java.sql.Date = new java.sql.Date(utilDate.getTime)

  def sqlDateToUtilDate(sqlDate: java.sql.Date): java.util.Date = new java.util.Date(sqlDate.getTime)

  def stringDateToTimeStamp(stringDate: String): Timestamp = Timestamp.valueOf(stringDate)

}
