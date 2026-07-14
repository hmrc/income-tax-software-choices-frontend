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
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod.SixthAprilToFifthApril
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.Recognised
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.{Agent, SoleTraderOrLandlord}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{VendorFilter, *}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.*

import java.time.Instant
import org.jsoup.Jsoup

class SearchSoftwareControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {
  private val testTime = Instant.now()

  def testUserFilters(answers: Option[UserAnswers], filters: Seq[VendorFilter]): UserFilters =
    UserFilters(
      SessionId,
      answers,
      finalFilters = filters,
      lastUpdated = testTime
    )

  override def beforeEach(): Unit = {
    await(userFiltersRepository.collection.drop().toFuture())
    super.beforeEach()
  }

  s"GET ${routes.SearchSoftwareController.show().url}" should {
    "redirect to the service index" when {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.getSoftwareResults

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "display the page" when {
      "there is data present in the database for this Individual user and Check feature not enabled" in {
        setupAnswers(SessionId, None)

        val response = SoftwareChoicesFrontend.getSoftwareResults

        response should have(
          httpStatus(OK),
          elementExists("#agent-filter", false)
        )
      }
      "there is data present in the database for this Agent user in the Find journey" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Find).get
          .set(UserTypePage, Agent).get
        setupAnswers(SessionId, Some(userAnswers), Seq(VendorFilter.Agent))

        val response = SoftwareChoicesFrontend.getSoftwareResults

        response should have(
          httpStatus(OK),
          elementExists("#agent-filter", false)
        )
      }
      "user type is individual in the database for this user in the Unguided Journey" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, ViewAll).get
          .set(UserTypePage, SoleTraderOrLandlord).get
        setupAnswers(SessionId, Some(userAnswers), Seq(VendorFilter.Individual))

        val response = SoftwareChoicesFrontend.getSoftwareResults

