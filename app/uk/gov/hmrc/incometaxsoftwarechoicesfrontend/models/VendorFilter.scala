/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models

import play.api.libs.json.{JsString, Reads, Writes, __}

import scala.language.implicitConversions

sealed trait VendorFilter {
  val key: String
  val priority: Int
  val showHint: Boolean = false
  val auditDescription: String

  override def toString: String = key
}

object VendorFilter {

  implicit def magicToString(vendorFilter: VendorFilter): String = vendorFilter.toString

  case object FreeTrial extends VendorFilter {
    override val key: String = "free-trial"
    override val priority: Int = 1
    override val auditDescription: String = "freeTrial"
  }

  case object FreeVersion extends VendorFilter {
    override val key: String = "free-version"
    override val priority: Int = 2
    override val showHint: Boolean = true
    override val auditDescription: String = "freeVersion"
  }

  case object SoleTrader extends VendorFilter {
    override val key: String = "sole-trader"
    override val priority: Int = 1
    override val auditDescription: String = "soleTrader"
  }

  case object UkProperty extends VendorFilter {
    override val key: String = "uk-property"
    override val priority: Int = 2
    override val auditDescription: String = "ukProperty"
  }

  case object OverseasProperty extends VendorFilter {
    override val key: String = "overseas-property"
    override val priority: Int = 3
    override val auditDescription: String = "overseasProperty"
  }

  case object RecordKeeping extends VendorFilter {
    override val key: String = "record-keeping"
    override val priority: Int = 1
    override val showHint: Boolean = true
    override val auditDescription: String = "recordKeeping"
  }

  case object Bridging extends VendorFilter {
    override val key: String = "bridging"
    override val priority: Int = 2
    override val showHint: Boolean = false
    override val auditDescription: String = "bridging"
  }

  case object Vat extends VendorFilter {
    override val key: String = "vat"
    override val priority: Int = 1
    override val auditDescription: String = "vat"
  }

  case object Visual extends VendorFilter {
    override val key: String = "visual"
    override val priority: Int = 1
    override val auditDescription: String = "visual"
  }

  case object Hearing extends VendorFilter {
    override val key: String = "hearing"
    override val priority: Int = 2
    override val auditDescription: String = "hearing"
  }

  case object Motor extends VendorFilter {
    override val key: String = "motor"
    override val priority: Int = 3
    override val auditDescription: String = "motor"
  }

  case object Cognitive extends VendorFilter {
    override val key: String = "cognitive"
    override val priority: Int = 4
    override val auditDescription: String = "cognitive"
  }

  case object QuarterlyUpdates extends VendorFilter {
    override val key: String = "quarterly-updates"
    override val priority: Int = 3
    override val auditDescription: String = "quarterlyUpdates"
  }

  case object TaxReturn extends VendorFilter {
    override val key: String = "tax-return"
    override val priority: Int = 3
    override val auditDescription: String = "taxReturn"
  }

  case object StandardUpdatePeriods extends VendorFilter {
    override val key: String = "standard-update-periods"
    override val priority: Int = 3
    override val auditDescription: String = "standardUpdatePeriods"
  }

  case object CalendarUpdatePeriods extends VendorFilter {
    override val key: String = "calendar-update-periods"
    override val priority: Int = 4
    override val showHint: Boolean = true
    override val auditDescription: String = "calendarUpdatePeriods"
  }

  case object ConstructionIndustryScheme extends VendorFilter {
    override val key: String = "construction-industry-scheme"
    override val priority: Int = 1
    override val auditDescription: String = "constructionIndustryScheme"
  }

  case object CapitalGainsTax extends VendorFilter {
    override val key: String = "capital-gains-tax"
    override val priority: Int = 2
    override val auditDescription: String = "capitalGains"
  }

  case object Employment extends VendorFilter {
    override val key: String = "employment"
    override val priority: Int = 3
    override val auditDescription: String = "employment(PAYE)"
  }

  case object ForeignIncome extends VendorFilter {
    override val key: String = "foreign-income"
    override val priority: Int = 4
    override val auditDescription: String = "foreignIncome"
  }

  case object UkDividends extends VendorFilter {
    override val key: String = "uk-dividends"
    override val priority: Int = 5
    override val auditDescription: String = "ukDividends"
  }

  case object UkInterest extends VendorFilter {
    override val key: String = "uk-interest"
    override val priority: Int = 6
    override val auditDescription: String = "ukInterest"
  }

  case object ForeignDividends extends VendorFilter {
    override val key: String = "foreign-dividends"
    override val priority: Int = 7
    override val auditDescription: String = "foreignDividends"
  }

