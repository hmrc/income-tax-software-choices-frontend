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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod.{OtherAccountingPeriod, SixthAprilToFifthApril}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.{AccountingPeriodPage, AdditionalIncomeSourcesPage, BusinessIncomePage, OtherItemsPage}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

import java.time.Instant

class CheckYourAnswersControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  lazy val controller = app.injector.instanceOf[CheckYourAnswersController]
  private val testTime = Instant.now()

  def testUserFilters(answers: Option[UserAnswers]): UserFilters = UserFilters(SessionId, answers, lastUpdated = testTime)

  override def beforeEach(): Unit = {
    await(userFiltersRepository.collection.drop().toFuture())
    super.beforeEach()
  }

  "GET /check-your-answers" when {
    "there are no existing page answers" should {
      "display the Technical difficulties page" in {

        val res = SoftwareChoicesFrontend.getCheckYourAnswers

        res.status shouldBe INTERNAL_SERVER_ERROR
        res.body.contains("Sorry, there is a problem with the service") shouldBe true
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
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("check-your-answers.heading")} - ${PageContentBase.title} - GOV.UK"),
          summaryListRow("Business income", Seq(SoleTrader,UkProperty,OverseasProperty)
            .map(vf => messages(s"check-your-answers.$vf")).mkString(" ")),
          summaryListRow("Other income", Seq(UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome,
            PrivatePensionIncome, ForeignDividends, ForeignInterest)
            .map(vf => messages(s"check-your-answers.$vf")).mkString(" ")),
          summaryListRow("Other items", Seq(PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans,
            MarriageAllowance, VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge)
            .map(vf => messages(s"check-your-answers.$vf")).mkString(" ")),
          summaryListRow("Accounting period", Set(OtherAccountingPeriod)
            .map(vf => messages(s"check-your-answers.${vf.key}")).mkString(" "))
        )
      }
      "'None of these' is selected for other income and other items sources" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("check-your-answers.heading")} - ${PageContentBase.title} - GOV.UK"),
          summaryListRow("Business income", Seq(SoleTrader,UkProperty,OverseasProperty)
            .map(vf => messages(s"check-your-answers.$vf")).mkString(" ")),
          summaryListRow("Other income", messages(s"check-your-answers.none-selected")),
          summaryListRow("Other items", messages(s"check-your-answers.none-selected")),
          summaryListRow("Accounting period", Set(OtherAccountingPeriod)
            .map(vf => messages(s"check-your-answers.${vf.key}")).mkString(" "))
        )
      }
      "Business Incomes sources is not set" in {
        val userAnswers = UserAnswers()
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers
        res.status shouldBe INTERNAL_SERVER_ERROR
        res.body.contains("Sorry, there is a problem with the service") shouldBe true
      }
      "Other Incomes sources is not set" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers
        res.status shouldBe INTERNAL_SERVER_ERROR
        res.body.contains("Sorry, there is a problem with the service") shouldBe true
      }
      "Other Items sources is not set" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers
        res.status shouldBe INTERNAL_SERVER_ERROR
        res.body.contains("Sorry, there is a problem with the service") shouldBe true
      }
      "Accounting Period is not set" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers
        res.status shouldBe INTERNAL_SERVER_ERROR
        res.body.contains("Sorry, there is a problem with the service") shouldBe true
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
          .set(AccountingPeriodPage, SixthAprilToFifthApril).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.postCheckYourAnswers()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.ZeroSoftwareResultsController.show().url)
        )

        await(userFiltersRepository.get(SessionId)) match {
          case Some(uf) => uf.finalFilters shouldBe Seq(
            SoleTrader, UkProperty, OverseasProperty,
            UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome,
            PrivatePensionIncome, ForeignDividends, ForeignInterest,
            PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans,
            MarriageAllowance, VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge,
            StandardUpdatePeriods
          )
          case None => fail("No user filters found")
        }
      }
      "there are all-in-one vendors found" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.postCheckYourAnswers()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SearchSoftwareController.show().url)
        )

        await(userFiltersRepository.get(SessionId)) match {
            case Some(uf) => uf.finalFilters shouldBe Seq(SoleTrader, UkProperty, OverseasProperty)
            case None => fail("No user filters found")
        }
      }
    }
  }
}
