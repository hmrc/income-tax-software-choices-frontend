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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.utils.Constraints.nonEmptySeqOrNone
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter


object OtherItemsForm {
  val formKey: String = "otherItems"
  val noneKey: String = "none"
  val formEmptyErrorKey: String = "other-items.error.non-empty"
  val formInvalidSelectionErrorKey: String = "other-items.error.invalid-selection"

  val form: Form[Seq[VendorFilter]] = Form(
    single(
      formKey -> seq(text)
        .verifying(nonEmptySeqOrNone(noneKey, formEmptyErrorKey, formInvalidSelectionErrorKey))
        .transform[Seq[VendorFilter]](toVendorFilters, fromVendorFilters)
    )
  )

  private def toVendorFilters(seq: Seq[String]): Seq[VendorFilter] = seq.flatMap(string => VendorFilter.filterKeyToFilter.get(string))

  private def fromVendorFilters(filters: Seq[VendorFilter]): Seq[String] = if (filters.isEmpty) Seq(noneKey) else filters.map(_.key)

}
