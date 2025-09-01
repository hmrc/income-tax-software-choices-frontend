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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.AdditionalIncomeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{ConstructionIndustryScheme, Employment, ForeignDividends, ForeignInterest, PrivatePensionIncome, StatePensionIncome, UkDividends, UkInterest}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.AdditionalIncomeSourcesPage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class AdditionalIncomeSourcesControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  s"GET ${routes.AdditionalIncomeSourcesController.show().url}" should {
    "redirect to the service index" when {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.getAdditionalIncome

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "display the page" when {
      "the additional income has not been answered previously" in {
        setupAnswers(SessionId, None)

        val res = SoftwareChoicesFrontend.getAdditionalIncome

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("additional.income.source-heading")} - ${PageContentBase.title} - GOV.UK"),
          checkboxSelected("additionalIncome", None),
          checkboxSelected("additionalIncome-2", None),
          checkboxSelected("additionalIncome-3", None),
          checkboxSelected("additionalIncome-4", None),
          checkboxSelected("additionalIncome-5", None),
          checkboxSelected("additionalIncome-6", None),
          checkboxSelected("additionalIncome-7", None),
          checkboxSelected("additionalIncome-8", None),
          checkboxSelected("additionalIncome-10", None)
        )
      }
      "the additional income has been answered previously with additional income selections" in {
        setPageData(SessionId, AdditionalIncomeSourcesPage, additionalIncomeFilters)

        val res = SoftwareChoicesFrontend.getAdditionalIncome

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("additional.income.source-heading")} - ${PageContentBase.title} - GOV.UK"),
          checkboxSelected("additionalIncome", Some(UkInterest.key)),
          checkboxSelected("additionalIncome-2", Some(ConstructionIndustryScheme.key)),
          checkboxSelected("additionalIncome-3", Some(Employment.key)),
          checkboxSelected("additionalIncome-4", Some(UkDividends.key)),
          checkboxSelected("additionalIncome-5", Some(StatePensionIncome.key)),
          checkboxSelected("additionalIncome-6", Some(PrivatePensionIncome.key)),
          checkboxSelected("additionalIncome-7", Some(ForeignDividends.key)),
          checkboxSelected("additionalIncome-8", Some(ForeignInterest.key)),
          checkboxSelected("additionalIncome-10", None)
        )
      }
      "the additional income has been answered previously with none selected" in {
        setPageData(SessionId, AdditionalIncomeSourcesPage, Seq.empty)

        val res = SoftwareChoicesFrontend.getAdditionalIncome

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("additional.income.source-heading")} - ${PageContentBase.title} - GOV.UK"),
          checkboxSelected("additionalIncome", None),
          checkboxSelected("additionalIncome-2", None),
          checkboxSelected("additionalIncome-3", None),
          checkboxSelected("additionalIncome-4", None),
          checkboxSelected("additionalIncome-5", None),
          checkboxSelected("additionalIncome-6", None),
          checkboxSelected("additionalIncome-7", None),
          checkboxSelected("additionalIncome-8", None),
          checkboxSelected("additionalIncome-10", Some("none"))
        )
      }
    }
  }

  s"POST ${routes.AdditionalIncomeSourcesController.submit().url}" when {
    "there is nothing saved in the database for this user" should {
      "redirect to the service index" in {
        val res = SoftwareChoicesFrontend.submitAdditionalIncome(Some(Seq(UkInterest)))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "not in edit mode" should {
      "save answers and redirect to the other items page" when {
        "they submit a single additional income" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.submitAdditionalIncome(Some(Seq(UkInterest)))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.OtherItemsController.show().url)
          )

          getPageData(SessionId, AdditionalIncomeSourcesPage) shouldBe Some(Seq(UkInterest))
        }
        "they submit multiple additional income" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.submitAdditionalIncome(Some(additionalIncomeFilters.map(_.key)))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.OtherItemsController.show().url)
          )

          getPageData(SessionId, AdditionalIncomeSourcesPage) shouldBe Some(additionalIncomeFilters)
        }
        "they submit none for their additional income" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.submitAdditionalIncome(Some(Seq.empty))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.OtherItemsController.show().url)
          )

          getPageData(SessionId, AdditionalIncomeSourcesPage) shouldBe Some(Seq.empty[VendorFilter])
        }
      }
    }
    "in edit mode" should {
      "save answers and redirect to the check your answers page" when {
        "they submit a single additional income" in {
          setPageData(SessionId, AdditionalIncomeSourcesPage, Seq.empty)

          val res = SoftwareChoicesFrontend.submitAdditionalIncome(Some(Seq(UkInterest)), editMode = true)

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, AdditionalIncomeSourcesPage) shouldBe Some(Seq(UkInterest))
        }
        "they submit multiple additional income" in {
          setPageData(SessionId, AdditionalIncomeSourcesPage, Seq.empty)

          val res = SoftwareChoicesFrontend.submitAdditionalIncome(Some(additionalIncomeFilters.map(_.key)), editMode = true)

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, AdditionalIncomeSourcesPage) shouldBe Some(additionalIncomeFilters)
        }
        "they submit none for their additional income" in {
          setPageData(SessionId, AdditionalIncomeSourcesPage, additionalIncomeFilters)

          val res = SoftwareChoicesFrontend.submitAdditionalIncome(Some(Seq.empty), editMode = true)

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, AdditionalIncomeSourcesPage) shouldBe Some(Seq.empty[VendorFilter])
        }
      }
    }
    "the user has no checkboxes selected" should {
      "return a bad request" in {
        setupAnswers(SessionId, None)

        val res = SoftwareChoicesFrontend.submitAdditionalIncome(None)

        res should have(
          httpStatus(BAD_REQUEST),
          pageTitle(s"Error: ${messages("additional.income.source-heading")} - ${PageContentBase.title} - GOV.UK")
        )

        getPageData(SessionId, AdditionalIncomeSourcesPage) shouldBe None
      }
    }
    "the user has additional income and the none checkbox selected" should {
      "return a bad request" in {
        setupAnswers(SessionId, None)

        val res = SoftwareChoicesFrontend.submitAdditionalIncome(Some(additionalIncomeFilters.map(_.key) :+ AdditionalIncomeForm.noneKey))

        res should have(
          httpStatus(BAD_REQUEST),
          pageTitle(s"Error: ${messages("additional.income.source-heading")} - ${PageContentBase.title} - GOV.UK")
        )

        getPageData(SessionId, AdditionalIncomeSourcesPage) shouldBe None
      }
    }
  }

  "backUrl" must {
    "return to guidance page when not in edit mode" in {
      additionalIncomeController.backUrl(editMode = false) shouldBe routes.BusinessIncomeController.show().url
    }
    "return to check your answers when in edit mode" in {
      additionalIncomeController.backUrl(editMode = true) shouldBe routes.CheckYourAnswersController.show().url
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()

    await(userFiltersRepository.collection.drop().toFuture())
  }

  lazy val additionalIncomeController: AdditionalIncomeSourcesController = app.injector.instanceOf[AdditionalIncomeSourcesController]

  lazy val additionalIncomeFilters: Seq[VendorFilter] = Seq(
    UkInterest,
    ConstructionIndustryScheme,
    Employment,
    UkDividends,
    StatePensionIncome,
    PrivatePensionIncome,
    ForeignDividends,
    ForeignInterest
  )

}
