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
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{OverseasProperty, SoleTrader, UkProperty}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.BusinessIncomePage

class BusinessIncomeControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  lazy val controller = app.injector.instanceOf[BusinessIncomeController]

  def testUserFilters(answers: UserAnswers): UserFilters = UserFilters(SessionId, Some(answers))

  override def beforeEach(): Unit = {
    userFiltersRepository.collection.drop().toFuture()
    super.beforeEach()
  }

  "GET /business-income" when {
    "there are no existing page answers" should {
      "display the page with an empty form" in {
        val res = SoftwareChoicesFrontend.getBusinessIncome

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("business-income.title")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"),
          checkboxSelected("businessIncome", None),
          checkboxSelected("businessIncome-2", None),
          checkboxSelected("businessIncome-3", None)
        )
      }
    }
    "there is pre-filled data" should {
      "display the page with pre-filled checkboxes" in {
        val userAnswers = UserAnswers().set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getBusinessIncome

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("business-income.title")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"),
          checkboxSelected("businessIncome", Some(SoleTrader.key)),
          checkboxSelected("businessIncome-2", Some(UkProperty.key)),
          checkboxSelected("businessIncome-3", Some(OverseasProperty.key)),
        )
      }
    }
  }

  "POST /business-income" must {
    s"return $SEE_OTHER and save the page answers" when {
      "not in edit mode" when {
        "user submits one business income" in {
          val res = SoftwareChoicesFrontend.postBusinessIncome(Seq(UkProperty))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.AdditionalIncomeSourcesController.show().url)
          )
          getPageData(SessionId, BusinessIncomePage.toString).size shouldBe 1
        }
        "user submits multiple business incomes" in {
          val res = SoftwareChoicesFrontend.postBusinessIncome(Seq(SoleTrader, UkProperty, OverseasProperty))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.AdditionalIncomeSourcesController.show().url)
          )
          getPageData(SessionId, BusinessIncomePage.toString).size shouldBe 1
        }
      }
      "in edit mode" when {
        "user submits one business income" in {
          val res = SoftwareChoicesFrontend.postBusinessIncome(Seq(UkProperty))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show.url)
          )
          getPageData(SessionId, BusinessIncomePage.toString).size shouldBe 1
        }
        "user submits multiple business incomes" in {
          val res = SoftwareChoicesFrontend.postBusinessIncome(Seq(SoleTrader, UkProperty, OverseasProperty))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show.url)
          )
          getPageData(SessionId, BusinessIncomePage.toString).size shouldBe 1
        }
      }
    }
    "return BAD_REQUEST" when {
      "user does not select an answer" in {
        val res = SoftwareChoicesFrontend.postBusinessIncome(Seq.empty)

        res should have(
          httpStatus(BAD_REQUEST),
        )
        getPageData(SessionId, BusinessIncomePage.toString).size shouldBe 0
      }
    }

  }

  "backUrl" must {
    "return to guidance page when not in edit mode" in {
      controller.backUrl(editMode = false) shouldBe appConfig.guidance
    }
    "return to check your answers when in edit mode" in {
      controller.backUrl(editMode = true) shouldBe routes.CheckYourAnswersController.show.url
    }
  }

}
