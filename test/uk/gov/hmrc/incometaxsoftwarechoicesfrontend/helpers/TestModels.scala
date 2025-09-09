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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers

import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{Intent, SoftwareVendorModel, SoftwareVendors, VendorFilter}

import java.time.LocalDate

object TestModels {
  private val allFilters = Seq(
    VendorFilter.FreeVersion,
    VendorFilter.Visual,
    VendorFilter.Hearing,
    VendorFilter.Motor,
    VendorFilter.Cognitive
  )

  val fullSoftwareVendorModel: SoftwareVendorModel = SoftwareVendorModel(
    name = "software vendor name",
    email = Some("test@software-vendor-name.com"),
    phone = Some("00000 000 000"),
    website = "software-vendor-name.com",
    filters = allFilters,
    accessibilityStatementLink = Some("software-vendor-accessibility.com")
  )

  val testVendorOne: SoftwareVendorModel = SoftwareVendorModel(
    "test software vendor one",
    Some("test@software-vendor-name-one.com"),
    Some("11111 111 111"),
    "software-vendor-name-one.com",
    Seq(FreeVersion),
    intent = Some(Intent(
      dateDue = LocalDate.of(2026, 1, 1),
      filters = Seq(Vat)
    )),
    accessibilityStatementLink = Some("software-vendor-accessibility.com")
  )

  val testVendorTwo: SoftwareVendorModel = SoftwareVendorModel(
    "test software vendor two",
    Some("test@software-vendor-name-two.com"),
    Some("22222 222 222"),
    "software-vendor-name-two.com",
    Seq.empty,
  )

  val testVendorThree: SoftwareVendorModel = SoftwareVendorModel(
    "test software vendor three",
    Some("test@software-vendor-name-three.com"),
    Some("33333 333 333"),
    "software-vendor-name-three.com",
    Seq(FreeVersion),
  )

  val testVendorFour: SoftwareVendorModel = SoftwareVendorModel(
    "test software vendor four",
    Some("test@software-vendor-name-four.com"),
    Some("44444 444 444"),
    "software-vendor-name-four.com",
    Seq(SoleTrader, Individual, StandardUpdatePeriods)
  )

  val testVendorFive: SoftwareVendorModel = SoftwareVendorModel(
    "test software vendor five",
    Some("test@software-vendor-name-five.com"),
    Some("55555 555 555"),
    "software-vendor-name-five.com",
    Seq(SoleTrader, Individual, Agent, Motor)
  )

  val fullSoftwareVendorsModel: SoftwareVendors = SoftwareVendors(
    lastUpdated = LocalDate.of(2022,12,2),
    vendors = Seq(
      fullSoftwareVendorModel.copy(
        filters = Seq.empty[VendorFilter]
      )
    )
  )
}
