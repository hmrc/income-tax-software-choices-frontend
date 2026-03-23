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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.helpers

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.*
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, UserAnswers}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.*

trait SelectBuilder {
  def buildSelects(vendors: Seq[SoftwareVendorModel])(implicit messages: Messages): Seq[SelectItem] = {

    val default = SelectItem(value = Some(""))
    
    val vendorList = vendors.map(vendor =>
      SelectItem(
        value = Some(vendor.productId.toString),
        text = vendor.name
      )
    )

    val otherOptionsList = Seq(
      SelectItem(
        value = Some("Google Sheets"),
        text = "Google Sheets"
      ),
      SelectItem(
        value = Some("Microsoft Excel"),
        text = "Microsoft Excel"
      )
    )
    
    default +: (vendorList ++ otherOptionsList)
  }


}
