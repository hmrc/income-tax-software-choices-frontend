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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.{Available, NotApplicable}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilterGroups.{nonMandatedIncomeGroup, quarterlyReturnsGroup}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{TaxReturn, QuarterlyUpdates}

case class SoftwareVendorModel(
  name: String,
  email: Option[String],
  phone: Option[String],
  website: String,
  filters: Map[VendorFilter, FeatureStatus],
  accessibilityStatementLink: Option[String] = None
) {
  def orderedFilterSubset(subsetFilters: Set[VendorFilter]): Map[VendorFilter, FeatureStatus] = {
    val filtersFromVendor = filters.filter(filter => subsetFilters.contains(filter._1)).toSet
    filtersFromVendor.toSeq.sortBy(_._1.priority).toMap
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

  def getFeatureStatus(vf: VendorFilter): FeatureStatus = {
    filters.getOrElse(vf, NotApplicable)
  }

  def isQuarterlyReady(searchFilters: Seq[VendorFilter]): Boolean = {
    val userMandatedIncomes = searchFilters.filter(quarterlyReturnsGroup.contains) ++ Seq(QuarterlyUpdates)
    userMandatedIncomes.forall(filter => getFeatureStatus(filter).eq(Available))
  }

  def isEoyReady(searchFilters: Seq[VendorFilter]): Option[Boolean] = {
    val nonMandatoryFilters = searchFilters.filter(nonMandatedIncomeGroup.contains)
    
    if (nonMandatoryFilters.isEmpty && getFeatureStatus(TaxReturn).eq(NotApplicable)) 
      None
    else 
      Some((nonMandatoryFilters ++ Seq(TaxReturn)).forall(filter => getFeatureStatus(filter).eq(Available)))
  }
}

object SoftwareVendorModel {
  implicit val reads: Reads[SoftwareVendorModel] = Json.reads[SoftwareVendorModel]
}
