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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models

import play.api.libs.json.{Json, Reads}

case class SoftwareVendorModel(
  name: String,
  email: Option[String],
  phone: Option[String],
  website: String,
  filters: Seq[VendorFilter],
  intent: Option[Intent] = None,
  accessibilityStatementLink: Option[String] = None
) {
  def allFilters: Seq[VendorFilter] = intent match {
    case Some(intent) => filters ++ intent.filters
    case None => filters
  }

  def getFilterState(filter: VendorFilter): FilterState = {
    if (!allFilters.contains(filter)) {
      Unsupported
    } else intent match {
      case Some(intent) =>
        if (intent.filters.contains(filter)) Planned(intent.dateDue) else Supported
      case None => Supported
    }
  }

  def orderedFilterSubset(subsetFilters: Set[VendorFilter]): Seq[VendorFilter] = {
    val filtersFromVendor = allFilters.filter(filter => subsetFilters.contains(filter)).toSet
    val alwaysDisplayedFilters = subsetFilters.filter(_.alwaysDisplay)
    (filtersFromVendor ++ alwaysDisplayedFilters).toSeq.sortBy(_.priority)
  }

  def mustHaveAll(list: Seq[VendorFilter]): Boolean = {
    list.forall(allFilters.contains)
  }

  def mustHaveOption(optFilter: Option[VendorFilter]): Boolean =
    mustHaveAll(optFilter.toSeq)

  def mustHaveAtLeast(list: Seq[VendorFilter]): Boolean = {
    val contains = list.map(allFilters.contains)
    contains.fold(false)((a, b) => a || b)
  }
}

object SoftwareVendorModel {
  implicit val reads: Reads[SoftwareVendorModel] = Json.reads[SoftwareVendorModel]
}
