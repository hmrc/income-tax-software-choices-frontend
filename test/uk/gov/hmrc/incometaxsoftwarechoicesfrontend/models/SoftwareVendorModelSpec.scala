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

import org.scalatestplus.play.PlaySpec
import play.api.libs.json._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.TestModels.fullSoftwareVendorModel

class SoftwareVendorModelSpec extends PlaySpec {

  val fullJson: JsObject = Json.obj(
    "name" -> "software vendor name",
    "url" -> "/test-url",
    "email" -> "test@software-vendor-name.com",
    "phone" -> "00000 000 000",
    "website" -> "software-vendor-name.com",
    "filters" -> Json.arr(
      VendorFilter.FreeVersion.key,
      VendorFilter.FreeTrial.key,
      VendorFilter.Individual.key,
      VendorFilter.Agent.key,
      VendorFilter.MicrosoftWindows.key,
      VendorFilter.MacOS.key,
      VendorFilter.Android.key,
      VendorFilter.AppleIOS.key,
      VendorFilter.BrowserBased.key,
      VendorFilter.ApplicationBased.key,
      VendorFilter.Visual.key,
      VendorFilter.Hearing.key,
      VendorFilter.Motor.key,
      VendorFilter.Cognitive.key
    ),
    "incomeAndDeductions" -> Json.arr(
      IncomeAndDeduction.BlindPersonsAllowance.key,
      IncomeAndDeduction.CapitalGainsTax.key,
      IncomeAndDeduction.ComplexPartnerships.key,
      IncomeAndDeduction.ConstructionIndustryScheme.key,
      IncomeAndDeduction.Employment.key,
      IncomeAndDeduction.ForeignIncome.key,
      IncomeAndDeduction.GiftAid.key,
      IncomeAndDeduction.HighIncomeChildBenefit.key,
      IncomeAndDeduction.Investments.key,
      IncomeAndDeduction.LloydsUnderwriters.key,
      IncomeAndDeduction.MarriageAllowance.key,
      IncomeAndDeduction.MarriedAllowance.key,
      IncomeAndDeduction.MemberOfParliament.key,
      IncomeAndDeduction.MinisterOfReligion.key,
      IncomeAndDeduction.PartnerIncome.key,
      IncomeAndDeduction.PAYE.key,
      IncomeAndDeduction.PensionContributions.key,
      IncomeAndDeduction.Pensions.key,
      IncomeAndDeduction.PropertyBusiness.key,
      IncomeAndDeduction.ResidenceAndRemittance.key,
      IncomeAndDeduction.SAAdditionalIncome.key,
      IncomeAndDeduction.SelfEmployment.key,
      IncomeAndDeduction.SimplePartnerships.key,
      IncomeAndDeduction.StatePension.key,
      IncomeAndDeduction.StudentLoans.key,
      IncomeAndDeduction.UKDividends.key,
      IncomeAndDeduction.UKInterest.key
    ),
    "accessibilityStatementLink" -> "software-vendor-accessibility.com"
  )

  "SoftwareVendorModel" must {
    "read from json correctly" when {
      "the json is complete" in {
        Json.fromJson[SoftwareVendorModel](fullJson) mustBe JsSuccess(fullSoftwareVendorModel)
      }
      "the json has no contact details" in {
        Json.fromJson[SoftwareVendorModel](
          fullJson - "email" - "phone"
        ) mustBe JsSuccess(fullSoftwareVendorModel.copy(email = None, phone = None))
      }
      "the json has no filter options" in {
        Json.fromJson[SoftwareVendorModel](
          fullJson - "filters" ++ Json.obj("filters" -> Json.arr())
        ) mustBe JsSuccess(fullSoftwareVendorModel.copy(filters = Seq.empty[VendorFilter]))
      }
      "the json has no income and deduction coverage" in {
        Json.fromJson[SoftwareVendorModel](
          fullJson - "incomeAndDeductions" ++ Json.obj("incomeAndDeductions" -> Json.arr())
        ) mustBe JsSuccess(fullSoftwareVendorModel.copy(incomeAndDeductions = Seq.empty[IncomeAndDeduction]))
      }
      "there is no accessibility statement link" in {
        Json.fromJson[SoftwareVendorModel](
          fullJson - "accessibilityStatementLink"
        ) mustBe JsSuccess(fullSoftwareVendorModel.copy(accessibilityStatementLink = None))
      }
    }
    "fail to read json" when {
      "name is missing" in {
        val json: JsObject = fullJson - "name"
        Json.fromJson[SoftwareVendorModel](json) mustBe JsError(JsPath \ "name", "error.path.missing")
      }
      "url is missing" in {
        val json: JsObject = fullJson - "url"
        Json.fromJson[SoftwareVendorModel](json) mustBe JsError(JsPath \ "url", "error.path.missing")
      }
      "filters is missing" in {
        val json: JsObject = fullJson - "filters"
        Json.fromJson[SoftwareVendorModel](json) mustBe JsError(JsPath \ "filters", "error.path.missing")
      }
      "incomeAndDeductions is missing" in {
        val json: JsObject = fullJson - "incomeAndDeductions"
        Json.fromJson[SoftwareVendorModel](json) mustBe JsError(JsPath \ "incomeAndDeductions", "error.path.missing")
      }
    }
  }

}
