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

import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.ComponentSpecBase
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FiltersFormModel
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.FreeVersion

class SearchSoftwareControllerISpec extends ComponentSpecBase {

  s"GET ${routes.SearchSoftwareController.show().url}" should {
    "respond with 200 status" in {
      When("GET / is called")
      val response = SoftwareChoicesFrontend.getSoftwareResults

      Then("Should return OK with the software search page")
      response should have(
        httpStatus(OK),
        pageTitle(s"""${messages("search-software.title")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK""")
      )
    }
  }

  s"GET ${routes.SearchSoftwareController.clear().url}" should {
    "redirect to the show page" in {
      When("GET / is called")
      val response = SoftwareChoicesFrontend.clear()

      Then("Should return SEE_OTHER to the show page")
      response should have(
        httpStatus(SEE_OTHER),
        redirectURI(routes.SearchSoftwareController.show().url)
      )
    }
  }

  s"POST ${routes.SearchSoftwareController.search().url}" should {
    "respond with 200 status" in {
      When("GET / is called")
      val response = SoftwareChoicesFrontend.submitSoftwareSearch(FiltersFormModel(Seq(FreeVersion)))

      Then("Should return OK with the software search page")
      response should have(
        httpStatus(OK),
        pageTitle(s"""${messages("search-software.title")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK""")
      )
    }
  }
}
