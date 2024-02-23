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

import play.api.http.Status.OK
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.ComponentSpecBase
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.GlossaryFormModel

class GlossaryControllerISpec extends ComponentSpecBase {

  s"GET ${routes.GlossaryController.show.url}" should {
    "respond with 200 status" in {
      When(s"GET ${routes.GlossaryController.show.url} is called")
      val response = SoftwareChoicesFrontend.getGlossaryPage

      Then("Should return OK with the glossary page")
      response should have(
        httpStatus(OK),
        pageTitle(s"""${messages("glossary.title")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK""")
      )
    }
  }

  s"POST ${routes.GlossaryController.search(ajax = false).url}" should {
    "respond with 200 status" in {
      When(s"GET ${routes.GlossaryController.search(ajax = false).url} is called")
      val response = SoftwareChoicesFrontend.submitGlossaryPage(GlossaryFormModel(searchTerm = Some("HMRC")))

      Then("Should return OK with the glossary page")
      response should have(
        httpStatus(OK),
        pageTitle(s"""${messages("glossary.title")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK""")
      )
    }
  }

  s"POST ${routes.GlossaryController.search(ajax = true).url}" should {
    "respond with 200 status" in {
      When(s"GET ${routes.GlossaryController.search(ajax = true).url} is called")
      val response = SoftwareChoicesFrontend.submitGlossaryPageAjax(GlossaryFormModel(Some("HMRC")))

      Then("Should return OK with the glossary page list")
      response should have(
        httpStatus(OK),
        pageTitle("")
      )
    }
  }

}