/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.data.Forms.{list, mapping, optional, text}
import play.api.data.validation.Constraint
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.utils.StringConstraints
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.filterKeyToFilter
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{FiltersFormModel, VendorFilter}

object FiltersForm {
  val searchTerm = "searchTerm"
  val filters = "filters"

  private val searchTermMaxLength = 256

  private val trimmedOptionalText: Mapping[Option[String]] =
    optional(text)
      .transform(_.flatMap { value =>
        val trimmed = value.trim
        if (trimmed.isEmpty) None else Some(trimmed)
      }, identity)

  def toVendorFilter(list: List[String]): Seq[VendorFilter] = {
    list.flatMap(filterKey => filterKeyToFilter.get(filterKey))
  }

  def fromVendorFilter(filterList: Seq[VendorFilter]): List[String] = {
    filterList.map(_.key).toList
  }

  private val nameMaxLength: Constraint[Option[String]] = StringConstraints.maxLength(searchTermMaxLength, "search-software.search-form.error")

  val form: Form[FiltersFormModel] = Form(
    mapping(
      searchTerm -> trimmedOptionalText.verifying(nameMaxLength),
      filters -> list(text).transform[Seq[VendorFilter]](toVendorFilter, fromVendorFilter)
    )(FiltersFormModel.apply)(FiltersFormModel.unapply)
  )
}

