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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.{SoleTraderOrLandlord, Agent}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.UserTypesPage

class UserTypeControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {
  def testUserFilters(answers: UserAnswers): UserFilters = UserFilters(SessionId, Some(answers))

  override def beforeEach(): Unit = {
    userFiltersRepository.collection.drop().toFuture()
    super.beforeEach()
  }

  "GET /type-of-user" when {
    "there are no existing page answers" should {
      "display the page with an empty form" in {
        val res = SoftwareChoicesFrontend.getUserType

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("type-of-user.heading")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"),
          radioButtonSelected(id = "type-of-user", None),
          radioButtonSelected(id = "type-of-user-2", None)
        )
      }
    }

    "there are pre-existing page answers with radio option selected" should {
      "display the page with the previously chosen radio checked" in {
        val userAnswers = UserAnswers().set(UserTypesPage, SoleTraderOrLandlord).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getUserType

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("type-of-user.heading")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"),
          radioButtonSelected(id = "type-of-user", selectedRadioButton = Some(SoleTraderOrLandlord.key))
        )
      }
    }
  }

  "POST /type-of-user" must {
    s"return $SEE_OTHER and save page answer" when {
      "user selects sole trader and landlord" in {
        val res = SoftwareChoicesFrontend.submitUserType(Some(SoleTraderOrLandlord))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.BusinessIncomeController.show().url)
        )

        getPageData(SessionId, UserTypesPage.toString).size shouldBe 1
      }

      "user selects agent working on behalf of client" in {
        val res = SoftwareChoicesFrontend.submitUserType(Some(Agent))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SearchSoftwareController.show.url)
        )
        getPageData(SessionId, UserTypesPage.toString).size shouldBe 1
      }
    }

    "return BAD_REQUEST" when {
      "no answer is given" in {
        val res = SoftwareChoicesFrontend.submitUserType(None)

        res should have(
          httpStatus(BAD_REQUEST)
        )

        getPageData(SessionId, UserTypesPage.toString).size shouldBe 0
      }
    }
  }
}
