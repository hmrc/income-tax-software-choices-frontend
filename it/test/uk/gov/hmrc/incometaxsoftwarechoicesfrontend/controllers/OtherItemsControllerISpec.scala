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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.OtherItemsForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.OtherItemsPage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class OtherItemsControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  s"GET ${routes.OtherItemsController.show().url}" should {
    "redirect to the service index" when {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.getOtherItems

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "display the page" when {
      "the other items page has not been answered previously" in {
        setupAnswers(SessionId, None)

        val res = SoftwareChoicesFrontend.getOtherItems

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("other-items.title")} - ${PageContentBase.title} - GOV.UK"),
          checkboxSelected("otherItems", None),
          checkboxSelected("otherItems-2", None),
          checkboxSelected("otherItems-3", None),
          checkboxSelected("otherItems-4", None),
          checkboxSelected("otherItems-5", None),
          checkboxSelected("otherItems-6", None),
          checkboxSelected("otherItems-7", None),
          checkboxSelected("otherItems-9", None)
        )
      }
      "the other items page has been answered previously with other items selections" in {
        setPageData(SessionId, OtherItemsPage, otherItemsFilters)

        val res = SoftwareChoicesFrontend.getOtherItems

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("other-items.title")} - ${PageContentBase.title} - GOV.UK"),
          checkboxSelected("otherItems", Some(PaymentsIntoAPrivatePension.key)),
          checkboxSelected("otherItems-2", Some(CharitableGiving.key)),
          checkboxSelected("otherItems-3", Some(CapitalGainsTax.key)),
          checkboxSelected("otherItems-4", Some(StudentLoans.key)),
          checkboxSelected("otherItems-5", Some(MarriageAllowance.key)),
          checkboxSelected("otherItems-6", Some(VoluntaryClass2NationalInsurance.key)),
          checkboxSelected("otherItems-7", Some(HighIncomeChildBenefitCharge.key)),
          checkboxSelected("otherItems-9", None)
        )
      }
      "the other items page has been answered previously with none selected" in {
        setPageData(SessionId, OtherItemsPage, Seq.empty)

        val res = SoftwareChoicesFrontend.getOtherItems

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("other-items.title")} - ${PageContentBase.title} - GOV.UK"),
          checkboxSelected("otherItems", None),
          checkboxSelected("otherItems-2", None),
          checkboxSelected("otherItems-3", None),
          checkboxSelected("otherItems-4", None),
          checkboxSelected("otherItems-5", None),
          checkboxSelected("otherItems-6", None),
          checkboxSelected("otherItems-7", None),
          checkboxSelected("otherItems-9", Some("none"))
        )
      }
    }
  }

  s"POST ${routes.OtherItemsController.submit().url}" when {
    "there is nothing saved in the database for this user" should {
      "redirect to the service index" in {
        val res = SoftwareChoicesFrontend.postOtherItems(Some(Seq(PaymentsIntoAPrivatePension)))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "not in edit mode" should {
      "save answers and redirect to the accounting period page" when {
        "they submit a single other item" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.postOtherItems(Some(Seq(PaymentsIntoAPrivatePension)))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.AccountingPeriodController.show().url)
          )

          getPageData(SessionId, OtherItemsPage) shouldBe Some(Seq(PaymentsIntoAPrivatePension))
        }
        "they submit multiple other items" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.postOtherItems(Some(otherItemsFilters.map(_.key)))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.AccountingPeriodController.show().url)
          )

          getPageData(SessionId, OtherItemsPage) shouldBe Some(otherItemsFilters)
        }
        "they submit none for their other items" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.postOtherItems(Some(Seq.empty))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.AccountingPeriodController.show().url)
          )

          getPageData(SessionId, OtherItemsPage) shouldBe Some(Seq.empty[VendorFilter])
        }
      }
    }
    "in edit mode" should {
      "save answers and redirect to the check your answers page" when {
        "they submit a single other item" in {
          setPageData(SessionId, OtherItemsPage, Seq.empty)

          val res = SoftwareChoicesFrontend.postOtherItems(Some(Seq(PaymentsIntoAPrivatePension)), editMode = true)

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, OtherItemsPage) shouldBe Some(Seq(PaymentsIntoAPrivatePension))
        }
        "they submit multiple other items" in {
          setPageData(SessionId, OtherItemsPage, Seq.empty)

          val res = SoftwareChoicesFrontend.postOtherItems(Some(otherItemsFilters.map(_.key)), editMode = true)

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, OtherItemsPage) shouldBe Some(otherItemsFilters)
        }
        "they submit none for their other items" in {
          setPageData(SessionId, OtherItemsPage, otherItemsFilters)

          val res = SoftwareChoicesFrontend.postOtherItems(Some(Seq.empty), editMode = true)

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, OtherItemsPage) shouldBe Some(Seq.empty[VendorFilter])
        }
      }
    }
    "the user has no checkboxes selected" should {
      "return a bad request" in {
        setupAnswers(SessionId, None)

        val res = SoftwareChoicesFrontend.postOtherItems(None)

        res should have(
          httpStatus(BAD_REQUEST),
          pageTitle(s"Error: ${messages("other-items.title")} - ${PageContentBase.title} - GOV.UK"),
        )

        getPageData(SessionId, OtherItemsPage) shouldBe None
      }
    }
    "the user has other items and the none checkbox selected" should {
      "return a bad request" in {
        setupAnswers(SessionId, None)

        val res = SoftwareChoicesFrontend.postOtherItems(Some(otherItemsFilters.map(_.key) :+ OtherItemsForm.noneKey))

        res should have(
          httpStatus(BAD_REQUEST),
          pageTitle(s"Error: ${messages("other-items.title")} - ${PageContentBase.title} - GOV.UK"),
        )

        getPageData(SessionId, OtherItemsPage) shouldBe None
      }
    }
  }

  "backUrl" must {
    "return to additional income page when not in edit mode" in {
      otherItemsController.backUrl(editMode = false) shouldBe routes.AdditionalIncomeSourcesController.show().url
    }
    "return to check your answers when in edit mode" in {
      otherItemsController.backUrl(editMode = true) shouldBe routes.CheckYourAnswersController.show().url
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()

    await(userFiltersRepository.collection.drop().toFuture())
  }

  lazy val otherItemsController: OtherItemsController = app.injector.instanceOf[OtherItemsController]

  lazy val otherItemsFilters: Seq[VendorFilter] = Seq(
    PaymentsIntoAPrivatePension,
    CharitableGiving,
    CapitalGainsTax,
    StudentLoans,
    MarriageAllowance,
    VoluntaryClass2NationalInsurance,
    HighIncomeChildBenefitCharge
  )

}
