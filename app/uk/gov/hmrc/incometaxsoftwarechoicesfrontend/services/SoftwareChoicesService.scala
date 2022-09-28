/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
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

  val softwareVendors: SoftwareVendors = Json.fromJson[SoftwareVendors](softwareVendorsJson) match {
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

  def filterVendors(maybeSearchTerm: Option[String], filters: Seq[VendorFilter]): SoftwareVendors = softwareVendors.copy(
    vendors = (SoftwareChoicesService.matchSearchTerm(maybeSearchTerm) _ andThen SoftwareChoicesService.matchFilter(filters)) (softwareVendors.vendors)
  )

}

object SoftwareChoicesService {

  private[services] def matchSearchTerm(maybeSearchTerm: Option[String])(vendors: Seq[SoftwareVendorModel]) = {
    val searchTermWords = maybeSearchTerm.map(_.toLowerCase().split("\\s+").toSeq).getOrElse(Seq.empty)
    vendors.filter(vendor => searchTermWords.forall(vendor.name.toLowerCase().contains(_)))
  }

  private[services] def matchFilter(filters: Seq[VendorFilter])(vendors: Seq[SoftwareVendorModel]) =
    vendors.filter(vendor => filters.forall(vendor.filters.contains(_)))

  val businessTypeFilters: Set[VendorFilter] = Set(
    Individual,
    Agent
  )

  val compatibleWithFilters: Set[VendorFilter] = Set(
    MicrosoftWindows,
    MacOS
  )

  val mobileAppFilters: Set[VendorFilter] = Set(
    Android,
    AppleIOS
  )

  val softwareTypeFilters: Set[VendorFilter] = Set(
    BrowserBased,
    ApplicationBased
  )

  val accessibilityFilters: Set[VendorFilter] = Set(
    Visual,
    Hearing,
    Motor,
    Cognitive
  )

}
