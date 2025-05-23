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

import play.api.libs.json.{Reads, __}

import scala.language.implicitConversions

sealed trait VendorFilter {
  val key: String
  val priority: Int
  val alwaysDisplay: Boolean = false
  val showHint: Boolean = false
  override def toString: String = key
}

object VendorFilter {

  implicit def magicToString(vendorFilter: VendorFilter): String = vendorFilter.toString

  case object FreeTrial extends VendorFilter {
    override val key: String = "free-trial"
    override val priority: Int = 1
  }

  case object FreeVersion extends VendorFilter {
    override val key: String = "free-version"
    override val priority: Int = 2
    override val showHint: Boolean = true
  }

  case object PaidFor extends VendorFilter {
    override val key: String = "paid-for"
    override val priority: Int = 3
  }

  case object SoleTrader extends VendorFilter {
    override val key: String = "sole-trader"
    override val priority: Int = 1
  }

  case object UkProperty extends VendorFilter {
    override val key: String = "uk-property"
    override val priority: Int = 2
  }

  case object OverseasProperty extends VendorFilter {
    override val key: String = "overseas-property"
    override val priority: Int = 3
  }

  case object Individual extends VendorFilter {
    override val key: String = "individual"
    override val priority: Int = 1
    override val showHint: Boolean = true
  }

  case object Agent extends VendorFilter {
    override val key: String = "agent"
    override val priority: Int = 2
    override val showHint: Boolean = true
  }

  case object MicrosoftWindows extends VendorFilter {
    override val key: String = "microsoft-windows"
    override val priority: Int = 1
  }

  case object MacOS extends VendorFilter {
    override val key: String = "mac-os"
    override val priority: Int = 2
  }

  case object Android extends VendorFilter {
    override val key: String = "android"
    override val priority: Int = 1
  }

  case object AppleIOS extends VendorFilter {
    override val key: String = "apple-ios"
    override val priority: Int = 2
  }

  case object BrowserBased extends VendorFilter {
    override val key: String = "browser-based"
    override val priority: Int = 1
    override val showHint: Boolean = true
  }

  case object ApplicationBased extends VendorFilter {
    override val key: String = "application-based"
    override val priority: Int = 2
    override val showHint: Boolean = true
  }

  case object RecordKeeping extends VendorFilter {
    override val key: String = "record-keeping"
    override val priority: Int = 1
    override val showHint: Boolean = true
  }

  case object Bridging extends VendorFilter {
    override val key: String = "bridging"
    override val priority: Int = 2
    override val showHint: Boolean = true
  }

  case object Vat extends VendorFilter {
    override val key: String = "vat"
    override val priority: Int = 1
  }

  case object English extends VendorFilter {
    override val key: String = "english"
    override val priority: Int = 2
    override val alwaysDisplay: Boolean = true
  }

  case object Welsh extends VendorFilter {
    override val key: String = "welsh"
    override val priority: Int = 1
  }

  case object Visual extends VendorFilter {
    override val key: String = "visual"
    override val priority: Int = 1
  }

  case object Hearing extends VendorFilter {
    override val key: String = "hearing"
    override val priority: Int = 2
  }

  case object Motor extends VendorFilter {
    override val key: String = "motor"
    override val priority: Int = 3
  }

  case object Cognitive extends VendorFilter {
    override val key: String = "cognitive"
    override val priority: Int = 4
  }

  case object Apr06ToApr05 extends VendorFilter {
    override val key: String = "apr6"
    override val priority: Int = 1
  }

  case object Apr01ToMar31 extends VendorFilter {
    override val key: String = "apr1"
    override val priority: Int = 2
    override val showHint: Boolean = true
  }

  case object QuarterlyUpdates extends VendorFilter {
    override val key: String = "quarterly-updates"
    override val priority: Int = 3
  }

  case object TaxReturn extends VendorFilter {
    override val key: String = "tax-return"
    override val priority: Int = 3
  }

  case object CconstructionIndustryScheme extends VendorFilter {
    override val key: String = "construction-industry-scheme"
    override val priority: Int = 1
  }

  case object CapitalGainsTax extends VendorFilter {
    override val key: String = "capital-gains-tax"
    override val priority: Int = 2
  }

  case object Employment extends VendorFilter {
    override val key: String = "employment"
    override val priority: Int = 3
  }

  case object ForeignIncome extends VendorFilter {
    override val key: String = "foreign-income"
    override val priority: Int = 4
  }

  case object UkDividends extends VendorFilter {
    override val key: String = "uk-dividends"
    override val priority: Int = 5
  }

  case object UkInterest extends VendorFilter {
    override val key: String = "uk-interest"
    override val priority: Int = 6
  }

  case object CharitableGiving extends VendorFilter {
    override val key: String = "charitable-giving"
    override val priority: Int = 1
  }

