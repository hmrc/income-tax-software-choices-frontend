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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services

import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{OverseasProperty, SoleTrader, UkProperty}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilterGroups.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, SoftwareVendors, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.viewmodels.VendorSuitabilityViewModel

import javax.inject.{Inject, Singleton}

@Singleton
class SoftwareChoicesService @Inject()(
  dataService: DataService
) {

  def softwareVendors: SoftwareVendors =
    dataService.getSoftwareVendors()

  def getSoftwareVendor(software: String): Option[SoftwareVendorModel] = {
    softwareVendors
      .vendors
      .collectFirst {
        case vendor if vendor.name == software => vendor
      }
  }

  def getVendorsWithIntent(filters: Seq[VendorFilter]): Seq[VendorSuitabilityViewModel] = {
    Seq.empty
  }

  def getAllInOneVendors(filters: Seq[VendorFilter]): SoftwareVendors = {
    val vendors = softwareVendors
    vendors.copy(
      vendors = (
        SoftwareChoicesService.matchFilter(filters) _
          andThen SoftwareChoicesService.sortVendors
        ) (vendors.vendors)
    )
  }

  def getOtherVendors(filters: Seq[VendorFilter], isAgentOrZeroResults: Boolean = false): SoftwareVendors = {
    val allInOne = if (isAgentOrZeroResults) Seq.empty else getAllInOneVendors(filters).vendors
    val vendors = softwareVendors
    val userTypes = filters.filter(userTypeFilters.contains)
    val otherVendors = if (userTypes.isEmpty) {
      (SoftwareChoicesService.matchFilter(filters.filterNot(userPageFilters.contains)) _
        andThen SoftwareChoicesService.sortVendors
      )(vendors.vendors)
    } else {
      val accountingPeriod = filters.find(accountingPeriodFilters.contains)
      val mandatedIncomeSources = filters.filter(Seq(SoleTrader, UkProperty, OverseasProperty).contains)
      val vendorsForUser = vendors.vendors.filter { vendor =>
        vendor.mustHaveAll(userTypes) &&
          vendor.mustHaveOption(accountingPeriod)
      }
      val matchingVendors = if (mandatedIncomeSources.isEmpty) {
        vendorsForUser
      } else {
        vendorsForUser.filter(_.mustHaveAtLeast(mandatedIncomeSources))
      }
      val preferencesFilters = filters
        .filterNot(userTypes.contains)
        .filterNot(userPageFilters.contains)
        .filterNot(mandatoryFiltersForIndividuals.contains)
      (SoftwareChoicesService.matchFilter(preferencesFilters) _
        andThen SoftwareChoicesService.sortVendors
      )(matchingVendors)
    }
    vendors.copy(
      vendors = otherVendors.filterNot(allInOne.contains)
    )
  }
}

object SoftwareChoicesService {

  private[services] def sortVendors(vendors: Seq[SoftwareVendorModel]) =
    vendors.sortBy(vendor => vendor.name)

  private[services] def matchFilter(filters: Seq[VendorFilter])(vendors: Seq[SoftwareVendorModel]) =
    vendors.filter(vendor => filters.forall(vendor.filters.contains(_)))

}
