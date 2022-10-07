/*
 * Copyright 2022 HM Revenue & Customs
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
  }

  case object Agent extends VendorFilter {
    override val key: String = "agent"
    override val priority: Int = 2
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
  }

  case object ApplicationBased extends VendorFilter {
    override val key: String = "application-based"
    override val priority: Int = 2
  }

  case object RecordKeeping extends VendorFilter {
    override val key: String = "record-keeping"
    override val priority: Int = 1
  }

  case object Bridging extends VendorFilter {
    override val key: String = "bridging"
    override val priority: Int = 2
  }

  case object IncomeTax extends VendorFilter {
    override val key: String = "income-tax"
    override val priority: Int = 2
    override val alwaysDisplay: Boolean = true
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

  val filterKeyToFilter: Map[String, VendorFilter] = Seq(
    FreeVersion,
    FreeTrial,
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
    Welsh,
    Visual,
    Hearing,
    Motor,
    Cognitive
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

  val languagesFilters: Set[VendorFilter] = Set(Welsh, English)

  val businessTypeFilters: Set[VendorFilter] = Set(Individual, Agent)

  val compatibility: Set[VendorFilter] = Set(IncomeTax, Vat)

  val incomeTypeFilters: Set[VendorFilter] = Set(SoleTrader, UkProperty, OverseasProperty)

  val compatibleWithFilters: Set[VendorFilter] = Set(MicrosoftWindows, MacOS)

  val mobileAppFilters: Set[VendorFilter] = Set(Android, AppleIOS)

  val softwareTypeFilters: Set[VendorFilter] = Set(BrowserBased, ApplicationBased)

  val softwareForFilters: Set[VendorFilter] = Set(RecordKeeping, Bridging)

  val accessibilityFilters: Set[VendorFilter] = Set(Visual, Hearing, Motor, Cognitive)

  def allGroups(displayExtraPricingOptions: Boolean): Seq[(Set[VendorFilter], String)] = Seq(
    (accessibilityFilters, "accessibility"),
    (pricingFilters(displayExtraPricingOptions), "pricing"),
    (incomeTypeFilters, "income-type"),
    (compatibleWithFilters, "compatible-with"),
    (mobileAppFilters, "mobile-app"),
    (softwareTypeFilters, "software-type"),
    (softwareForFilters, "software-for"),
    (businessTypeFilters, "business-type"),
    (compatibility, "software-compatibility"),
    (languagesFilters, "language")
  )
  def detailPageGroups(displayExtraPricingOptions: Boolean): Seq[(Set[VendorFilter], String)] = Seq(
    (pricingFilters(displayExtraPricingOptions), "pricing"),
    (incomeTypeFilters, "income-type"),
    (compatibleWithFilters, "compatible-with"),
    (mobileAppFilters, "mobile-app"),
    (softwareTypeFilters, "software-type"),
    (softwareForFilters, "software-for"),
    (businessTypeFilters, "business-type"),
    (compatibility, "software-compatibility"),
    (languagesFilters, "language"),
    (accessibilityFilters, "accessibility")
  )
}