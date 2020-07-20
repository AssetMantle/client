package feeders

import constants.Test
import io.gatling.jdbc.Predef.jdbcFeeder
import io.gatling.core.Predef._

object JDBCFeeder {

  def getUserType(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "userType" FROM master."Account" WHERE id = '$query'),'0') AS "userType";""")
    sqlQueryFeeder.apply().next()("userType").toString
  }

  def getOrganizationID(query: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master."Organization" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getUBOID(organizationID: String, firstName: String, lastName: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master."OrganizationUBO" WHERE "organizationID" = '$organizationID' AND "firstName" = '$firstName' AND "lastName" = '$lastName' FETCH FIRST ROW ONLY),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }


  def getZoneID(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s""" SELECT COALESCE((SELECT "id" FROM master."Zone" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getZoneStatus(query: String): Boolean = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s"""SELECT "status" FROM master."Zone" WHERE "accountID" = '$query';""")
    if (sqlQueryFeeder.apply().next()("status") == true) true
    else false
  }

  def getZoneInvitationToken(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s""" SELECT COALESCE((SELECT "id" FROM master_transaction."ZoneInvitation" WHERE "emailAddress" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getAssetID(traderID: String, assetType: String, assetDescription: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master."Asset" WHERE "ownerID" = '$traderID' AND "assetType" = '$assetType' AND "description" = '$assetDescription'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }


  def getVesselScanID(query: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "scanID" FROM member_check."VesselScan" WHERE "vesselName" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getCorporateScanID(query: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "scanID" FROM member_check."CorporateScan" WHERE "companyName" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getRequestIDForIssueFiatRequest(traderID: String, amount: String): String = {
    println("amount----------" + amount)
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM western_union."FiatRequest" WHERE "traderID" = '$traderID' AND "transactionAmount" = '$amount'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

  def getNegotiationStatus(query: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "status" FROM master."Negotiation" WHERE "id" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("status").toString
  }

  def getOrderStatus(query: String) = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "status" FROM master."Order" WHERE "id" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("status").toString
  }

  def getTraderID(query: String): String = {
    val sqlQueryFeeder = jdbcFeeder("jdbc:postgresql://" + Test.TEST_IP + ":5432/commit", "commit", "commit",
      s"""SELECT COALESCE((SELECT "id" FROM master."Trader" WHERE "accountID" = '$query'),'0') AS "id";""")
    sqlQueryFeeder.apply().next()("id").toString
  }

}
