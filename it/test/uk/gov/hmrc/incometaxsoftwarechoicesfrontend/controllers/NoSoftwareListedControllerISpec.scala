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

import play.api.http.Status.{OK, SEE_OTHER}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId

class NoSoftwareListedControllerISpec extends ComponentSpecBase  with DatabaseHelper {

  lazy val noSoftwareListedController: NoSoftwareListedController = app.injector.instanceOf[NoSoftwareListedController]

  s"GET ${routes.NoSoftwareListedController.show().url}" should {
    s"return $OK" in {
      setupAnswers(SessionId, None)

      val result = SoftwareChoicesFrontend.getNoListedSoftware()

      result should have(
        httpStatus(OK),
        pageTitle(s"${messages("not-listed-software.title")} - ${PageContentBase.title} - GOV.UK")
      )
    }
  }
}
