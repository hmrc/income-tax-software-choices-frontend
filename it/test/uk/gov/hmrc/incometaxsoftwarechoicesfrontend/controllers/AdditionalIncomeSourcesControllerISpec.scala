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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{ConstructionIndustryScheme, Employment, UkInterest}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.AdditionalIncomeSourcesPage
class AdditionalIncomeSourcesControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {
  def testUserFilters(answers: UserAnswers): UserFilters = UserFilters(SessionId, Some(answers))

  override def beforeEach(): Unit = {
    userFiltersRepository.collection.drop().toFuture()
    super.beforeEach()
  }

  "GET /additional-income" when {
    "there are no existing page answers" should {
      "display the page with an empty form" in {
        val res = SoftwareChoicesFrontend.getAdditionalIncome

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("additional.income.source-heading")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"),
          checkboxSelected("additionalIncome", None),
          checkboxSelected("additionalIncome-2", None),
          checkboxSelected("additionalIncome-3", None),
          checkboxSelected("additionalIncome-4", None),
          checkboxSelected("additionalIncome-5", None),
          checkboxSelected("additionalIncome-6", None),
          checkboxSelected("additionalIncome-7", None),
          checkboxSelected("additionalIncome-8", None),
          checkboxSelected("none", None)
        )
      }
    }
    "there is pre-filled data" should {
      "display the page with pre-filled checkboxes" in {
        val userAnswers = UserAnswers().set(AdditionalIncomeSourcesPage, Seq(UkInterest, ConstructionIndustryScheme, Employment)).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getAdditionalIncome

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("additional.income.source-heading")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"),
          checkboxSelected("additionalIncome", Some(UkInterest.key)),
          checkboxSelected("additionalIncome-2", Some(ConstructionIndustryScheme.key)),
          checkboxSelected("additionalIncome-3", Some(Employment.key)),
        )
      }
    }
  }

  "POST /additional-income" must {
    s"return $SEE_OTHER and save the page answers" when {
      "user submits one additional income source" in {
        val res = SoftwareChoicesFrontend.submitAdditionalIncome(Some(Seq(UkInterest)))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SearchSoftwareController.show.url)
        )
        getPageData(SessionId, AdditionalIncomeSourcesPage.toString).size shouldBe 1
      }
      "user submits multiple income sources" in {
        val res = SoftwareChoicesFrontend.submitAdditionalIncome(Some(Seq(UkInterest, ConstructionIndustryScheme, Employment)))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SearchSoftwareController.show.url)
        )
        getPageData(SessionId, AdditionalIncomeSourcesPage.toString).size shouldBe 1
      }
      "user submits none" in {
        val res = SoftwareChoicesFrontend.submitAdditionalIncome(Some(Seq.empty))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SearchSoftwareController.show.url)
        )
        getPageData(SessionId, AdditionalIncomeSourcesPage.toString).size shouldBe 1
      }
    }
    "return BAD_REQUEST" when {
      "user does not select an answer" in {
        val res = SoftwareChoicesFrontend.submitAdditionalIncome(None)

        res should have(
          httpStatus(BAD_REQUEST),
        )
        getPageData(SessionId, AdditionalIncomeSourcesPage.toString).size shouldBe 0
      }
      "user selects an income source and None" in {
        val res = SoftwareChoicesFrontend.submitAdditionalIncome(Some(Seq(UkInterest, AdditionalIncomeForm.noneKey)))

        res should have(
          httpStatus(BAD_REQUEST),
        )
        getPageData(SessionId, AdditionalIncomeSourcesPage.toString).size shouldBe 0
      }
    }
  }
}
