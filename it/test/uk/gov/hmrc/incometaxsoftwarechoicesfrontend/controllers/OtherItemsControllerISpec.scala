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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.OtherItemsPage

class OtherItemsControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  lazy val otherItemsController = app.injector.instanceOf[OtherItemsController]

  def testUserFilters(answers: UserAnswers): UserFilters = UserFilters(SessionId, Some(answers))

  override def beforeEach(): Unit = {
    userFiltersRepository.collection.drop().toFuture()
    super.beforeEach()
  }

  "GET /other-items" when {
    "there are no existing page answers" should {
      "display the page with an empty form" in {
        val res = SoftwareChoicesFrontend.getOtherItems

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("other-items.title")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"),
          checkboxSelected("otherItems", None),
          checkboxSelected("otherItems-2", None),
          checkboxSelected("otherItems-3", None),
          checkboxSelected("otherItems-4", None),
          checkboxSelected("otherItems-5", None),
          checkboxSelected("otherItems-6", None),
          checkboxSelected("otherItems-7", None),
          checkboxSelected("otherItems-8", None),
          checkboxSelected("otherItems-9", None)
        )
      }
    }
    "there is pre-filled data" should {
      "display the page with pre-filled checkboxes" in {
        val userAnswers = UserAnswers().set(OtherItemsPage,
          Seq(
            PaymentsIntoAPrivatePension,
            CharitableGiving,
            CapitalGainsTax,
            StudentLoans,
            MarriageAllowance,
            VoluntaryClass2NationalInsurance,
            HighIncomeChildBenefitCharge)).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getOtherItems

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("other-items.title")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"),
          checkboxSelected("otherItems", Some("payments-into-a-private-pension")),
          checkboxSelected("otherItems-2", Some("charitable-giving")),
          checkboxSelected("otherItems-3", Some("capital-gains-tax")),
          checkboxSelected("otherItems-4", Some("student-loans")),
          checkboxSelected("otherItems-5", Some("marriage-allowance")),
          checkboxSelected("otherItems-6", Some("voluntary-class-2-national-insurance")),
          checkboxSelected("otherItems-7", Some("high-income-child-benefit-charge")),
          checkboxSelected("otherItems-8", None),
          checkboxSelected("otherItems-9", None)
        )
      }
      "display the page with None of these pre-filled" in {
        val userAnswers = UserAnswers().set(OtherItemsPage, Seq()).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getOtherItems

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("other-items.title")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"),
          checkboxSelected("otherItems-9", Some("none"))
        )
      }
    }
  }

  "POST /other-items" must {
    s"return $SEE_OTHER and save the page answers" when {
      "not in edit mode" when {
        "user submits one other item" in {
          val res = SoftwareChoicesFrontend.postOtherItems(Some(Seq(StudentLoans)))

          res should have(
            httpStatus(SEE_OTHER),
          redirectURI(routes.AccountingPeriodController.show.url)
          )
          getPageData(SessionId, OtherItemsPage.toString).size shouldBe 1
        }
        "user submits multiple other items" in {
          val res = SoftwareChoicesFrontend.postOtherItems(Some(allItems))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show.url)
          )
          getPageData(SessionId, OtherItemsPage.toString).size shouldBe 1
        }
        "user submits None of these" in {
          val res = SoftwareChoicesFrontend.postOtherItems(Some(Seq.empty))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show.url)
          )
          getPageData(SessionId, OtherItemsPage.toString).size shouldBe 1
        }
      }
      "in edit mode" when {
        "user submits multiple other items" in {
          val res = SoftwareChoicesFrontend.postOtherItems(
            maybeKeys = Some(allItems),
            editMode = true
          )

          res should have(
            httpStatus(SEE_OTHER),
          redirectURI(routes.AccountingPeriodController.show.url)
          )
          getPageData(SessionId, OtherItemsPage.toString).size shouldBe 1
        }
        "user submits None of these" in {
          val res = SoftwareChoicesFrontend.postOtherItems(Some(Seq.empty))

          res should have(
            httpStatus(SEE_OTHER),
          redirectURI(routes.AccountingPeriodController.show.url)
          )
          getPageData(SessionId, OtherItemsPage.toString).size shouldBe 1
        }
      }
    }
    "return BAD_REQUEST" when {
      "user does not select an answer" in {
        val res = SoftwareChoicesFrontend.postOtherItems(None)

        res should have(
          httpStatus(BAD_REQUEST),
        )
        getPageData(SessionId, OtherItemsPage.toString).size shouldBe 0
      }
    }
  }

  "backUrl" must {
    "return to additional income page when not in edit mode" in {
      otherItemsController.backUrl(editMode = false) shouldBe routes.AdditionalIncomeSourcesController.show().url
    }
    "return to check your answers when in edit mode" in {
      otherItemsController.backUrl(editMode = true) shouldBe routes.CheckYourAnswersController.show.url
    }
  }

  private val allItems: Seq[String] = Seq(
    PaymentsIntoAPrivatePension,
    CharitableGiving,
    CapitalGainsTax,
    StudentLoans,
    MarriageAllowance,
    VoluntaryClass2NationalInsurance,
    HighIncomeChildBenefitCharge
  )

}
