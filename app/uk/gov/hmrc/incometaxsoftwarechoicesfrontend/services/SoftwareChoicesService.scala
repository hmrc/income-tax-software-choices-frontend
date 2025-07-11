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

import play.api.libs.json._
import play.api.{Environment, Logging}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, SoftwareVendors, VendorFilter}

import javax.inject.{Inject, Singleton}

@Singleton
class SoftwareChoicesService @Inject()(appConfig: AppConfig, environment: Environment) extends Logging {

  private val softwareVendorsJson: JsValue = environment.resourceAsStream(appConfig.softwareChoicesVendorFileName) match {
    case Some(resource) =>
      Json.parse(resource)
    case None =>
      throw new InternalServerException(s"[SoftwareChoicesService][jsonFile] - ${appConfig.softwareChoicesVendorFileName} not found")
  }

  private[services] val softwareVendors: SoftwareVendors = Json.fromJson[SoftwareVendors](softwareVendorsJson) match {
    case JsSuccess(value, _) =>
      value
    case JsError(errors) =>
      throw new InternalServerException(s"[SoftwareChoicesService][softwareVendors] - Json parse failures - ${errors.mkString(",")}")
  }

  def getSoftwareVendor(software: String): Option[SoftwareVendorModel] = {
    softwareVendors
      .vendors
      .collectFirst {
        case vendor if vendor.name == software => vendor
      }
  }

  def getVendors(filters: Seq[VendorFilter] = Seq.empty): SoftwareVendors = softwareVendors.copy(
    vendors = (
      SoftwareChoicesService.matchFilter(filters) _
        andThen SoftwareChoicesService.sortVendors
      ) (softwareVendors.vendors)
  )

}

object SoftwareChoicesService {

  private[services] def sortVendors(vendors: Seq[SoftwareVendorModel]) =
    vendors.sortBy(vendor => vendor.name)

  private[services] def matchFilter(filters: Seq[VendorFilter])(vendors: Seq[SoftwareVendorModel]) =
    vendors.filter(vendor => filters.forall(vendor.filters.contains(_)))

}
