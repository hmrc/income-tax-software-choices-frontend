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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.{Recognised, Spreadsheet}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareProduct, UserAnswers}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.EnterSoftwareNamePage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class FullyCompatibleControllerISpec
  extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  s"GET ${routes.FullyCompatibleController.show().url}" should {
    "redirect to the service index" when {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.getSoftwareInDevelopment

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "display the page for a recognised product" in {
      val softwareProduct = SoftwareProduct(0, "A1 Tax Stuff", Recognised)
      val userAnswers = UserAnswers()
        .set(EnterSoftwareNamePage, softwareProduct).get

      setupAnswers(SessionId, Some(userAnswers))

      val res = SoftwareChoicesFrontend.getFullyCompatible()

      res should have(
        httpStatus(OK),
        pageTitle(s"${softwareProduct.name} ${messages("fully-compatible.heading1")} - ${PageContentBase.title} - GOV.UK"),
      )
    }
    "display an error for a non-recognised product" in {
      val softwareProduct = SoftwareProduct(0, "Old-Fashioned Tax Stuff", Spreadsheet)
      val userAnswers = UserAnswers()
        .set(EnterSoftwareNamePage, softwareProduct).get

      setupAnswers(SessionId, Some(userAnswers))

      val res = SoftwareChoicesFrontend.getFullyCompatible()

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }

  }
  override def beforeEach(): Unit = {
    super.beforeEach()
    await(userFiltersRepository.collection.drop().toFuture())
  }
}
