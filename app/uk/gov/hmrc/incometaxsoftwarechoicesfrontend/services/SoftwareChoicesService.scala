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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.requests.SessionDataRequest
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, SoftwareVendors, VendorFilter, VendorFilterGroups}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.viewmodels.VendorSuitabilityViewModel

import javax.inject.{Inject, Singleton}
import scala.util.Random.shuffle

@Singleton
class SoftwareChoicesService @Inject()(
  dataService: DataService,
  userFiltersRepository: UserFiltersRepository
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

  def getVendorsWithIntent(finalFilters: Seq[VendorFilter])
                          (implicit appConfig: AppConfig, request: SessionDataRequest[_]): Seq[VendorSuitabilityViewModel] = {
    val allPotentialVendorsInRandOrder = getAllInOneVendors(finalFilters)

    val mandatoryFilters = finalFilters.filter((mandatoryFilterGroup++quarterlyReturnsGroup).contains) ++ Seq(QuarterlyUpdates)
    val qualifyingVendors = SoftwareChoicesService.matchAvailableFilter(mandatoryFilters)(allPotentialVendorsInRandOrder.vendors)

    val nonMandatoryFilters = finalFilters.filter(nonMandatedIncomeGroup.contains)
    val vendorsToDisplay = if (nonMandatoryFilters.isEmpty) {
      qualifyingVendors
    } else {
      SoftwareChoicesService.matchAvailableOrIntendedFilter(nonMandatoryFilters ++ Seq(TaxReturn))(qualifyingVendors)
    }

    vendorsToDisplay.map(vendor =>
      VendorSuitabilityViewModel(
        vendor = vendor,
        quarterlyReady = Some(vendor.isQuarterlyReady(finalFilters)),
        eoyReady = vendor.isEoyReady(finalFilters)
      ))
  }

  def getAllInOneVendors(finalFilters: Seq[VendorFilter])(implicit appConfig: AppConfig, request: SessionDataRequest[_]): SoftwareVendors = {
    val vendors = softwareVendors
    val selectedFilters = finalFilters.filterNot(_.eq(TaxReturn)).filterNot(_.eq(QuarterlyUpdates))

    val orderedMatchingVendors = if (request.userFilters.randomVendorOrder.isEmpty) {
      val randomisedVendors = vendors.copy(vendors = shuffle(vendors.vendors))
      userFiltersRepository.set(request.userFilters.copy(finalFilters = finalFilters, randomVendorOrder = randomisedVendors.vendors.map(_.productId).toList))

      SoftwareChoicesService.matchFilter(selectedFilters)(randomisedVendors.vendors)
    } else {
      val matchingVendors = SoftwareChoicesService.matchFilter(selectedFilters)(vendors.vendors)
      val vendorMap = matchingVendors.map(_.productId).zip(matchingVendors).toMap

      request.userFilters.randomVendorOrder.flatMap(vendorMap.get)
    }

    vendors.copy(vendors = orderedMatchingVendors)
  }

}

object SoftwareChoicesService {

  private[services] def matchFilter(filters: Seq[VendorFilter])(vendors: Seq[SoftwareVendorModel]) =
    vendors.filter(vendor => filters.forall(vendor.filters.contains(_)))

  private[services] def matchAvailableFilter(filters: Seq[VendorFilter])(vendors: Seq[SoftwareVendorModel]) = {
    vendors.filter(vendor => filters.forall(filter => vendor.getFeatureStatus(filter).eq(Available)))
  }

  private[services] def matchAvailableOrIntendedFilter(filters: Seq[VendorFilter])(vendors: Seq[SoftwareVendorModel]) = {
    vendors.filter(vendor => filters.forall(filter => vendor.getFeatureStatus(filter).eq(Available) || vendor.getFeatureStatus(filter).eq(Intended)))
  }
}
