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

import play.api.libs.json.{Json, Reads, __}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.CurrentFeature

case class SoftwareVendorModel(
  name: String,
  email: Option[String],
  phone: Option[String],
  website: String,
  filters: Map[VendorFilter,FeatureStatus],
  accessibilityStatementLink: Option[String] = None,
  nextRelease: Option[String] = None
) {
  def orderedFilterSubset(subsetFilters: Set[VendorFilter]): Map[VendorFilter,FeatureStatus] = {
    val filtersFromVendor = filters.filter(filter => subsetFilters.contains(filter._1)).toSet
    val alwaysDisplayedFilters = subsetFilters.filter(_.alwaysDisplay).map(_ -> CurrentFeature) // What is this used for
    (filtersFromVendor ++ alwaysDisplayedFilters).toSeq.sortBy(_._1.priority).toMap
  }


  def mustHaveAll(list: Seq[VendorFilter]): Boolean = {
    list.forall(filters.contains)
  }

  def mustHaveOption(optFilter: Option[VendorFilter]): Boolean =
    mustHaveAll(optFilter.toSeq)

  def mustHaveAtLeast(list: Seq[VendorFilter]): Boolean = {
    val contains = list.map(filters.contains)
    contains.fold(false)((a, b) => a || b)
  }
}

object SoftwareVendorModel {
  implicit val reads: Reads[SoftwareVendorModel] = Json.reads[SoftwareVendorModel]
}
