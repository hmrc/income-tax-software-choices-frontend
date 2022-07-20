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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{FiltersFormModel, SoftwareVendors, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.SoftwareChoicesService.softwareVendorsFileName

import javax.inject.{Inject, Singleton}

@Singleton
class SoftwareChoicesService @Inject()(environment: Environment) extends Logging {

  private val softwareVendorsJson: JsValue = environment.resourceAsStream(softwareVendorsFileName) match {
    case Some(resource) =>
      Json.parse(resource)
    case None =>
      throw new InternalServerException("[SoftwareChoicesService][jsonFile] - file not found")
  }

  val softwareVendors: SoftwareVendors = Json.fromJson[SoftwareVendors](softwareVendorsJson) match {
    case JsSuccess(value, _) =>
      value
    case JsError(errors) =>
      throw new InternalServerException(s"[SoftwareChoicesService][softwareVendors] - Json parse failures - ${errors.mkString(",")}")
  }

  val filters: Seq[VendorFilter] = softwareVendors.vendors.head.filters

  def filter(filtersFormModel: FiltersFormModel): SoftwareVendors = filterAll(filtersFormModel)(softwareVendors)

  def filterAll(filtersFormModel: FiltersFormModel): SoftwareVendors => SoftwareVendors =
    filterVendors(filtersFormModel.searchTerm) _ andThen
      filterIndividual(filtersFormModel.individual) andThen
      filterAgent(filtersFormModel.agent)

  private[services] def filterVendors(maybeSearchTerm: Option[String])(vendors: SoftwareVendors): SoftwareVendors = maybeSearchTerm match {
    case Some(searchTerm) if searchTerm.nonEmpty =>
      vendors.copy(vendors = vendors.vendors.filter(_.name.toLowerCase.contains(searchTerm.toLowerCase())))
    case None => vendors
  }

  private def filterIndividual(maybeIndividual: Option[String])(vendors: SoftwareVendors): SoftwareVendors = maybeIndividual match {
    case None => vendors
    case _ => vendors.copy(vendors = vendors.vendors.filter(_.filters.contains(Individual)))
  }

  private def filterAgent(maybeAgent: Option[String])(vendors: SoftwareVendors): SoftwareVendors = maybeAgent match {
    case None => vendors
    case _ => vendors.copy(vendors = vendors.vendors.filter(_.filters.contains(Agent)))
  }
}

object SoftwareChoicesService {

  val softwareVendorsFileName: String = "software-vendors.json"

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
