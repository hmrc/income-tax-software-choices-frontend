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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserAnswers
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class AccountingPeriodNotAlignedControllerISpec 
  extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  s"GET ${routes.AccountingPeriodNotAlignedController.show().url}" should {
    "redirect to the service index" when {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.getAccountingPeriodNotAligned

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "display the page" in {
      setupAnswers(SessionId, Some(UserAnswers()))

      val res = SoftwareChoicesFrontend.getAccountingPeriodNotAligned

      res should have(
        httpStatus(OK),
        pageTitle(s"${messages("accounting-period-not-aligned.heading")} - ${PageContentBase.title} - GOV.UK"),
      )
    }
  }

  s"POST ${routes.AccountingPeriodNotAlignedController.submit().url}" should {
    "redirect to the service index" should {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.postAccountingPeriodNotAligned

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "redirect to software results page" in {
      setupAnswers(SessionId, Some(UserAnswers()))

      val res = SoftwareChoicesFrontend.postAccountingPeriodNotAligned

      res should have(
        httpStatus(SEE_OTHER),
        redirectURI(routes.CheckYourAnswersController.show().url)
      )
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(userFiltersRepository.collection.drop().toFuture())
  }
}