  case object ForeignInterest extends VendorFilter {
    override val key: String = "foreign-interest"
    override val priority: Int = 8
    override val auditDescription: String = "foreignInterest"
  }

  case object CharitableGiving extends VendorFilter {
    override val key: String = "charitable-giving"
    override val priority: Int = 1
    override val auditDescription: String = "charitableGiving"
  }

  case object HighIncomeChildBenefitCharge extends VendorFilter {
    override val key: String = "high-income-child-benefit-charge"
    override val priority: Int = 2
    override val auditDescription: String = "highIncomeChildBenefitCharge"
  }

  case object StudentLoans extends VendorFilter {
    override val key: String = "student-loans"
    override val priority: Int = 3
    override val auditDescription: String = "studentLoans"
  }

  case object VoluntaryClass2NationalInsurance extends VendorFilter {
    override val key: String = "voluntary-class-2-national-insurance"
    override val priority: Int = 4
    override val auditDescription: String = "voluntaryClass2NationalInsurance"
  }

  case object StatePensionIncome extends VendorFilter {
    override val key: String = "state-pension-income"
    override val priority: Int = 1
    override val auditDescription: String = "statePensionIncome"
  }

  case object PrivatePensionIncome extends VendorFilter {
    override val key: String = "private-pension-income"
    override val priority: Int = 2
    override val auditDescription: String = "privatePensionIncome"
  }

  case object PaymentsIntoAPrivatePension extends VendorFilter {
    override val key: String = "payments-into-a-private-pension"
    override val priority: Int = 3
    override val auditDescription: String = "paymentsIntoPrivatePension"
  }

  case object MarriageAllowance extends VendorFilter {
    override val key: String = "marriage-allowance"
    override val priority: Int = 1
    override val auditDescription: String = "marriageAllowance"
  }

  case object Agent extends VendorFilter {
    override val key: String = "agent"
    override val priority: Int = 1
    override val auditDescription: String = "agent"
  }

  case object Individual extends VendorFilter {
    override val key: String = "individual"
    override val priority: Int = 1
    override val auditDescription: String = "individual"
  }

  case object PartnerIncome extends VendorFilter {
    override val key: String = "partner-income"
    override val priority: Int = 1
    override val auditDescription: String = "partnerIncome"
  }

  case object AveragingAdjustment extends VendorFilter {
    override val key: String = "averaging-adjustment"
    override val priority: Int = 1
    override val auditDescription: String = "averagingAdjustment"
  }

  case object FosterCarer extends VendorFilter {
    override val key: String = "foster-carer"
    override val priority: Int = 1
    override val auditDescription: String = "fosterCarer"
  }

  case object TrustIncome extends VendorFilter {
    override val key: String = "trust-income"
    override val priority: Int = 1
    override val auditDescription: String = "trustIncome"
  }

  case object DesktopApplication extends VendorFilter {
    override val key: String = "desktop-based"
    override val priority: Int = 1
    override val auditDescription: String = "desktopApplication"
  }

  case object WebBrowser extends VendorFilter {
    override val key: String = "web-browser"
    override val priority: Int = 1
    override val auditDescription: String = "webBrowser"
  }

  case object MicrosoftWindows extends VendorFilter {
    override val key: String = "microsoft-windows"
    override val priority: Int = 1
    override val auditDescription: String = "microsoftWindows"
  }

  case object MacOS extends VendorFilter {
    override val key: String = "mac-os"
    override val priority: Int = 2
    override val auditDescription: String = "macOS"
  }

  case object Linux extends VendorFilter {
    override val key: String = "linux"
    override val priority: Int = 3
    override val auditDescription: String = "linux"
  }

  case object Android extends VendorFilter {
    override val key: String = "android"
    override val priority: Int = 1
    override val auditDescription: String = "android"
  }

  case object Apple extends VendorFilter {
    override val key: String = "apple-ios"
    override val priority: Int = 2
    override val auditDescription: String = "apple"
  }

  case object English extends VendorFilter {
    override val key: String = "english"
    override val priority: Int = 1
    override val auditDescription: String = "english"
  }

  val filterKeyToFilter: Map[String, VendorFilter] = Seq(
    FreeVersion,
    QuarterlyUpdates,
    TaxReturn,
    StandardUpdatePeriods,
    CalendarUpdatePeriods,
    SoleTrader,
    UkProperty,
    OverseasProperty,
    RecordKeeping,
    Bridging,
    Vat,
    Visual,
    Hearing,
    Motor,
    Cognitive,
    ConstructionIndustryScheme,
    CapitalGainsTax,
    Employment,
    ForeignIncome,
    UkDividends,
    UkInterest,
    ForeignDividends,
    ForeignInterest,
    CharitableGiving,
    HighIncomeChildBenefitCharge,
    StudentLoans,
    VoluntaryClass2NationalInsurance,
    StatePensionIncome,
    PrivatePensionIncome,
    PaymentsIntoAPrivatePension,
    MarriageAllowance,
    Agent,
    Individual,
    PartnerIncome,
    AveragingAdjustment,
    FosterCarer,
    TrustIncome,
    DesktopApplication,
    WebBrowser,
    MicrosoftWindows,
    MacOS,
    Linux,
    Android,
    Apple,
    English
  ).map(value => value.key -> value).toMap

