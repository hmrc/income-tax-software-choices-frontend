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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod.{FirstAprilToThirtyFirstMarch, OtherAccountingPeriod, SixthAprilToFifthApril}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.AccountingPeriodPage

class AccountingPeriodControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  lazy val accountingPeriodController: AccountingPeriodController = app.injector.instanceOf[AccountingPeriodController]
  def testUserFilters(answers: UserAnswers): UserFilters = UserFilters(SessionId, Some(answers))

  override def beforeEach(): Unit = {
    userFiltersRepository.collection.drop().toFuture()
    super.beforeEach()
  }

  "GET /accounting-period" when {
    "there are no existing page answers" should {
      "display the page with an empty form" in {
        val res = SoftwareChoicesFrontend.getAccountingPeriod

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("accounting-period.heading")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"),
          radioButtonSelected(id = "accounting-period", None),
          radioButtonSelected(id = "accounting-period-2", None),
          radioButtonSelected(id = "accounting-period-4", None)
        )
      }
    }

    "there are pre-existing page answers with radio option selected" should {
      "display the page with the previously chosen radio checked" in {
        val userAnswers = UserAnswers().set(AccountingPeriodPage, SixthAprilToFifthApril).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getAccountingPeriod

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("accounting-period.heading")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"),
          radioButtonSelected(id = "accounting-period", selectedRadioButton = Some(SixthAprilToFifthApril.key))
        )
      }
    }
  }

  "POST /accounting-period" must {
    s"return $SEE_OTHER and save page answer" when {
      "user selects 6 April to 5 April option" in {
        val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(SixthAprilToFifthApril))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SearchSoftwareController.show.url)
        )

        getPageData(SessionId, AccountingPeriodPage.toString).size shouldBe 1
      }

      "user selects 1 April to 31 March option" in {
        val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(FirstAprilToThirtyFirstMarch))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SearchSoftwareController.show.url)
        )
        getPageData(SessionId, AccountingPeriodPage.toString).size shouldBe 1
      }

      "user selects Neither of these option" in {
        val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(OtherAccountingPeriod))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.UnsupportedAccountingPeriodController.show.url)
        )
        getPageData(SessionId, AccountingPeriodPage.toString).size shouldBe 1
      }
    }

    "return BAD_REQUEST" when {
      "no answer is given" in {
        val res = SoftwareChoicesFrontend.submitAccountingPeriod(None)

        res should have(
          httpStatus(BAD_REQUEST)
        )

        getPageData(SessionId, AccountingPeriodPage.toString).size shouldBe 0
      }
    }
  }

  "backUrl" must {
    "return to additional income page when not in edit mode" in {
      accountingPeriodController.backUrl(editMode = false) shouldBe routes.OtherItemsController.show().url
    }
    "return to check your answers when in edit mode" in {
      accountingPeriodController.backUrl(editMode = true) shouldBe routes.CheckYourAnswersController.show().url
    }
  }
}
