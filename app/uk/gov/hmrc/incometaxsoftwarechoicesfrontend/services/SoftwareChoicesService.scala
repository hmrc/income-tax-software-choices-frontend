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

import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilterGroups._
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
    val all = vendors.copy(
      vendors = (
        SoftwareChoicesService.matchFilter(filters.filterNot(userPageFilters.contains)) _
          andThen SoftwareChoicesService.sortVendors
        ) (vendors.vendors)
    )
    all.copy(
      vendors = all.vendors.filterNot(allInOne.contains)
    )
  }
}

object SoftwareChoicesService {

  private[services] def sortVendors(vendors: Seq[SoftwareVendorModel]) =
    vendors.sortBy(vendor => vendor.name)

  private[services] def matchFilter(filters: Seq[VendorFilter])(vendors: Seq[SoftwareVendorModel]) =
    vendors.filter(vendor => filters.forall(vendor.filters.contains(_)))

}
