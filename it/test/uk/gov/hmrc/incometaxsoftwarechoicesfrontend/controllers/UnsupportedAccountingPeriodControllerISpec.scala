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

import play.api.http.Status.OK
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.ComponentSpecBase
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class UnsupportedAccountingPeriodControllerISpec extends ComponentSpecBase {

  s"GET ${routes.UnsupportedAccountingPeriodController.show}" should {
    s"return $OK" in {
      val result = SoftwareChoicesFrontend.getUnsupportedAccountingPeriod

      result should have(
        httpStatus(OK),
        pageTitle(s"${messages("unsupported-accounting-period.heading")} - ${PageContentBase.title} - GOV.UK"),
      )
    }
  }

}
