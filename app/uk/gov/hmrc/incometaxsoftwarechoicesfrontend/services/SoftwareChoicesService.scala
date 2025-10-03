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

import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.Available
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{OverseasProperty, SoleTrader, UkProperty}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilterGroups.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{FeatureStatus, SoftwareVendorModel, SoftwareVendors, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.viewmodels.SoftwareVendorViewModel

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

  def getAllInOneVendors(filters: Seq[VendorFilter]): Seq[SoftwareVendorViewModel] = {
    val vendors = softwareVendors
    val filteredVendors: Seq[SoftwareVendorModel] = (
        SoftwareChoicesService.matchFilter(filters) _
          andThen SoftwareChoicesService.sortVendors
        ) (vendors.vendors)

    filteredVendors.map(vendor => SoftwareVendorViewModel(
      vendor = vendor,
      quarterlyReady = None,
      eoyReady = None
    ))

  }

  def getAllInOneVendorsWithIntent(filters: Seq[VendorFilter]): Seq[SoftwareVendorViewModel] = {
    val vendors = getAllInOneVendors(filters)
    val userMandatedIncomes = filters.filter(quarterlyReturnsGroup.contains)

    val filteredVendors = (
      SoftwareChoicesService.allAvailable(userMandatedIncomes)
        andThen SoftwareChoicesService.sortVendors
      )(vendors.map(_.vendor))

      filteredVendors.map(vendor => vendor).map(vendor => SoftwareVendorViewModel(
      vendor = vendor,
      quarterlyReady = Some(vendor.isQuarterlyReady(filters)),
      eoyReady = Some(vendor.isEoyReady(filters))
    ))

  }

  def getOtherVendors(filters: Seq[VendorFilter], isAgentOrZeroResults: Boolean = false): Seq[SoftwareVendorViewModel] = {
    val allInOne = if (isAgentOrZeroResults) Seq.empty else getAllInOneVendors(filters).map(_.vendor)
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

    otherVendors.filterNot(allInOne.contains).map(vendor => SoftwareVendorViewModel(
      vendor = vendor, None, None

    ))

  }
  
}

object SoftwareChoicesService {

  private[services] def sortVendors(vendors: Seq[SoftwareVendorModel]) =
    vendors.sortBy(vendor => vendor.name)

  private[services] def matchFilter(filters: Seq[VendorFilter])(vendors: Seq[SoftwareVendorModel]) =
    vendors.filter(vendor => filters.forall(vendor.filters.contains(_)))

  private[services] def allAvailable(filters: Seq[VendorFilter])(vendors: Seq[SoftwareVendorModel]) = {
    vendors.filter(vendor => filters.forall(filter => vendor.getFeatureStatus(filter).eq(Available)))
  }


}
