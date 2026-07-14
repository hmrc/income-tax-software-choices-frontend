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

import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.*
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod.{OtherAccountingPeriod, SixthAprilToFifthApril}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.{Check, Find}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.{Recognised, Unrecognised}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.SoleTraderOrLandlord
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareProduct, UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class CheckYourAnswersControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  private val recognisedProduct = SoftwareProduct(3, "Vendor 03", Recognised)
  private val unrecognisedProduct = SoftwareProduct(0, "", Unrecognised)

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
    "user answers is empty" should {
      "redirect to the service index" in {
        await(userFiltersRepository.set(testUserFilters(None)))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers

        res.status shouldBe INTERNAL_SERVER_ERROR
        res.body.contains("Sorry, we could not process your request") shouldBe true
      }
    }
    "there is pre-filled data" should {
      "display the page with appropriate summary lists with answers organised as lists" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Find).get
          .set(UserTypePage, SoleTraderOrLandlord).get
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
          pageTitle(s"${messages("check-your-answers.guided-heading")} - ${PageContentBase.title} - GOV.UK"),
          summaryListRow(SummaryListKeys.userType, Set(SoleTraderOrLandlord)
            .map(vf => messages(s"check-your-answers.user-type.${vf.key}")).mkString(" ")),
          summaryListRow(SummaryListKeys.incomeSources, Seq(SoleTrader, UkProperty, OverseasProperty)
            .map(vf => messages(s"business-income.$vf")).mkString(" ")),
          summaryListRow(SummaryListKeys.otherIncome, Seq(UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome,
            PrivatePensionIncome, ForeignDividends, ForeignInterest)
            .map(vf => messages(s"additional.income.source-$vf")).mkString(" ")),
          summaryListRow(SummaryListKeys.otherItems, Seq(PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans,
            MarriageAllowance, VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge)
            .map(vf => messages(s"other-items.$vf")).mkString(" ")),
          summaryListRow(SummaryListKeys.accountingPeriod, Set(OtherAccountingPeriod)
            .map(vf => messages(s"accounting-period.${vf.key}")).mkString(" "))
        )

        Jsoup.parse(res.body).select("main li").size() shouldBe 18
      }
      "display the page with minimal summary lists" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, recognisedProduct).get
          .set(UserTypePage, SoleTraderOrLandlord).get
          .set(BusinessIncomePage, Seq(SoleTrader)).get
          .set(AdditionalIncomeSourcesPage, Seq(UkInterest)).get
          .set(OtherItemsPage, Seq(PaymentsIntoAPrivatePension)).get
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("check-your-answers.checked-heading")} - ${PageContentBase.title} - GOV.UK"),
          summaryListRow(SummaryListKeys.softwareName, Set(recognisedProduct)
            .map(vf => vf.name).mkString(" ")),
          summaryListRow(SummaryListKeys.incomeSources, Seq(SoleTrader)
            .map(vf => messages(s"business-income.$vf")).mkString(" ")),
          summaryListRow(SummaryListKeys.otherIncome, Seq(UkInterest)
            .map(vf => messages(s"additional.income.source-$vf")).mkString(" ")),
          summaryListRow(SummaryListKeys.otherItems, Seq(PaymentsIntoAPrivatePension)
            .map(vf => messages(s"other-items.$vf")).mkString(" ")),
          summaryListRow(SummaryListKeys.accountingPeriod, Set(OtherAccountingPeriod)
            .map(vf => messages(s"accounting-period.${vf.key}")).mkString(" "))
        )

        Jsoup.parse(res.body).select("main li").size() shouldBe 0
      }
      "'None of these' is selected for other income and other items sources" in {
        val userAnswers = UserAnswers()
          .set(EnterSoftwareNamePage, recognisedProduct).get
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("check-your-answers.guided-heading")} - ${PageContentBase.title} - GOV.UK"),
          summaryListRow(SummaryListKeys.incomeSources, Seq(SoleTrader, UkProperty, OverseasProperty)
            .map(vf => messages(s"business-income.$vf")).mkString(" ")),
          summaryListRow(SummaryListKeys.otherIncome, messages("check-your-answers.none-selected")),
          summaryListRow(SummaryListKeys.otherItems, messages("check-your-answers.none-selected")),
          summaryListRow(SummaryListKeys.accountingPeriod, Set(OtherAccountingPeriod)
            .map(vf => messages(s"accounting-period.${vf.key}")).mkString(" "))
        )
      }
      "'Software not listed' is displayed when software not recognised" in {
        val userAnswers = UserAnswers()
          .set(EnterSoftwareNamePage, unrecognisedProduct).get
          .set(HowYouFindSoftwarePage, Check).get
          .set(UserTypePage, SoleTraderOrLandlord).get
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("check-your-answers.guided-heading")} - ${PageContentBase.title} - GOV.UK"),
          summaryListRow(SummaryListKeys.softwareName, messages("check-your-answers.software-not-listed")),
          summaryListRow(SummaryListKeys.incomeSources, Seq(SoleTrader, UkProperty, OverseasProperty)
            .map(vf => messages(s"business-income.$vf")).mkString(" ")),
          summaryListRow(SummaryListKeys.otherIncome, messages("check-your-answers.none-selected")),
          summaryListRow(SummaryListKeys.otherItems, messages("check-your-answers.none-selected")),
          summaryListRow(SummaryListKeys.accountingPeriod, Set(OtherAccountingPeriod)
            .map(vf => messages(s"accounting-period.${vf.key}")).mkString(" "))
        )
      }
      "In the Check journey, Software Product is not set" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq(UkInterest)).get
          .set(OtherItemsPage, Seq(StudentLoans)).get
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers
        res.status shouldBe INTERNAL_SERVER_ERROR
        res.body.contains("Sorry, we could not process your request") shouldBe true
      }
      "In the Check journey, user type is not set" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, recognisedProduct).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers
        res.status shouldBe INTERNAL_SERVER_ERROR
        res.body.contains("Sorry, we could not process your request") shouldBe true
      }
      "Business Incomes sources is not set" in {
        val userAnswers = UserAnswers()
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers
        res.status shouldBe INTERNAL_SERVER_ERROR
        res.body.contains("Sorry, we could not process your request") shouldBe true
      }
      "Other Incomes sources is not set" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers
        res.status shouldBe INTERNAL_SERVER_ERROR
        res.body.contains("Sorry, we could not process your request") shouldBe true
      }
      "Other Items sources is not set" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(AccountingPeriodPage, OtherAccountingPeriod).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers
        res.status shouldBe INTERNAL_SERVER_ERROR
        res.body.contains("Sorry, we could not process your request") shouldBe true
      }
      "Accounting Period is not set" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.getCheckYourAnswers
        res.status shouldBe INTERNAL_SERVER_ERROR
        res.body.contains("Sorry, we could not process your request") shouldBe true
      }
    }
  }

  "POST /check-your-answers" must {
    s"return $SEE_OTHER" when {
      "redirect to the zero results page when there are no all-in-one vendors" in {
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
      "redirect to the software results page when there are all-in-one vendors and no journey has been set" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, SixthAprilToFifthApril).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.postCheckYourAnswers()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SearchSoftwareController.show().url)
        )

        await(userFiltersRepository.get(SessionId)) match {
          case Some(uf) => uf.finalFilters shouldBe Seq(SoleTrader, UkProperty, OverseasProperty, StandardUpdatePeriods)
          case None => fail("No user filters found")
        }
      }
      "redirect to the software results page when there are all-in-one vendors in the find journey" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, SixthAprilToFifthApril).get
          .set(HowYouFindSoftwarePage, Find).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.postCheckYourAnswers()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SearchSoftwareController.show().url)
        )

        await(userFiltersRepository.get(SessionId)) match {
          case Some(uf) => uf.finalFilters shouldBe Seq(SoleTrader, UkProperty, OverseasProperty, StandardUpdatePeriods)
          case None => fail("No user filters found")
        }
      }
      "redirect to the fully compatible page when in the check journey when software type is recognised and fully compatible" in {
        val fullyCompatibleProduct = SoftwareProduct(105, "vendor 05", Recognised)

        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, SixthAprilToFifthApril).get
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, fullyCompatibleProduct).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.postCheckYourAnswers()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.FullyCompatibleController.show().url)
        )

        await(userFiltersRepository.get(SessionId)) match {
          case Some(uf) => uf.finalFilters shouldBe Seq(SoleTrader, UkProperty, OverseasProperty, StandardUpdatePeriods)
          case None => fail("No user filters found")
        }
      }
      "redirect to the check your answers page when in the check journey when software type is recognised and partially compatible" in {
        val partiallyCompatibleProduct = SoftwareProduct(104, "vendor 04", Recognised)

        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, SixthAprilToFifthApril).get
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, partiallyCompatibleProduct).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.postCheckYourAnswers()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.PartiallyCompatibleController.show().url)
        )

        await(userFiltersRepository.get(SessionId)) match {
          case Some(uf) => uf.finalFilters shouldBe Seq(SoleTrader, UkProperty, OverseasProperty, StandardUpdatePeriods)
          case None => fail("No user filters found")
        }
      }
      "redirect to the check your answers page when in the check journey when software type is recognised and quarterly only" in {
        val quarterlyOnlyProduct = SoftwareProduct(102, "vendor 02", Recognised)

        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, SixthAprilToFifthApril).get
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, quarterlyOnlyProduct).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.postCheckYourAnswers()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.QuarterlyOnlyController.show().url)
        )

        await(userFiltersRepository.get(SessionId)) match {
          case Some(uf) => uf.finalFilters shouldBe Seq(SoleTrader, UkProperty, OverseasProperty, StandardUpdatePeriods)
          case None => fail("No user filters found")
        }
      }
      "redirect to the check your answers page when in the check journey when software type is recognised and not compatible" in {
        val nonCompatibleProduct = SoftwareProduct(101, "vendor 01", Recognised)

        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, SixthAprilToFifthApril).get
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, nonCompatibleProduct).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.postCheckYourAnswers()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.NotCompatibleController.show().url)
        )

        await(userFiltersRepository.get(SessionId)) match {
          case Some(uf) => uf.finalFilters shouldBe Seq(SoleTrader, UkProperty, OverseasProperty, StandardUpdatePeriods)
          case None => fail("No user filters found")
        }
      }

      "redirect to the software results page when there are all-in-one vendors in the check journey when software type is not set to recognised" in {
        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, SixthAprilToFifthApril).get
          .set(HowYouFindSoftwarePage, Check).get
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        val res = SoftwareChoicesFrontend.postCheckYourAnswers()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SearchSoftwareController.show().url)
        )

        await(userFiltersRepository.get(SessionId)) match {
          case Some(uf) => uf.finalFilters shouldBe Seq(SoleTrader, UkProperty, OverseasProperty, StandardUpdatePeriods)
          case None => fail("No user filters found")
        }
      }
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(userFiltersRepository.collection.drop().toFuture())
  }

  lazy val controller: CheckYourAnswersController = app.injector.instanceOf[CheckYourAnswersController]

  def testUserFilters(answers: Option[UserAnswers]): UserFilters = UserFilters(
    id = SessionId,
    answers = answers,
    randomVendorOrder = (for (x <- 100 to 200) yield x).toList // range of productId in local test data
  )

  object SummaryListKeys {
    val userType = "User type"
    val softwareName = "Software name"
    val incomeSources = "Income sources (quarterly updates and tax return)"
    val otherIncome = "Other incomes (tax return only)"
    val otherItems = "Other items (tax return only)"
    val accountingPeriod = "Accounting period"
  }
}

