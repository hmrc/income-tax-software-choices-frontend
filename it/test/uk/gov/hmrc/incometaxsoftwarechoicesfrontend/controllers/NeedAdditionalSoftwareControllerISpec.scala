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
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.Check
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.Spreadsheet
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareProduct, UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.{EnterSoftwareNamePage, HowYouFindSoftwarePage}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class NeedAdditionalSoftwareControllerISpec
  extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {
  
  def testUserFilters(answers: UserAnswers): UserFilters = UserFilters(SessionId, Some(answers))
  
  private val firstOtherSpreadsheetProduct = SoftwareProduct(1001, "Microsoft Excel", Spreadsheet)


  s"GET ${routes.NeedAdditionalSoftwareController.show().url}" should {
    "redirect to the service index" when {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.getNeedAdditionalSoftware

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "display the page" in {
      setupAnswers(SessionId, Some(UserAnswers()))

      val res = SoftwareChoicesFrontend.getNeedAdditionalSoftware

      res should have(
        httpStatus(OK),
        pageTitle(s"${messages("need-additional-software.heading")} - ${PageContentBase.title} - GOV.UK"),
      )
    }
    "in edit mode" should {
      "have a back link to check your answers" in {
        val userAnswers = UserAnswers().set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, firstOtherSpreadsheetProduct).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getEnterSoftwareName(editMode = true)

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("enter-software-name.heading")} - ${PageContentBase.title} - GOV.UK"),
          elementHasHref(".govuk-back-link", routes.CheckYourAnswersController.show().url)
        )
      }
    }
  }
  
  override def beforeEach(): Unit = {
    super.beforeEach()
    await(userFiltersRepository.collection.drop().toFuture())
  }
}
