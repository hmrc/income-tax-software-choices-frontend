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
  override def toString: String = key
}

object VendorFilter {

  implicit def magicToString(vendorFilter: VendorFilter): String = vendorFilter.toString

  case object FreeVersion extends VendorFilter {
    override val key: String = "free-version"
  }

  case object FreeTrial extends VendorFilter {
    override val key: String = "free-trial"
  }

  case object PaidFor extends VendorFilter {
    override val key: String = "paid-for"
  }

  case object SoleTrader extends VendorFilter {
    override val key: String = "sole-trader"
  }

  case object UkProperty extends VendorFilter {
    override val key: String = "uk-property"
  }

  case object OverseasProperty extends VendorFilter {
    override val key: String = "overseas-property"
  }

  case object Individual extends VendorFilter {
    override val key: String = "individual"
  }

  case object Agent extends VendorFilter {
    override val key: String = "agent"
  }

  case object MicrosoftWindows extends VendorFilter {
    override val key: String = "microsoft-windows"
  }

  case object MacOS extends VendorFilter {
    override val key: String = "mac-os"
  }

  case object Android extends VendorFilter {
    override val key: String = "android"
  }

  case object AppleIOS extends VendorFilter {
    override val key: String = "apple-ios"
  }

  case object BrowserBased extends VendorFilter {
    override val key: String = "browser-based"
  }

  case object ApplicationBased extends VendorFilter {
    override val key: String = "application-based"
  }

  case object RecordKeeping extends VendorFilter {
    override val key: String = "record-keeping"
  }

  case object Bridging extends VendorFilter {
    override val key: String = "bridging"
  }

  case object Vat extends VendorFilter {
    override val key: String = "vat"
  }

  case object IncomeTax extends VendorFilter {
    override val key: String = "income-tax"
  }

  case object Welsh extends VendorFilter {
    override val key: String = "welsh"
  }

  case object English extends VendorFilter {
    override val key: String = "english"
  }

  case object Visual extends VendorFilter {
    override val key: String = "visual"
  }

  case object Hearing extends VendorFilter {
    override val key: String = "hearing"
  }

  case object Motor extends VendorFilter {
    override val key: String = "motor"
  }

  case object Cognitive extends VendorFilter {
    override val key: String = "cognitive"
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
