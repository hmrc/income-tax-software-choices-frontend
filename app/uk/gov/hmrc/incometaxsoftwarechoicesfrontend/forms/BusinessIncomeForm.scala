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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.utils.Constraints.nonEmptySeq
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter

object BusinessIncomeForm {

  val form: Form[Seq[VendorFilter]] = Form(
    single(
      "businessIncome" -> seq(text)
        .transform[Seq[VendorFilter]](toVendorFilters, fromVendorFilters)
        .verifying(nonEmptySeq("business-income.error.nonEmpty"))
    )
  )

  private def toVendorFilters(seq: Seq[String]): Seq[VendorFilter] = seq.flatMap(string => VendorFilter.filterKeyToFilter.get(string))

  private def fromVendorFilters(vendorFilter: Seq[VendorFilter]): Seq[String] = vendorFilter.map(_.key)

}
