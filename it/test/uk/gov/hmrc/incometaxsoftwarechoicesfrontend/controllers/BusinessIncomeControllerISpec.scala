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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.BusinessIncomePage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class BusinessIncomeControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  s"GET ${routes.BusinessIncomeController.show().url}" should {
    "redirect to the service index" when {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.getBusinessIncome

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "display the page" when {
      "the business income sources has not been answered previously" in {
        setupAnswers(SessionId, None)

        val res = SoftwareChoicesFrontend.getBusinessIncome

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("business-income.title")} - ${PageContentBase.title} - GOV.UK"),
          checkboxSelected("businessIncome", None),
          checkboxSelected("businessIncome-2", None),
          checkboxSelected("businessIncome-3", None)
        )
      }
      "the business income sources has been answered previously" in {
        setPageData(SessionId, BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty))

        val res = SoftwareChoicesFrontend.getBusinessIncome

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("business-income.title")} - ${PageContentBase.title} - GOV.UK"),
          checkboxSelected("businessIncome", Some(SoleTrader.key)),
          checkboxSelected("businessIncome-2", Some(UkProperty.key)),
          checkboxSelected("businessIncome-3", Some(OverseasProperty.key)),
        )
      }
    }
  }

  s"POST ${routes.BusinessIncomeController.submit().url}" when {
    "there is nothing saved in the database for this user" should {
      "redirect to the service index" in {
        val res = SoftwareChoicesFrontend.postBusinessIncome(pageAnswers = Seq(SoleTrader))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "not in edit mode" should {
      "save answers and redirect to the additional income page" when {
        "they submit a single business income source" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.postBusinessIncome(pageAnswers = Seq(UkProperty))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.AdditionalIncomeSourcesController.show().url)
          )

          getPageData(SessionId, BusinessIncomePage) shouldBe Some(Seq(UkProperty))
        }
        "they submit multiple business income sources" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.postBusinessIncome(pageAnswers = Seq(SoleTrader, UkProperty, OverseasProperty))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.AdditionalIncomeSourcesController.show().url)
          )

          getPageData(SessionId, BusinessIncomePage) shouldBe Some(Seq(SoleTrader, UkProperty, OverseasProperty))
        }
      }
    }
    "in edit mode" should {
      "save answers and redirect to the check your answers page" when {
        "they submit a single business income source" in {
          setPageData(SessionId, BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty))

          val res = SoftwareChoicesFrontend.postBusinessIncome(pageAnswers = Seq(UkProperty), editMode = true)

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, BusinessIncomePage) shouldBe Some(Seq(UkProperty))
        }
        "they submit multiple business income sources" in {
          setPageData(SessionId, BusinessIncomePage, Seq(UkProperty))

          val res = SoftwareChoicesFrontend.postBusinessIncome(pageAnswers = Seq(SoleTrader, UkProperty, OverseasProperty), editMode = true)

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, BusinessIncomePage) shouldBe Some(Seq(SoleTrader, UkProperty, OverseasProperty))
        }
      }
    }
    "the user has no checkboxes selected" should {
      "return a bad request" in {
        setupAnswers(SessionId, None)

        val res = SoftwareChoicesFrontend.postBusinessIncome(Seq.empty)

        res should have(
          httpStatus(BAD_REQUEST),
          pageTitle(s"Error: ${messages("business-income.title")} - ${PageContentBase.title} - GOV.UK"),
        )
      }
    }
  }

  "backUrl" must {
    "return to guidance page when not in edit mode" in {
      controller.backUrl(editMode = false) shouldBe routes.UserTypeController.show().url
    }
    "return to check your answers when in edit mode" in {
      controller.backUrl(editMode = true) shouldBe routes.CheckYourAnswersController.show().url
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()

    await(userFiltersRepository.collection.drop().toFuture())
  }

  lazy val controller: BusinessIncomeController = app.injector.instanceOf[BusinessIncomeController]

}
