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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers

import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.{Agent, SoleTraderOrLandlord}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase
import org.mongodb.scala.SingleObservableFuture

class UserTypeControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  def testUserFilters(answers: UserAnswers): UserFilters = UserFilters(SessionId, Some(answers))

  override def beforeEach(): Unit = {
    await(userFiltersRepository.collection.drop().toFuture())
    super.beforeEach()
  }

  "GET /how-will-you-use-it" when {
    "there are no existing page answers" should {
      "display the page with an empty form" in {
        val res = SoftwareChoicesFrontend.getUserType

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("type-of-user.heading")} - ${PageContentBase.title} - GOV.UK"),
          radioButtonSelected(id = "type-of-user", None),
          radioButtonSelected(id = "type-of-user-2", None)
        )
      }
    }

    "there are pre-existing page answers with radio option selected" should {
      "display the page with the previously chosen radio checked" in {
        val userAnswers = UserAnswers().set(UserTypePage, SoleTraderOrLandlord).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getUserType

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("type-of-user.heading")} - ${PageContentBase.title} - GOV.UK"),
          radioButtonSelected(id = "type-of-user", selectedRadioButton = Some(SoleTraderOrLandlord.key))
        )
      }
    }
  }

  "POST /how-will-you-use-it" when {
    "user selects sole trader and landlord" must {
      s"return $SEE_OTHER and save page answer" in {
        val res = SoftwareChoicesFrontend.submitUserType(Some(SoleTraderOrLandlord))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.BusinessIncomeController.show().url)
        )

        getPageData(SessionId, UserTypePage.toString).size shouldBe 1
        getFinalFilters(SessionId) shouldBe Seq.empty
      }
    }
    "user selects agent working on behalf of client" must {
      s"return $SEE_OTHER, reset user filters and save page answer" in {
        await(userFiltersRepository.set(testUserFilters(UserAnswers()
          .set(UserTypePage, SoleTraderOrLandlord).get
          .set(BusinessIncomePage, Seq(SoleTrader, UkProperty)).get
        )))
        getAllPageData(SessionId).size shouldBe 2 //verify existing user answers

        val res = SoftwareChoicesFrontend.submitUserType(Some(Agent))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SearchSoftwareController.show().url)
        )
        getPageData(SessionId, UserTypePage.toString).size shouldBe 1
        getAllPageData(SessionId).size shouldBe 1
        getFinalFilters(SessionId) shouldBe Seq(VendorFilter.Agent)

      }
    }

    "return BAD_REQUEST" when {
      "no answer is given" in {
        val res = SoftwareChoicesFrontend.submitUserType(None)

        res should have(
          httpStatus(BAD_REQUEST)
        )

        getPageData(SessionId, UserTypePage.toString).size shouldBe 0
      }
    }
  }
}