  implicit val reads: Reads[VendorFilter] = __.read[String] map filterKeyToFilter
  implicit val writes: Writes[VendorFilter] = Writes(JsString(_))
  implicit val optReads: Reads[Option[Seq[VendorFilter]]] = Reads.optionWithNull[Seq[VendorFilter]]
}

object VendorFilterGroups {

  import VendorFilter._

  val pricingFilters: Set[VendorFilter] = Set(FreeVersion)

  val compatibility: Set[VendorFilter] = Set(Vat)

  val suitableForFilters: Set[VendorFilter] = Set(
    SoleTrader,
    UkProperty,
    OverseasProperty
  )

  val softwareForFilters: Set[VendorFilter] = Set(
    Bridging,
    RecordKeeping
  )

  val accessibilityFilters: Set[VendorFilter] = Set(
    Visual,
    Hearing,
    Motor,
    Cognitive
  )

  val accountingPeriodFilters: Set[VendorFilter] = Set(
    StandardUpdatePeriods,
    CalendarUpdatePeriods
  )

  val submissionTypeFilters: Set[VendorFilter] = Set(
    QuarterlyUpdates,
    TaxReturn
  )
  
  val compatibleWith: Set[VendorFilter] = Set(
    MicrosoftWindows,
    MacOS,
    Linux
  )

  val mobileApp: Set[VendorFilter] = Set(
    Android,
    Apple
  )

  val languageFilter: Set[VendorFilter] = Set(
    English
  )

  val userPageFilters: Set[VendorFilter] = Set(
    SoleTrader,
    UkProperty,
    OverseasProperty,
    UkInterest,
    ConstructionIndustryScheme,
    Employment,
    UkDividends,
    StatePensionIncome,
    PrivatePensionIncome,
    ForeignDividends,
    ForeignInterest,
    PaymentsIntoAPrivatePension,
    CharitableGiving,
    CapitalGainsTax,
    StudentLoans,
    MarriageAllowance,
    VoluntaryClass2NationalInsurance,
    HighIncomeChildBenefitCharge,
    StandardUpdatePeriods,
    CalendarUpdatePeriods
  )

  // product details page groups //
  val featuresProvidedGroup: List[VendorFilter] = List(
    FreeVersion, RecordKeeping, Bridging, Agent, Individual, StandardUpdatePeriods, CalendarUpdatePeriods
  )

  val incomeSourcesGroup: List[VendorFilter] = List(
    SoleTrader,
    UkProperty,
    OverseasProperty
  )

  val userTypeFilters: Set[VendorFilter] = Set(
    Agent,
    Individual
  )

  val mandatoryFiltersForIndividuals: Set[VendorFilter] = Set(
    TaxReturn,
    QuarterlyUpdates
  )

  def allGroups(isAgent: Boolean): Seq[(Set[VendorFilter], String)] =
    if (isAgent) {
      Seq((userTypeFilters, "user-type")) ++ groups
    } else {
      groups
    }

  private val groups: Seq[(Set[VendorFilter], String)] = Seq(
    (pricingFilters, "pricing"),
    (Set(Bridging), "software-for"),
    (compatibility, "software-compatibility"),
    (accessibilityFilters, "accessibility")
  )

  val nonMandatedIncomeGroup: List[VendorFilter] = List(
      UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome, PrivatePensionIncome, PartnerIncome,
      ForeignDividends, ForeignInterest, PaymentsIntoAPrivatePension, CharitableGiving,
      CapitalGainsTax, StudentLoans, MarriageAllowance, VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge
     )

  val softwareTypeGroup: List[VendorFilter] = List(DesktopApplication, WebBrowser)
  val compatibleWithGroup: List[VendorFilter] = List(MicrosoftWindows, MacOS, Linux)
  val mobileGroup: List[VendorFilter] = List(Android, Apple)
  val languageGroup: List[VendorFilter] = List(English)

  val quarterlyReturnsGroup: List[VendorFilter] = List(QuarterlyUpdates) ++ incomeSourcesGroup

  val mandatoryFilterGroup: List[VendorFilter] =
    userTypeFilters.toList ++
      accountingPeriodFilters ++
      groups.flatMap(_._1.toList)
}
