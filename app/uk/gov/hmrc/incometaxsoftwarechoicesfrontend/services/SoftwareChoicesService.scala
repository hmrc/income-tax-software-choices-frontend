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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilterGroups.{accountingPeriodFilters, userPageFilters, userTypeFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, SoftwareVendors, VendorFilter}

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

  def getAllInOneVendors(filters: Seq[VendorFilter] = Seq.empty): SoftwareVendors = {
    val vendors = softwareVendors
    vendors.copy(
      vendors = (
        SoftwareChoicesService.matchFilter(filters) _
          andThen SoftwareChoicesService.sortVendors
        ) (vendors.vendors)
    )
  }

  def getOtherVendors(filters: Seq[VendorFilter] = Seq.empty, isAgentOrZeroResults: Boolean = false): SoftwareVendors = {
    val allInOne = if (isAgentOrZeroResults) Seq.empty else getAllInOneVendors(filters).vendors
    val vendors = softwareVendors
    val userTypes = filters.filter(userTypeFilters.contains)
    userTypes.isEmpty match {
      case false =>
        val accountingPeriod = filters.find(accountingPeriodFilters.contains)
        val mandatedIncomeSources = filters.filter(Seq(SoleTrader, UkProperty, OverseasProperty).contains)
        val vendorsForUser = vendors.vendors.filter { v =>
          val contains = userTypes.map(v.filters.contains)
          val valid = contains.fold(false)((a, b) => a || b)
          accountingPeriod match {
            case Some(accountingPeriod) => valid && v.filters.contains(accountingPeriod)
            case None => valid
          }
        }
        val matchingVendors = mandatedIncomeSources.isEmpty match {
          case false =>
            mandatedIncomeSources.flatMap { incomeSource =>
              vendorsForUser.filter(_.filters.contains(incomeSource))
            }.distinct
          case true =>
            vendorsForUser
        }
        val preferencesFilters = filters
          .filterNot(userTypes.contains)
          .filterNot(userPageFilters.contains)
        val all = vendors.copy(
          vendors = (
            SoftwareChoicesService.matchFilter(preferencesFilters) _
              andThen SoftwareChoicesService.sortVendors
            )(matchingVendors)
        )
        all.copy(
          vendors = all.vendors.filterNot(allInOne.contains)
        )
      case true =>
        vendors.copy(
          vendors = (
            SoftwareChoicesService.matchFilter(filters.filterNot(userPageFilters.contains)) _
              andThen SoftwareChoicesService.sortVendors
            )(vendors.vendors)
        )
    }
  }
}

object SoftwareChoicesService {

  private[services] def sortVendors(vendors: Seq[SoftwareVendorModel]) =
    vendors.sortBy(vendor => vendor.name)

  private[services] def matchFilter(filters: Seq[VendorFilter])(vendors: Seq[SoftwareVendorModel]) =
    vendors.filter(vendor => filters.forall(vendor.filters.contains(_)))

}
