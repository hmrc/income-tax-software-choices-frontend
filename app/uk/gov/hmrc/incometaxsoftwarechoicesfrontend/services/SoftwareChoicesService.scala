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

import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.{Available, Intended}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilterGroups.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, SoftwareVendors, VendorFilter, VendorFilterGroups}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.viewmodels.VendorSuitabilityViewModel

import javax.inject.{Inject, Singleton}

@Singleton
class SoftwareChoicesService @Inject()(
  dataService: DataService
) {

  def softwareVendors: SoftwareVendors =
    dataService.getSoftwareVendors()

  def getSoftwareVendor(productId: Int): Option[SoftwareVendorModel] = {
    softwareVendors
      .vendors
      .collectFirst {
        case vendor if vendor.productId == productId => vendor
      }
  }

  def getVendorsWithIntent(filters: Seq[VendorFilter])(implicit appConfig: AppConfig): Seq[VendorSuitabilityViewModel] = {
    val userFilters = filters.filterNot(_.eq(TaxReturn)).filterNot(_.eq(QuarterlyUpdates))
    val allPotentialVendors = getAllInOneVendors(userFilters)

    val mandatoryFilters = filters.filter((mandatoryFilterGroup++quarterlyReturnsGroup).contains) ++ Seq(QuarterlyUpdates)
    val qualifyingVendors = (
      SoftwareChoicesService.matchAvailableFilter(mandatoryFilters)
        andThen SoftwareChoicesService.sortVendors
      )(allPotentialVendors.vendors)

    val nonMandatoryFilters = filters.filter(nonMandatedIncomeGroup.contains)
    val vendorsToDisplay = if (nonMandatoryFilters.isEmpty) {
      qualifyingVendors
    } else {
      (SoftwareChoicesService.matchAvailableOrIntendedFilter(nonMandatoryFilters ++ Seq(TaxReturn))
      andThen SoftwareChoicesService.sortVendors
      ) (qualifyingVendors)
    }

    vendorsToDisplay.map(vendor =>
      VendorSuitabilityViewModel(
        vendor = vendor,
        quarterlyReady = Some(vendor.isQuarterlyReady(filters)),
        eoyReady = vendor.isEoyReady(filters)
      ))
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

}

object SoftwareChoicesService {

  private[services] def sortVendors(vendors: Seq[SoftwareVendorModel]) =
    vendors.sortBy(vendor => vendor.name)

  private[services] def matchFilter(filters: Seq[VendorFilter])(vendors: Seq[SoftwareVendorModel]) =
    vendors.filter(vendor => filters.forall(vendor.filters.contains(_)))

  private[services] def matchAvailableFilter(filters: Seq[VendorFilter])(vendors: Seq[SoftwareVendorModel]) = {
    vendors.filter(vendor => filters.forall(filter => vendor.getFeatureStatus(filter).eq(Available)))
  }

  private[services] def matchAvailableOrIntendedFilter(filters: Seq[VendorFilter])(vendors: Seq[SoftwareVendorModel]) = {
    vendors.filter(vendor => filters.forall(filter => vendor.getFeatureStatus(filter).eq(Available) || vendor.getFeatureStatus(filter).eq(Intended)))
  }
}
