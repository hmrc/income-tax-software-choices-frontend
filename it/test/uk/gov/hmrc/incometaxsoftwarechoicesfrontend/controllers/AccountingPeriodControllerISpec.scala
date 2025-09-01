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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.AccountingPeriodPage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class AccountingPeriodControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  s"GET ${routes.AccountingPeriodController.show().url}" should {
    "redirect to the service index" when {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.getAccountingPeriod

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "display the page" when {
      "the accounting period question has not been answered previously" in {
        setupAnswers(SessionId, None)

        val res = SoftwareChoicesFrontend.getAccountingPeriod

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("accounting-period.heading")} - ${PageContentBase.title} - GOV.UK"),
          radioButtonSelected(id = "accounting-period", None),
          radioButtonSelected(id = "accounting-period-2", None),
          radioButtonSelected(id = "accounting-period-4", None)
        )
      }
      "the accounting period question has been answered previously" which {
        "was the 6th April to 5th April option" in {
          setPageData(SessionId, AccountingPeriodPage, SixthAprilToFifthApril)

          val res = SoftwareChoicesFrontend.getAccountingPeriod

          res should have(
            httpStatus(OK),
            pageTitle(s"${messages("accounting-period.heading")} - ${PageContentBase.title} - GOV.UK"),
            radioButtonSelected(id = "accounting-period", Some(SixthAprilToFifthApril.key)),
            radioButtonSelected(id = "accounting-period-2", None),
            radioButtonSelected(id = "accounting-period-4", None)
          )
        }
        "was the 1st April to 31st March option" in {
          setPageData(SessionId, AccountingPeriodPage, FirstAprilToThirtyFirstMarch)

          val res = SoftwareChoicesFrontend.getAccountingPeriod

          res should have(
            httpStatus(OK),
            pageTitle(s"${messages("accounting-period.heading")} - ${PageContentBase.title} - GOV.UK"),
            radioButtonSelected(id = "accounting-period", None),
            radioButtonSelected(id = "accounting-period-2", Some(FirstAprilToThirtyFirstMarch.key)),
            radioButtonSelected(id = "accounting-period-4", None)
          )
        }
        "was the neither option" in {
          setPageData(SessionId, AccountingPeriodPage, OtherAccountingPeriod)

          val res = SoftwareChoicesFrontend.getAccountingPeriod

          res should have(
            httpStatus(OK),
            pageTitle(s"${messages("accounting-period.heading")} - ${PageContentBase.title} - GOV.UK"),
            radioButtonSelected(id = "accounting-period", None),
            radioButtonSelected(id = "accounting-period-2", None),
            radioButtonSelected(id = "accounting-period-4", Some(OtherAccountingPeriod.key))
          )
        }
      }
    }
  }

  s"POST ${routes.AccountingPeriodController.submit().url}" when {
    "there is nothing saved in the database for this user" should {
      "redirect to the service index" in {
        val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(SixthAprilToFifthApril))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "not in edit mode" should {
      "redirect to the check your answers page" when {
        "the user has selected the 6th April to 5th April radio button" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(SixthAprilToFifthApril))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, AccountingPeriodPage) shouldBe Some(SixthAprilToFifthApril)
        }
        "the user has selected the 1st April to 31st March radio button" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(FirstAprilToThirtyFirstMarch))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, AccountingPeriodPage) shouldBe Some(FirstAprilToThirtyFirstMarch)
        }
      }
      "redirect to the unsupported accounting period page" when {
        "the user selected the neither radio button" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(OtherAccountingPeriod))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.UnsupportedAccountingPeriodController.show.url)
          )

          getPageData(SessionId, AccountingPeriodPage) shouldBe Some(OtherAccountingPeriod)
        }
      }
    }
    "in edit mode" should {
      "redirect to the check your answers page" when {
        "the user has selected the 6th April to 5th April radio button" in {
          setPageData(SessionId, AccountingPeriodPage, FirstAprilToThirtyFirstMarch)

          val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(SixthAprilToFifthApril))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, AccountingPeriodPage) shouldBe Some(SixthAprilToFifthApril)
        }
        "the user has selected the 1st April to 31st March radio button" in {
          setPageData(SessionId, AccountingPeriodPage, OtherAccountingPeriod)

          val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(FirstAprilToThirtyFirstMarch))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, AccountingPeriodPage) shouldBe Some(FirstAprilToThirtyFirstMarch)
        }
      }
      "redirect to the unsupported accounting period page" when {
        "the user selected the neither radio button" in {
          setPageData(SessionId, AccountingPeriodPage, SixthAprilToFifthApril)

          val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(OtherAccountingPeriod))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.UnsupportedAccountingPeriodController.show.url)
          )

          getPageData(SessionId, AccountingPeriodPage) shouldBe Some(OtherAccountingPeriod)
        }
      }
    }
    "no radio button has been selected" should {
      "return a bad request" in {
        setupAnswers(SessionId, None)

        val res = SoftwareChoicesFrontend.submitAccountingPeriod(None)

        res should have(
          httpStatus(BAD_REQUEST),
          pageTitle(s"Error: ${messages("accounting-period.heading")} - ${PageContentBase.title} - GOV.UK")
        )
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

  override def beforeEach(): Unit = {
    super.beforeEach()

    await(userFiltersRepository.collection.drop().toFuture())
  }

  lazy val accountingPeriodController: AccountingPeriodController = app.injector.instanceOf[AccountingPeriodController]

}
