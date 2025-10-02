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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms

import play.api.data.Form
import play.api.data.Forms.{list, mapping, text}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.filterKeyToFilter
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{FiltersFormModel, VendorFilter}

object FiltersForm {
  val filters = "filters"

  def toVendorFilter(list: List[String]): Seq[VendorFilter] = {
    list.flatMap(filterKey => filterKeyToFilter.get(filterKey))
  }

  def fromVendorFilter(filterList: Seq[VendorFilter]): List[String] = {
    filterList.map(_.key).toList
  }

  val form: Form[FiltersFormModel] = Form(
    mapping(
      filters -> list(text).transform[Seq[VendorFilter]](toVendorFilter, fromVendorFilter)
    )(FiltersFormModel.apply)(f => Some(f.filters))
  )
}

