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

import play.api.data.{Form, Mapping}
import play.api.data.Forms._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.utils.Constraints.nonEmptySeq
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter

object AdditionalIncomeForm {

  val formKey: String = "additionalIncome"
  val noneKey = "none"

  private val initialIncome: Mapping[Seq[String]] =
    seq(text)
      .verifying(nonEmptySeq("additional.income.source.error-non-empty"))
      .verifying("additional.income.source.error-none-only", page => !(page.contains(noneKey) && page.size > 1)
      )

  val form: Form[Seq[VendorFilter]] = Form(
    single(
      formKey ->
        initialIncome
          .transform(toVendorFilters, fromVendorFilters)
    )
  )

  private def toVendorFilters(seq: Seq[String]): Seq[VendorFilter] = seq.flatMap(string => VendorFilter.filterKeyToFilter.get(string))

  private def fromVendorFilters(vendorFilter: Seq[VendorFilter]): Seq[String] = if (vendorFilter.isEmpty) Seq(noneKey) else vendorFilter.map(_.key)

}