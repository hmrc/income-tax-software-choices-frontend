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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{OtherItemsModel, VendorFilter}


object OtherItemsForm {
  val formKey: String = "otherItems"
  val noneKey: String = "none"
  val formErrorKey: String = "other-items.error.non-empty"

  val form: Form[OtherItemsModel] = Form(
    single(
      formKey -> seq(text)
        .verifying(nonEmptySeqOrNone(noneKey, "other-items.error.non-empty", "other-items.error.invalid-selection"))
        .transform[OtherItemsModel](toVendorFilters, fromVendorFilters)
    )
  )

  private def toVendorFilters(seq: Seq[String]): OtherItemsModel = {
    val filters = seq.flatMap(string => VendorFilter.filterKeyToFilter.get(string))
    val noneOfThese = !seq.filter(_.equals(noneKey)).isEmpty
    OtherItemsModel(filters, noneOfThese)
  }

  private def fromVendorFilters(model: OtherItemsModel): Seq[String] =
    if (model.noneOfThese) Seq(noneKey) else model.filters.map(_.key)

}
