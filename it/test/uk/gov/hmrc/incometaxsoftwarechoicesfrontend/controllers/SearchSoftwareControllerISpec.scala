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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.{Agent, SoleTraderOrLandlord}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{VendorFilter, *}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.*

import java.time.Instant

class SearchSoftwareControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {
  private val testTime = Instant.now()

  def testUserFilters(answers: Option[UserAnswers], filters: Seq[VendorFilter]): UserFilters =
    UserFilters(
      SessionId,
      answers,
      finalFilters = filters,
      randomVendorOrder = (for (x <- 100 to 200) yield x).toList, // range of productId in local test data
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
      "there is data present in the database for this user" in {
        setupAnswers(SessionId, None)

        val response = SoftwareChoicesFrontend.getSoftwareResults

        response should have(
          httpStatus(OK),
          elementExists("#agent-filter", false)
        )
      }
      "user type is agent in the database for this user" in {
        val userAnswers = UserAnswers()
          .set(UserTypePage, Agent).get
        setupAnswers(SessionId, Some(userAnswers), Seq(VendorFilter.Agent))

        val response = SoftwareChoicesFrontend.getSoftwareResults

        response should have(
          httpStatus(OK),
          elementExists("#agent-filter", true)
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

      val initialFilter = Seq(Individual, QuarterlyUpdates, TaxReturn, SoleTrader, UkInterest, PaymentsIntoAPrivatePension, StandardUpdatePeriods, FreeVersion)

      await(userFiltersRepository.set(testUserFilters(Some(userAnswers), initialFilter)))

      val response = SoftwareChoicesFrontend.clear()
      response should have(
        httpStatus(SEE_OTHER)
      )

      await(userFiltersRepository.get(SessionId)) match {
        case Some(uf) => uf.finalFilters shouldBe Seq(
          Individual, QuarterlyUpdates, TaxReturn, SoleTrader, UkInterest, PaymentsIntoAPrivatePension, StandardUpdatePeriods
        )
        case None => fail("No user filters found")
      }
    }

    "remove all preference filters including UserType for Agent" in {
      val userAnswers = UserAnswers()
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
          Individual, QuarterlyUpdates, TaxReturn, SoleTrader, UkInterest, PaymentsIntoAPrivatePension, StandardUpdatePeriods, FreeVersion
        )
        case None => fail("No user filters found")
      }
    }

    "add preference filters for Agent but not User Type" in {
      val userAnswers = UserAnswers()
        .set(UserTypePage, Agent).get

      await(userFiltersRepository.set(testUserFilters(Some(userAnswers), Seq(VendorFilter.Agent))))

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
      val randonVendorOrder = (for (x <- 100 to 200) yield x).toList
      setupAnswers(SessionId, Some(userAnswers), initialFilter, randonVendorOrder)

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