        response should have(
          httpStatus(OK),
          elementExists("#agent-filter", true)
        )
      }
    }
    "have a back link that returns to the user type page" when {
      "the journey type is View All" in {
        val userAnswers = UserAnswers()
          .set(UserTypePage, Agent).get
          .set(HowYouFindSoftwarePage, ViewAll).get
        setupAnswers(SessionId, Some(userAnswers), Seq(VendorFilter.Agent))

        val res = SoftwareChoicesFrontend.getSoftwareResults

        res should have(
          httpStatus(OK),
          elementExists(s""".govuk-back-link[href="${routes.UserTypeController.show().url}"]""", true)
        )
      }
    }
    "have a back link that returns to the check your answers page" when {
      "the journey type is Find" in {
        val userAnswers = UserAnswers()
          .set(UserTypePage, Agent).get
          .set(HowYouFindSoftwarePage, Find).get
        setupAnswers(SessionId, Some(userAnswers), Seq(VendorFilter.Agent))

        val res = SoftwareChoicesFrontend.getSoftwareResults

        res should have(
          httpStatus(OK),
          elementExists(s""".govuk-back-link[href="${routes.CheckYourAnswersController.show().url}"]""", true)
        )
      }
    }
    "have a back link that returns to the not compatible page" when {
      "the journey type is Check and has a non compatible product" in {
        val softwareProduct = SoftwareProduct(101, "Product 101", Recognised)
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, softwareProduct).get
          .set(UserTypePage, Agent).get
          .set(BusinessIncomePage, Seq(SoleTrader)).get
          .set(OtherItemsPage, Seq(UkInterest)).get
          .set(AccountingPeriodPage, SixthAprilToFifthApril).get

        val initialFilter = Seq(VendorFilter.Agent, SoleTrader, UkInterest, StandardUpdatePeriods)
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers), initialFilter)))

        val res = SoftwareChoicesFrontend.getSoftwareResults

        res should have(
          httpStatus(OK),
          elementExists(s""".govuk-back-link[href="${routes.NotCompatibleController.show().url}"]""", true)
        )
      }
    }
    "have a back link that returns to the quarterly updates only page" when {
      "the journey type is Check and has a quarterly quarterly-updates-only product" in {
        val softwareProduct = SoftwareProduct(102, "Product 102", Recognised)

        val userAnswers = UserAnswers()
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
          .set(AdditionalIncomeSourcesPage, Seq.empty).get
          .set(OtherItemsPage, Seq.empty).get
          .set(AccountingPeriodPage, SixthAprilToFifthApril).get
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, softwareProduct).get
          .set(UserTypePage, Agent).get

        val initialFilter = Seq(VendorFilter.Agent, SoleTrader, UkProperty, OverseasProperty, StandardUpdatePeriods)
        await(userFiltersRepository.set(testUserFilters(Some(userAnswers), initialFilter)))

        val res = SoftwareChoicesFrontend.getSoftwareResults

        res should have(
          httpStatus(OK),
          elementExists(s""".govuk-back-link[href="${routes.QuarterlyOnlyController.show().url}"]""", true)
        )
      }
    }
  }

  s"GET ${routes.SearchSoftwareController.clear().url}" should {
    "redirect to the service index" when {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.clear()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }

    "remove all preference filters leaving user answers filters and UserType for Individual" in {
      val userAnswers = UserAnswers()
        .set(UserTypePage, SoleTraderOrLandlord).get
        .set(BusinessIncomePage, Seq(SoleTrader)).get
        .set(AdditionalIncomeSourcesPage, Seq(UkInterest)).get
        .set(OtherItemsPage, Seq(PaymentsIntoAPrivatePension)).get
        .set(AccountingPeriodPage, SixthAprilToFifthApril).get

      val initialFilter = Seq(Individual, SoleTrader, UkInterest, PaymentsIntoAPrivatePension, StandardUpdatePeriods, FreeVersion)

      await(userFiltersRepository.set(testUserFilters(Some(userAnswers), initialFilter)))

      val response = SoftwareChoicesFrontend.clear()
      response should have(
        httpStatus(SEE_OTHER)
      )

      await(userFiltersRepository.get(SessionId)) match {
        case Some(uf) => uf.finalFilters shouldBe Seq(
          Individual, SoleTrader, UkInterest, PaymentsIntoAPrivatePension, StandardUpdatePeriods
        )
        case None => fail("No user filters found")
      }
    }

    "remove all preference filters leaving user answers filters and UserType for Agent in Guided journey" in {
      val userAnswers = UserAnswers()
        .set(HowYouFindSoftwarePage, Find).get
        .set(UserTypePage, SoleTraderOrLandlord).get
        .set(BusinessIncomePage, Seq(SoleTrader)).get
        .set(AdditionalIncomeSourcesPage, Seq(UkInterest)).get
        .set(OtherItemsPage, Seq(PaymentsIntoAPrivatePension)).get
        .set(AccountingPeriodPage, SixthAprilToFifthApril).get

      val initialFilter = Seq(Individual, SoleTrader, UkInterest, PaymentsIntoAPrivatePension, StandardUpdatePeriods, FreeVersion)

      await(userFiltersRepository.set(testUserFilters(Some(userAnswers), initialFilter)))

      val response = SoftwareChoicesFrontend.clear()
      response should have(
        httpStatus(SEE_OTHER)
      )

      await(userFiltersRepository.get(SessionId)) match {
        case Some(uf) => uf.finalFilters shouldBe Seq(
          Individual, SoleTrader, UkInterest, PaymentsIntoAPrivatePension, StandardUpdatePeriods
        )
        case None => fail("No user filters found")
      }
    }

    "remove all preference filters including UserType for Agent in Unguided journey" in {
      val userAnswers = UserAnswers()
        .set(HowYouFindSoftwarePage, ViewAll).get
        .set(UserTypePage, Agent).get

      val initialFilter = Seq(VendorFilter.Agent, FreeVersion)

      await(userFiltersRepository.set(testUserFilters(Some(userAnswers), initialFilter)))

      val response = SoftwareChoicesFrontend.clear()
      response should have(
        httpStatus(SEE_OTHER)
      )

      await(userFiltersRepository.get(SessionId)) match {
        case Some(uf) => uf.finalFilters shouldBe Seq()
        case None => fail("No user filters found")
      }
    }

    "remove all preference filters including UserType for Individual in Unguided journey" in {
      val userAnswers = UserAnswers()
        .set(HowYouFindSoftwarePage, ViewAll).get
        .set(UserTypePage, SoleTraderOrLandlord).get

      val initialFilter = Seq(VendorFilter.Individual, FreeVersion)

      await(userFiltersRepository.set(testUserFilters(Some(userAnswers), initialFilter)))

      val response = SoftwareChoicesFrontend.clear()
      response should have(
        httpStatus(SEE_OTHER)
      )

      await(userFiltersRepository.get(SessionId)) match {
        case Some(uf) => uf.finalFilters shouldBe Seq()
        case None => fail("No user filters found")
      }
    }
  }

  s"POST ${routes.SearchSoftwareController.search().url}" should {
    "redirect to the service index" when {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.submitSoftwareSearch(FiltersFormModel(Seq(FreeVersion)))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "return the same software result count when Individual is unchecked or Agent is unchecked in the Unguided (ViewAll) journey" in {
      val soleTraderAnswers = UserAnswers()
        .set(HowYouFindSoftwarePage, ViewAll).get
        .set(UserTypePage, SoleTraderOrLandlord).get
      setupAnswers(SessionId, Some(soleTraderAnswers), Seq(VendorFilter.Individual))

      val soleTraderResponse = SoftwareChoicesFrontend.submitSoftwareSearch(FiltersFormModel(Seq.empty))
      soleTraderResponse should have(httpStatus(OK))
      val soleTraderCount = Jsoup.parse(soleTraderResponse.body).select("h1.govuk-heading-xl").text()

      val agentAnswers = UserAnswers()
        .set(HowYouFindSoftwarePage, ViewAll).get
        .set(UserTypePage, Agent).get
      setupAnswers(SessionId, Some(agentAnswers), Seq(VendorFilter.Agent))

      val agentResponse = SoftwareChoicesFrontend.submitSoftwareSearch(FiltersFormModel(Seq.empty))
      agentResponse should have(httpStatus(OK))
      val agentCount = Jsoup.parse(agentResponse.body).select("h1.govuk-heading-xl").text()

      soleTraderCount shouldBe agentCount
    }

    "add preference filters for Individual including UserAnswers and UserType" in {
      val userAnswers = UserAnswers()
        .set(UserTypePage, SoleTraderOrLandlord).get
        .set(BusinessIncomePage, Seq(SoleTrader)).get
        .set(AdditionalIncomeSourcesPage, Seq(UkInterest)).get
        .set(OtherItemsPage, Seq(PaymentsIntoAPrivatePension)).get
        .set(AccountingPeriodPage, SixthAprilToFifthApril).get

      val initialFilter = Seq()
      await(userFiltersRepository.set(testUserFilters(Some(userAnswers), initialFilter)))

      val response = SoftwareChoicesFrontend.submitSoftwareSearch(FiltersFormModel(Seq(FreeVersion)))

      response should have(
        httpStatus(OK),
        elementExists("#agent-filter", false)
      )

      await(userFiltersRepository.get(SessionId)) match {
        case Some(uf) => uf.finalFilters shouldBe Seq(
          Individual, SoleTrader, UkInterest, PaymentsIntoAPrivatePension, StandardUpdatePeriods, FreeVersion
        )
        case None => fail("No user filters found")
      }
    }

    "add preference filters for Agent including UserAnswers and UserType in Guided journey" in {
      val userAnswers = UserAnswers()
        .set(HowYouFindSoftwarePage, Find).get
        .set(UserTypePage, Agent).get
        .set(BusinessIncomePage, Seq(SoleTrader)).get
        .set(AdditionalIncomeSourcesPage, Seq(UkInterest)).get
        .set(OtherItemsPage, Seq(PaymentsIntoAPrivatePension)).get
        .set(AccountingPeriodPage, SixthAprilToFifthApril).get

      val initialFilter = Seq()
      await(userFiltersRepository.set(testUserFilters(Some(userAnswers), initialFilter)))

      val response = SoftwareChoicesFrontend.submitSoftwareSearch(FiltersFormModel(Seq(FreeVersion)))

      response should have(
        httpStatus(OK),
        elementExists("#agent-filter", false)
      )

      await(userFiltersRepository.get(SessionId)) match {
        case Some(uf) => uf.finalFilters shouldBe Seq(
          VendorFilter.Agent, SoleTrader, UkInterest, PaymentsIntoAPrivatePension, StandardUpdatePeriods, FreeVersion
        )
        case None => fail("No user filters found")
      }
    }

    "add preference filters for Individual but not User Type for Unguided journey" in {
      val userAnswers = UserAnswers()
        .set(HowYouFindSoftwarePage, ViewAll).get
        .set(UserTypePage, SoleTraderOrLandlord).get

      await(userFiltersRepository.set(testUserFilters(Some(userAnswers), Seq(VendorFilter.Individual))))

      val response = SoftwareChoicesFrontend.submitSoftwareSearch(FiltersFormModel(Seq(FreeVersion, Bridging)))

      response should have(
        httpStatus(OK),
        elementExists("#agent-filter", true),
        checkboxSelected("agent-filter", None)
      )

      await(userFiltersRepository.get(SessionId)) match {
        case Some(uf) => uf.finalFilters shouldBe Seq(FreeVersion, Bridging)
        case None => fail("No user filters found")
      }
    }

    "add preference filters for Agent including User Type" in {
      val userAnswers = UserAnswers()
        .set(UserTypePage, Agent).get

      val initialFilter = Seq()
      setupAnswers(SessionId, Some(userAnswers), initialFilter)

      val response = SoftwareChoicesFrontend.submitSoftwareSearch(FiltersFormModel(Seq(VendorFilter.Agent, FreeVersion)))

      response should have(
        httpStatus(OK),
        elementExists("#agent-filter", true),
        checkboxSelected("agent-filter", Some("agent"))
      )

      await(userFiltersRepository.get(SessionId)) match {
        case Some(uf) => uf.finalFilters shouldBe Seq(VendorFilter.Agent, FreeVersion)
        case None => fail("No user filters found")
      }
    }

    "add preference filters for Individual including User Type for Unguided journey" in {
      val userAnswers = UserAnswers()
        .set(HowYouFindSoftwarePage, ViewAll).get
        .set(UserTypePage, Agent).get

      val initialFilter = Seq()
      setupAnswers(SessionId, Some(userAnswers), initialFilter)

      val response = SoftwareChoicesFrontend.submitSoftwareSearch(FiltersFormModel(Seq(VendorFilter.Agent, FreeVersion)))

      response should have(
        httpStatus(OK),
        elementExists("#agent-filter", true),
        checkboxSelected("agent-filter", Some("agent"))
      )

      await(userFiltersRepository.get(SessionId)) match {
        case Some(uf) => uf.finalFilters shouldBe Seq(VendorFilter.Agent, FreeVersion)
        case None => fail("No user filters found")
      }
    }
  }
}