  case object HighIncomeChildBenefitCharge extends VendorFilter {
    override val key: String = "high-income-child-benefit-charge"
    override val priority: Int = 2
  }

  case object StudentLoans extends VendorFilter {
    override val key: String = "student-loans"
    override val priority: Int = 3
  }

  case object VoluntaryClass2NationalInsurance extends VendorFilter {
    override val key: String = "voluntary-class-2-national-insurance"
    override val priority: Int = 4
  }

  case object StatePensionIncome extends VendorFilter {
    override val key: String = "state-pension-income"
    override val priority: Int = 1
  }

  case object PrivatePensionIncome extends VendorFilter {
    override val key: String = "private-pension-income"
    override val priority: Int = 2
  }

  case object PaymentsIntoAPrivatePension extends VendorFilter {
    override val key: String = "payments-into-a-private-pension"
    override val priority: Int = 3
  }

  case object MarriageAllowance extends VendorFilter {
    override val key: String = "marriage-allowance"
    override val priority: Int = 1
  }

  val filterKeyToFilter: Map[String, VendorFilter] = Seq(
    FreeVersion,
    FreeTrial,
    QuarterlyUpdates,
    TaxReturn,
    PaidFor,
    SoleTrader,
    UkProperty,
    OverseasProperty,
    Individual,
    Agent,
    MicrosoftWindows,
    MacOS,
    Android,
    AppleIOS,
    BrowserBased,
    ApplicationBased,
    RecordKeeping,
    Bridging,
    Vat,
    English,
    Welsh,
    Visual,
    Hearing,
    Motor,
    Cognitive,
    Apr06ToApr05,
    Apr01ToMar31,
    CconstructionIndustryScheme,
    CapitalGainsTax,
    Employment,
    ForeignIncome,
    UkDividends,
    UkInterest,
    CharitableGiving,
    HighIncomeChildBenefitCharge,
    StudentLoans,
    VoluntaryClass2NationalInsurance,
    StatePensionIncome,
    PrivatePensionIncome,
    PaymentsIntoAPrivatePension,
    MarriageAllowance,
  ).map(value => value.key -> value).toMap

  implicit val reads: Reads[VendorFilter] = __.read[String] map filterKeyToFilter

}

object VendorFilterGroups {
  import VendorFilter._

  def pricingFilters(displayExtraPricingOptions: Boolean): Set[VendorFilter] =
    Set(
      Some(FreeTrial).filter(_ => displayExtraPricingOptions),
      Some(FreeVersion),
      Some(PaidFor).filter(_ => displayExtraPricingOptions)
    ).flatten.toSet

  val compatibility: Set[VendorFilter] = Set(
    Vat
  )

  def suitableForFilters(displayOverseasPropertyOption: Boolean): Set[VendorFilter] =
    Set(
      Some(SoleTrader),
      Some(UkProperty),
      Some(OverseasProperty).filter(_ => displayOverseasPropertyOption)
    ).flatten.toSet

  val softwareForFilters: Set[VendorFilter] = Set(
    RecordKeeping,
    Bridging
  )

  val accessibilityFilters: Set[VendorFilter] = Set(
    Visual,
    Hearing,
    Motor,
    Cognitive
  )

  val accountingPeriodFilters: Set[VendorFilter] = Set(
    Apr06ToApr05,
    Apr01ToMar31,
  )

  val submissionTypeFilters: Set[VendorFilter] = Set(
    QuarterlyUpdates,
    TaxReturn
  )

  val personalIncomeSources: Set[VendorFilter] = Set(
    CconstructionIndustryScheme,
    CapitalGainsTax,
    Employment,
    ForeignIncome,
    UkDividends,
    UkInterest,
  )

  val deductions: Set[VendorFilter] = Set(
    CharitableGiving,
    HighIncomeChildBenefitCharge,
    StudentLoans,
    VoluntaryClass2NationalInsurance,
  )

  val pensions: Set[VendorFilter] = Set(
    StatePensionIncome,
    PrivatePensionIncome,
    PaymentsIntoAPrivatePension,
  )

  val allowances: Set[VendorFilter] = Set(
    MarriageAllowance,
  )

  def allGroups(displayExtraPricingOptions: Boolean, displayOverseasPropertyOption: Boolean): Seq[(Set[VendorFilter], String)] = Seq(
    (pricingFilters(displayExtraPricingOptions), "pricing"),
    (softwareForFilters, "software-for"),
    (compatibility, "software-compatibility"),
    (accessibilityFilters, "accessibility"),
    (accountingPeriodFilters, "accounting-period"),
    (suitableForFilters(true), "suitable-for"),
    (personalIncomeSources, "personal-income-sources"),
    (deductions, "deductions"),
    (pensions, "pensions"),
    (allowances, "allowances"),
  )
 
}