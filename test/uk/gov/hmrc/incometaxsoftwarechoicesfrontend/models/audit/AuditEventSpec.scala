/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.audit

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import uk.gov.hmrc.http.SessionKeys.sessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod.FirstAprilToThirtyFirstMarch
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.SoleTraderOrLandlord
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters, UserType, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.*

class AuditEventSpec extends PlaySpec {

  "SearchResultsEvent" should {
    "have the correct auditType" in {
      val auditEvent = SearchResultsEvent(UserFilters("test-id"), Seq.empty)

      auditEvent.auditType mustBe "SoftwareChoicesSearchResults"
    }

    "convert data into correct json detail" when {
      "there are minimal filters and no vendor results" in {
        val userAnswers = UserAnswers().set(UserTypePage, UserType.Agent).get
        val userFilters = UserFilters(
          id = "test-id",
          answers = Some(userAnswers),
          finalFilters = Seq(Agent)
        )

        val auditEvent = SearchResultsEvent(userFilters, Seq.empty[String])

        val expectedDetail = Json.obj(
          "userAnswers" -> Json.obj(
            "userType" -> "agent",
            "businessIncome" -> Json.arr(),
            "additionalIncome" -> Json.arr(),
            "otherItems" -> Json.arr(),
            "accountingPeriod" -> ""
          ),
          "filtersApplied" -> Json.arr("agent"),
          "vendorsList" -> Json.arr(),
          "vendorsListCount" -> 0
        )

        Json.toJson(auditEvent) mustBe expectedDetail
      }

      "there are many filters with vendor results" in {

        val userAnswers: UserAnswers = UserAnswers()
          .set(UserTypePage, SoleTraderOrLandlord).get
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq(UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome)).get
          .set(OtherItemsPage, Seq(CharitableGiving, CapitalGainsTax, StudentLoans, MarriageAllowance, HighIncomeChildBenefitCharge)).get
          .set(AccountingPeriodPage, FirstAprilToThirtyFirstMarch).get

        val finalFilters: Seq[VendorFilter] = Seq(
          Individual, QuarterlyUpdates, TaxReturn,
          SoleTrader, UkProperty, OverseasProperty,
          UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome,
          CharitableGiving, CapitalGainsTax, StudentLoans, MarriageAllowance, HighIncomeChildBenefitCharge,
          CalendarUpdatePeriods, FreeVersion, Visual, Cognitive)

        val userFilters = UserFilters(sessionId, Some(userAnswers), finalFilters)
        val vendorsList = Seq("Test Vendor 1", "Test Vendor 2", "Test Vendor 3")

        val event = SearchResultsEvent(userFilters, vendorsList)

        val expectedDetail = Json.obj(
          "userAnswers" -> Json.obj(
            "userType" -> "soleTraderOrLandlord",
            "businessIncome" -> Json.arr("soleTrader", "ukProperty", "overseasProperty"),
            "additionalIncome" -> Json.arr("ukInterest", "constructionIndustryScheme", "employment(PAYE)", "ukDividends", "statePensionIncome"),
            "otherItems" -> Json.arr("charitableGiving", "capitalGains", "studentLoans", "marriageAllowance", "highIncomeChildBenefitCharge"),
            "accountingPeriod" -> "1st April to 31st March"
          ),
          "filtersApplied" -> Json.arr(
            "individual",
            "quarterlyUpdates",
            "taxReturn",
            "soleTrader",
            "ukProperty",
            "overseasProperty",
            "ukInterest",
            "constructionIndustryScheme",
            "employment(PAYE)",
            "ukDividends",
            "statePensionIncome",
            "charitableGiving",
            "capitalGains",
            "studentLoans",
            "marriageAllowance",
            "highIncomeChildBenefitCharge",
            "calendarUpdatePeriods",
            "freeVersion",
            "visual",
            "cognitive"
          ),
          "vendorsList" -> Json.arr("Test Vendor 1", "Test Vendor 2", "Test Vendor 3"),
          "vendorsListCount" -> 3
        )
        
        Json.toJson(event) mustBe expectedDetail
      }
    }
  }

}
