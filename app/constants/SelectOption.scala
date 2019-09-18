package constants


object SelectOption {

  class DropdownOption(val options: Seq[String])

  val ASSET_TYPE = new DropdownOption(Seq("Oil", "Coal", "Gold", "Wheat"))

  val DELIVERY_TERM = new DropdownOption(Seq("FOB", "CIF", "CFR"))

  val QUALITY = new DropdownOption(Seq("A+", "A", "B"))

  val TRADE_TYPE = new DropdownOption(Seq("POST TRADE"))

  val COUNTRY = new DropdownOption(Seq("INDIA", "BHUTAN", "NEPAL", "PAKISTAN", "BANGLADESH", "SRI LANKA", "CHINA"))

  val COMDEX = "COMDEX"

  val PHYSICAL_DOCUMENTS_HANDLED_VIA = new DropdownOption(Seq(COMDEX, "BANK", "TRUST"))

  val ONLY_SUPPLIER = "ONLY_SUPPLIER"

  val ONLY_BUYER = "ONLY_BUYER"

  val BOTH_PARTIES = "BOTH_PARTIES"

  val COMDEX_PAYMENT_TERMS = new DropdownOption(Seq(ONLY_SUPPLIER, ONLY_BUYER, BOTH_PARTIES))

}
