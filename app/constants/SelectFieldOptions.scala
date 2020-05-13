package constants

object SelectFieldOptions {
  val ASSET_TYPES: Seq[String] = Seq("Oil", "Coal", "Gold", "Wheat")
  val DELIVERY_TERMS: Seq[String] = Seq("FOB", "CIF", "CFR")
  val COUNTRIES: Seq[String] = Seq("INDIA", "BHUTAN", "NEPAL", "PAKISTAN", "BANGLADESH", "SRI LANKA", "CHINA")
  val PHYSICAL_DOCUMENTS_HANDLED_VIA: Seq[String] = Seq("BANK", "TRUST")
  val PAYMENT_TERMS: Seq[String] = Seq(constants.View.ONLY_SUPPLIER, constants.View.ONLY_BUYER, constants.View.BOTH_PARTIES)
  val MODE: Seq[String] = Seq("async", "sync", "block")
  val REFERENCE_DATES: Seq[String] = Seq("INVOICE_DATE", "SHIPPING_DATE")

  //memberCheck
  val MATCH_TYPE: Seq[String] = Seq("Exact","Close")
  val WHITELIST: Seq[String] = Seq("Apply","Ignore")
  val RESIDENCE: Seq[String] = Seq("ApplyPEP", "ApplyAll", "Ignore", "ApplySIP", "ApplyRCA")
  val PEP_JURISDICTION: Seq[String] = Seq("Apply","Ignore")
  val MATCH_DECISION: Seq[String] = Seq("Match", "NoMatch", "NotSure", "NotReviewed", "Invalid")
  val ASSESSED_RISK: Seq[String] = Seq("Unallocated", "Low", "Med", "High", "Invalid")

  val COUNTRY_CODES: Seq[String] = Seq(
    "+1",
    "+1340",
    "+1284",
    "+1784",
    "+1868",
    "+1649",
    "+1721",
    "+1787",
    "+1939",
    "+1664",
    "+1670",
    "+1758",
    "+1345",
    "+1869",
    "+1876",
    "+1671",
    "+1473",
    "+1809",
    "+1829",
    "+1849",
    "+1767",
    "+1242",
    "+1441",
    "+1246",
    "+1684",
    "+1264",
    "+1268",
    "+7",
    "+20",
    "+27",
    "+30",
    "+31",
    "+32",
    "+33",
    "+34",
    "+36",
    "+39",
    "+40",
    "+41",
    "+43",
    "+44",
    "+45",
    "+46",
    "+47",
    "+48",
    "+49",
    "+51",
    "+52",
    "+53",
    "+54",
    "+55",
    "+56",
    "+5",
    "+58",
    "+6",
    "+61",
    "+62",
    "+63",
    "+64",
    "+65",
    "+66",
    "+81",
    "+82",
    "+84",
    "+86",
    "+90",
    "+91",
    "+92",
    "+9",
    "+94",
    "+95",
    "+98",
    "+211",
    "+212",
    "+213",
    "+216",
    "+218",
    "+220",
    "+221",
    "+222",
    "+223",
    "+224",
    "+225",
    "+226",
    "+227",
    "+228",
    "+229",
    "+230",
    "+231",
    "+232",
    "+233",
    "+234",
    "+235",
    "+236",
    "+237",
    "+238",
    "+239",
    "+240",
    "+241",
    "+242",
    "+243",
    "+244",
    "+245",
    "+246",
    "+248",
    "+249",
    "+250",
    "+251",
    "+252",
    "+253",
    "+254",
    "+255",
    "+257",
    "+258",
    "+260",
    "+261",
    "+262",
    "+263",
    "+264",
    "+265",
    "+266",
    "+267",
    "+268",
    "+269",
    "+290",
    "+291",
    "+297",
    "+298",
    "+299",
    "+350",
    "+351",
    "+352",
    "+353",
    "+354",
    "+355",
    "+356",
    "+357",
    "+358",
    "+359",
    "+370",
    "+371",
    "+372",
    "+373",
    "+374",
    "+375",
    "+376",
    "+377",
    "+378",
    "+380",
    "+381",
    "+382",
    "+385",
    "+386",
    "+387",
    "+389",
    "+420",
    "+421",
    "+423",
    "+500",
    "+501",
    "+502",
    "+503",
    "+504",
    "+505",
    "+506",
    "+507",
    "+508",
    "+509",
    "+590",
    "+591",
    "+592",
    "+593",
    "+594",
    "+595",
    "+596",
    "+597",
    "+598",
    "+599",
    "+670",
    "+672",
    "+673",
    "+674",
    "+675",
    "+676",
    "+677",
    "+678",
    "+679",
    "+680",
    "+681",
    "+682",
    "+683",
    "+685",
    "+686",
    "+687",
    "+688",
    "+689",
    "+690",
    "+691",
    "+692",
    "+850",
    "+852",
    "+853",
    "+855",
    "+856",
    "+880",
    "+886",
    "+960",
    "+961",
    "+962",
    "+963",
    "+964",
    "+965",
    "+966",
    "+967",
    "+968",
    "+970",
    "+971",
    "+972",
    "+973",
    "+974",
    "+975",
    "+976",
    "+977",
    "+992",
    "+993",
    "+994",
    "+995",
    "+996",
    "+998"
  )
}
