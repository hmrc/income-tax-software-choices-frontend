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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers

import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.IncomeAndDeduction.{BlindPersonsAllowance, CapitalGainsTax}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{FreeTrial, FreeVersion}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{IncomeAndDeduction, SoftwareVendorModel, SoftwareVendors, VendorFilter}

import java.time.LocalDate

object TestModels {
  val allFilters = Seq(
    VendorFilter.FreeVersion,
    VendorFilter.FreeTrial,
    VendorFilter.Individual,
    VendorFilter.Agent,
    VendorFilter.MicrosoftWindows,
    VendorFilter.MacOS,
    VendorFilter.Android,
    VendorFilter.AppleIOS,
    VendorFilter.BrowserBased,
    VendorFilter.ApplicationBased,
    VendorFilter.Visual,
    VendorFilter.Hearing,
    VendorFilter.Motor,
    VendorFilter.Cognitive
  )

  val allIncomeAndDeductions = Seq(
    IncomeAndDeduction.BlindPersonsAllowance,
    IncomeAndDeduction.CapitalGainsTax,
    IncomeAndDeduction.ComplexPartnerships,
    IncomeAndDeduction.ConstructionIndustryScheme,
    IncomeAndDeduction.Employment,
    IncomeAndDeduction.ForeignIncome,
    IncomeAndDeduction.GiftAid,
    IncomeAndDeduction.HighIncomeChildBenefit,
    IncomeAndDeduction.Investments,
    IncomeAndDeduction.LloydsUnderwriters,
    IncomeAndDeduction.MarriageAllowance,
    IncomeAndDeduction.MarriedAllowance,
    IncomeAndDeduction.MemberOfParliament,
    IncomeAndDeduction.MinisterOfReligion,
    IncomeAndDeduction.PartnerIncome,
    IncomeAndDeduction.PAYE,
    IncomeAndDeduction.PensionContributions,
    IncomeAndDeduction.Pensions,
    IncomeAndDeduction.PropertyBusiness,
    IncomeAndDeduction.ResidenceAndRemittance,
    IncomeAndDeduction.SAAdditionalIncome,
    IncomeAndDeduction.SelfEmployment,
    IncomeAndDeduction.SimplePartnerships,
    IncomeAndDeduction.StatePension,
    IncomeAndDeduction.StudentLoans,
    IncomeAndDeduction.UKDividends,
    IncomeAndDeduction.UKInterest
  )

  val fullSoftwareVendorModel: SoftwareVendorModel = SoftwareVendorModel(
    name = "software vendor name",
    url = "/test-url",
    email = Some("test@software-vendor-name.com"),
    phone = Some("00000 000 000"),
    website = "software-vendor-name.com",
    filters = allFilters,
    incomeAndDeductions = allIncomeAndDeductions,
    accessibilityStatementLink = Some("software-vendor-accessibility.com")
  )

  val testVendorOne: SoftwareVendorModel = SoftwareVendorModel(
    "test software vendor one",
    "/test-url-one",
    Some("test@software-vendor-name-one.com"),
    Some("11111 111 111"),
    "software-vendor-name-one.com",
    Seq(FreeVersion),
    Seq(BlindPersonsAllowance),
    accessibilityStatementLink = Some("software-vendor-accessibility.com")
  )

  val testVendorTwo: SoftwareVendorModel = SoftwareVendorModel(
    "test software vendor two",
    "/test-url-two",
    Some("test@software-vendor-name-two.com"),
    Some("22222 222 222"),
    "software-vendor-name-two.com",
    Seq(FreeTrial),
    Seq(CapitalGainsTax)
  )

  val testVendorThree: SoftwareVendorModel = SoftwareVendorModel(
    "test software vendor three",
    "/test-url-three",
    Some("test@software-vendor-name-three.com"),
    Some("33333 333 333"),
    "software-vendor-name-three.com",
    Seq(FreeTrial, FreeVersion),
    Seq(BlindPersonsAllowance, CapitalGainsTax)
  )

  val fullSoftwareVendorsModel: SoftwareVendors = SoftwareVendors(
    lastUpdated = LocalDate.of(2022,12,2),
    vendors = Seq(
      fullSoftwareVendorModel.copy(
        filters = Seq.empty[VendorFilter],
        incomeAndDeductions = Seq.empty[IncomeAndDeduction]
      )
    )
  )
}
