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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareVendorModel
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

  val softwareVendors: Seq[SoftwareVendorModel] = Json.fromJson[Seq[SoftwareVendorModel]](softwareVendorsJson) match {
    case JsSuccess(value, _) =>
      value
    case JsError(errors) =>
      throw new InternalServerException(s"[SoftwareChoicesService][softwareVendors] - Json parse failures - ${errors.mkString(",")}")
  }

}

object SoftwareChoicesService {
  val softwareVendorsFileName: String = "software-vendors.json"
}
