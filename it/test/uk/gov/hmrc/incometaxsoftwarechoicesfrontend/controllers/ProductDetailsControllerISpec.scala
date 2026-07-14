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
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareProduct, UserAnswers, UserFilters, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.Recognised
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.SoleTraderOrLandlord
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.SoleTrader

import java.time.Instant

class ProductDetailsControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {
  private val testTime = Instant.now()
  private val address = "/product-details"

  def testUserFilters(answers: Option[UserAnswers], filters: Seq[VendorFilter] = Seq.empty): UserFilters =
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


  s"GET /find-making-tax-digital-income-tax-software${address}" when {

    "there are not UserAnswers" should {
      "respond with 200 status for a real software name" in {
        When(s"GET $address is called")
        val response = SoftwareChoicesFrontend.productDetails("101")

        Then("Should return OK with the software search page")
        response should have(
          httpStatus(OK)
        )
      }
      "respond with 404 status for a non existent product id" in {
        When(s"GET $address is called")
        val response = SoftwareChoicesFrontend.productDetails("0")

        Then("Should return Not Found")
        response should have(
          httpStatus(NOT_FOUND)
        )
      }
    }
    
    "the user has come through Find Journey" should {
      "respond with 200 status for a real software name" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Find).get
          .set(UserTypePage, SoleTraderOrLandlord).get

        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        When(s"GET $address is called")
        val response = SoftwareChoicesFrontend.productDetails("101")

        Then("Should return OK with the software search page as the back link")
        response should have(
          httpStatus(OK),
          elementExists(s""".govuk-back-link[href="${routes.SearchSoftwareController.show().url}"]""", true)
        )
      }
      
      "respond with 404 status for a non existent product id" in {
        When(s"GET $address is called")
        val response = SoftwareChoicesFrontend.productDetails("0")

        Then("Should return Not Found")
        response should have(
          httpStatus(NOT_FOUND)
        )
      }
    }

    "the user has come through ViewAll Journey" should {
      "respond with 200 status for a real software name" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, ViewAll).get
          .set(UserTypePage, SoleTraderOrLandlord).get

        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        When(s"GET $address is called")
        val response = SoftwareChoicesFrontend.productDetails("101")

        Then("Should return OK with the software search page as the back link")
        response should have(
          httpStatus(OK),
          elementExists(s""".govuk-back-link[href="${routes.SearchSoftwareController.show().url}"]""", true)
        )
      }
    }

    "the user has come through Check Journey" should {
      "respond with 200 status for Fully Compatible Software" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, SoftwareProduct(101,"", Recognised)).get
          .set(UserTypePage, SoleTraderOrLandlord).get
          .set(BusinessIncomePage, Seq(SoleTrader)).get

        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        When(s"GET $address is called")
        val response = SoftwareChoicesFrontend.productDetails("101")

        Then("Should return OK with the fully compatible page as the back link")
        response should have(
          httpStatus(OK),
          elementExists(s""".govuk-back-link[href="${routes.FullyCompatibleController.show().url}"]""", true)
        )
      }

      "respond with 200 status for Partially Compatible Software" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, SoftwareProduct(104,"", Recognised)).get
          .set(UserTypePage, SoleTraderOrLandlord).get
          .set(BusinessIncomePage, Seq(SoleTrader)).get

        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        When(s"GET $address is called")
        val response = SoftwareChoicesFrontend.productDetails("104")

        Then("Should return OK with the partially compatible page as the back link")
        response should have(
          httpStatus(OK),
          elementExists(s""".govuk-back-link[href="${routes.PartiallyCompatibleController.show().url}"]""", true)
        )
      }

      "respond with 200 status for Quarterly Compatible Only Software" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, SoftwareProduct(102,"", Recognised)).get
          .set(UserTypePage, SoleTraderOrLandlord).get
          .set(BusinessIncomePage, Seq(SoleTrader)).get

        await(userFiltersRepository.set(testUserFilters(Some(userAnswers))))

        When(s"GET $address is called")
        val response = SoftwareChoicesFrontend.productDetails("102")

        Then("Should return OK with the quarterly only page as the back link")
        response should have(
          httpStatus(OK),
          elementExists(s""".govuk-back-link[href="${routes.QuarterlyOnlyController.show().url}"]""", true)
        )
      }

    }


  }

}
