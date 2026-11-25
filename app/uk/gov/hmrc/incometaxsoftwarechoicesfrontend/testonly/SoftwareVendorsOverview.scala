/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.testonly

import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, SoftwareVendors, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.Intended
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilterGroups.{businessIncomeGroup, nonMandatedIncomeGroup}

import java.util.Locale

case class SoftwareVendorsOverview(softwareVendors: SoftwareVendors,
                                   search: String = "",
                                   selectedFeature: Option[VendorFilter] = None,
                                   developmentOnly: Boolean = false) {
  val groups: Seq[(String, Seq[VendorFilter])] = {
    val incomes = businessIncomeGroup ++ nonMandatedIncomeGroup ++
      Seq(ForeignIncome, AveragingAdjustment, FosterCarer, TrustIncome)
    val submissions = Seq(QuarterlyUpdates, TaxReturn)
    val otherFeatures = (VendorFilter.allFilters ++ softwareVendors.vendors.flatMap(_.filters.keys))
      .distinct.filterNot(filter => (incomes ++ submissions).contains(filter))
    Seq(
      "Mandated business incomes" -> businessIncomeGroup,
      "Other incomes and items (non-mandated)" -> incomes.filterNot(businessIncomeGroup.contains),
      "Quarterly updates and tax returns" -> submissions,
      "Other listing features" -> otherFeatures
    )
  }

  val incomeFeatures: Seq[VendorFilter] = groups.take(2).flatMap(_._2)
  val allFeatures: Seq[VendorFilter] = groups.flatMap(_._2)

  def outstandingFeatures(vendor: SoftwareVendorModel): Seq[VendorFilter] =
    allFeatures.filter(feature => vendor.filters.get(feature).contains(Intended))

  val productsInDevelopment: Int = softwareVendors.vendors.count(outstandingFeatures(_).nonEmpty)
  val productsWithIncomeInDevelopment: Int =
    softwareVendors.vendors.count(vendor => incomeFeatures.exists(feature => vendor.filters.get(feature).contains(Intended)))
  val incomeCommitments: Int =
    softwareVendors.vendors.map(vendor => incomeFeatures.count(feature => vendor.filters.get(feature).contains(Intended))).sum

  val matchingVendors: Seq[SoftwareVendorModel] = softwareVendors.vendors.filter { vendor =>
    vendor.name.toLowerCase(Locale.ROOT).contains(search.trim.toLowerCase(Locale.ROOT)) &&
      (!developmentOnly || selectedFeature.fold(outstandingFeatures(vendor).nonEmpty)(feature =>
        vendor.filters.get(feature).contains(Intended)))
  }.sortBy(vendor => (vendor.name.toLowerCase(Locale.ROOT), vendor.productId))

  val comparisonGroups: Seq[(String, Seq[VendorFilter])] = groups.flatMap { case (heading, features) =>
    val selected = features.filter(feature => selectedFeature.forall(_ == feature))
    Option.when(selected.nonEmpty)(heading -> selected)
  }
}
