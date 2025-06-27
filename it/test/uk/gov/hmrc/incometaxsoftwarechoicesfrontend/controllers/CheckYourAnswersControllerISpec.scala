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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers

import org.scalatest.BeforeAndAfterEach
import play.api.http.Status._
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.{AccountingPeriodPage, AdditionalIncomeSourcesPage, BusinessIncomePage, OtherItemsPage}

class CheckYourAnswersControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  lazy val controller = app.injector.instanceOf[CheckYourAnswersController]

  def testUserFilters(answers: UserAnswers): UserFilters = UserFilters(SessionId, Some(answers))

  override def beforeEach(): Unit = {
    userFiltersRepository.collection.drop().toFuture()
    super.beforeEach()
  }

  "GET /check-your-answers" when {
    "there are no existing page answers" should {
      "display the Technical difficulties page" in {

        val res = SoftwareChoicesFrontend.getCheckYourAnswers

        res.status shouldBe INTERNAL_SERVER_ERROR
        res.body.contains("Sorry, we’re experiencing technical difficulties") shouldBe true
      }
    }
    "there is pre-filled data" should {
      "display the page with appropriate summary lists" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader,UkProperty,OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq(UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome,
            PrivatePensionIncome, ForeignDividends, ForeignInterest)).get
          .set(OtherItemsPage, Seq(PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans,
            MarriageAllowance, VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge)).get
          .set(AccountingPeriodPage, Seq(StandardUpdatePeriods)).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("check-your-answers.heading")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"),
          summaryListRow("Business income", Seq(SoleTrader,UkProperty,OverseasProperty)
            .map(vf => messages(s"check-your-answers.$vf")).mkString(" ")),
          summaryListRow("Other income", Seq(UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome,
            PrivatePensionIncome, ForeignDividends, ForeignInterest)
            .map(vf => messages(s"check-your-answers.$vf")).mkString(" ")),
          summaryListRow("Other items", Seq(PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans,
            MarriageAllowance, VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge)
            .map(vf => messages(s"check-your-answers.$vf")).mkString(" ")),
          summaryListRow("Accounting period", Seq(StandardUpdatePeriods)
            .map(vf => messages(s"check-your-answers.$vf")).mkString(" "))
        )
      }
    }
  }

  "POST /check-your-answers" must {
    s"return $SEE_OTHER" when {
      "there are no all-in-one vendors found" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq(UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome,
            PrivatePensionIncome, ForeignDividends, ForeignInterest)).get
          .set(OtherItemsPage, Seq(PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans,
            MarriageAllowance, VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge)).get
          .set(AccountingPeriodPage, Seq(StandardUpdatePeriods)).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.postCheckYourAnswers()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.CheckYourAnswersController.show().url)
        )
      }
      "there are all-in-one vendors found" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, Seq.empty).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.postCheckYourAnswers()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SearchSoftwareController.show.url)
        )
      }
    }
  }
}
