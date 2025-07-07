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

import play.api.http.Status.SEE_OTHER
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.ComponentSpecBase

class IndexControllerISpec extends ComponentSpecBase {

  lazy val indexController: IndexController = app.injector.instanceOf[IndexController]

  "GET /" must {
    "redirect to the user type page" in {
      val res = SoftwareChoicesFrontend.index()

      res should have(
        httpStatus(SEE_OTHER),
        redirectURI(routes.UserTypeController.show().url)
      )
    }
  }

}
