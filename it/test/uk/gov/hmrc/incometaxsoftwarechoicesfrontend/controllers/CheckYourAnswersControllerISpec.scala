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
import play.api.http.Status.*
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.IntentFeature
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod.{OtherAccountingPeriod, SixthAprilToFifthApril}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.{AccountingPeriodPage, AdditionalIncomeSourcesPage, BusinessIncomePage, OtherItemsPage}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class CheckYourAnswersControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper with FeatureSwitching {

  s"GET ${routes.CheckYourAnswersController.show().url}" when {
    "there are no existing page answers" should {
      "redirect to the service index" in {
        val res = SoftwareChoicesFrontend.getCheckYourAnswers

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "there is pre-filled data" should {
      "display the page with appropriate summary lists" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
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
          summaryListRow("Income sources", Seq(SoleTrader, UkProperty, OverseasProperty)
            .map(vf => messages(s"business-income.$vf")).mkString(" ")),
          summaryListRow("Other income", Seq(UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome,
            PrivatePensionIncome, ForeignDividends, ForeignInterest)
            .map(vf => messages(s"additional.income.source-$vf")).mkString(" ")),
          summaryListRow("Other items", Seq(PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans,
            MarriageAllowance, VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge)
            .map(vf => messages(s"other-items.$vf")).mkString(" ")),
          summaryListRow("Accounting period", Set(OtherAccountingPeriod)
            .map(vf => messages(s"accounting-period.${vf.key}")).mkString(" "))
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
          summaryListRow("Income sources", Seq(SoleTrader, UkProperty, OverseasProperty)
            .map(vf => messages(s"business-income.$vf")).mkString(" ")),
          summaryListRow("Other income", messages(s"check-your-answers.none-selected")),
          summaryListRow("Other items", messages(s"check-your-answers.none-selected")),
          summaryListRow("Accounting period", Set(OtherAccountingPeriod)
            .map(vf => messages(s"accounting-period.${vf.key}")).mkString(" "))
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
      "intent feature switch is enabled" should {
        "redirect to the choosing software page when there are all-in-one vendors" in {
          enable(IntentFeature)
          val userAnswers = UserAnswers()
            .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
            .set(AdditionalIncomeSourcesPage, Seq.empty).get
            .set(OtherItemsPage, Seq.empty).get
            .set(AccountingPeriodPage, SixthAprilToFifthApril).get
          await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

          val res = SoftwareChoicesFrontend.postCheckYourAnswers()

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.ChoosingSoftwareController.show().url)
          )

          await(userFiltersRepository.get(SessionId)) match {
            case Some(uf) => uf.finalFilters shouldBe Seq(SoleTrader, UkProperty, OverseasProperty, StandardUpdatePeriods)
            case None => fail("No user filters found")
          }
        }
        "redirect to the zero results page when there are no all-in-one vendors" in {
          enable(IntentFeature)
          val userAnswers = UserAnswers()
            .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
            .set(AdditionalIncomeSourcesPage, Seq(ForeignInterest)).get
            .set(OtherItemsPage, Seq.empty).get
            .set(AccountingPeriodPage, SixthAprilToFifthApril).get
          await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

          val res = SoftwareChoicesFrontend.postCheckYourAnswers()

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.ZeroSoftwareResultsController.show().url)
          )

          await(userFiltersRepository.get(SessionId)) match {
            case Some(uf) => uf.finalFilters shouldBe Seq(SoleTrader, UkProperty, OverseasProperty, ForeignInterest, StandardUpdatePeriods)
            case None => fail("No user filters found")
          }
        }
      }
      "intent feature switch is disabled" should {
        "redirect to zero results page when there are no all-in-one vendors found" in {
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
        "redirect to software results page when there are all-in-one vendors found" in {
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

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(userFiltersRepository.collection.drop().toFuture())
    disable(IntentFeature)
  }

  lazy val controller: CheckYourAnswersController = app.injector.instanceOf[CheckYourAnswersController]

  def testUserFilters(answers: Option[UserAnswers]): UserFilters = UserFilters(SessionId, answers)

}
