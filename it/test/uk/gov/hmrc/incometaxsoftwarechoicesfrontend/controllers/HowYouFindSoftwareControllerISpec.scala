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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.{Check, Find}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.SoleTraderOrLandlord
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{JourneyType, UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class HowYouFindSoftwareControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  def testUserFilters(answers: UserAnswers): UserFilters = UserFilters(SessionId, Some(answers))

  override def beforeEach(): Unit = {
    await(userFiltersRepository.collection.drop().toFuture())
    super.beforeEach()
  }

  s"GET ${routes.HowYouFindSoftwareController.show()}" when {
    "there are no existing page answers" should {
      "display the page with an empty form" in {
        val res = SoftwareChoicesFrontend.getHowYouFindSoftware()

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("how-you-find-software.heading")} - ${PageContentBase.title} - GOV.UK"),
          radioButtonSelected(id = "how-you-find-software", None),
          radioButtonSelected(id = "how-you-find-software-2", None)
        )
      }
    }

    "there are pre-existing page answers with radio option selected" should {
      "display the page with Check radio checked" in {
        val userAnswers = UserAnswers().set(HowYouFindSoftwarePage, Check).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getHowYouFindSoftware()

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("how-you-find-software.heading")} - ${PageContentBase.title} - GOV.UK"),
          radioButtonSelected(id = "how-you-find-software-2", selectedRadioButton = Some(Check.key))
        )
      }
      "display the page with Find radio checked" in {
        val userAnswers = UserAnswers().set(HowYouFindSoftwarePage, Find).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getHowYouFindSoftware()

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("how-you-find-software.heading")} - ${PageContentBase.title} - GOV.UK"),
          radioButtonSelected(id = "how-you-find-software", selectedRadioButton = Some(Find.key))
        )
      }
    }
  }

  s"POST ${routes.HowYouFindSoftwareController.submit()}" when {
    "user selects Check journey" must {
      s"return $SEE_OTHER and save page answer" in {
        val res = SoftwareChoicesFrontend.postHowYouFindSoftware(Some(Check))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.EnterSoftwareNameController.show().url)
        )

        getPageData(SessionId, HowYouFindSoftwarePage.toString).size shouldBe 1
      }
    }
    "user selects Find journey" must {
      s"return $SEE_OTHER and save page answer" in {
        val res = SoftwareChoicesFrontend.postHowYouFindSoftware(Some(Find))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.UserTypeController.show().url)
        )

        getPageData(SessionId, HowYouFindSoftwarePage.toString).size shouldBe 1
      }
    }

    "user has preexisting UserAnswers" must {
      s"return $SEE_OTHER and update page answer" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Find).get
          .set(UserTypePage, SoleTraderOrLandlord).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.postHowYouFindSoftware(Some(Check))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.EnterSoftwareNameController.show().url)
        )
        getAllPageData(SessionId).size shouldBe 2
      }
    }

    "return BAD_REQUEST" when {
      "no answer is given" in {
        val res = SoftwareChoicesFrontend.postHowYouFindSoftware(None)

        res should have(
          httpStatus(BAD_REQUEST)
        )
        getPageData(SessionId, HowYouFindSoftwarePage.toString).size shouldBe 0
      }
    }
  }
}
