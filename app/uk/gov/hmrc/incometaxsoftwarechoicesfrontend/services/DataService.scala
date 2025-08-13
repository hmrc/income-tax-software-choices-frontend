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

import play.api.Environment
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareVendors

import javax.inject.{Inject, Singleton}

@Singleton
class DataService @Inject()(
  appConfig: AppConfig,
  environment: Environment
) {

  private val softwareVendorsJson: JsValue = environment.resourceAsStream(appConfig.softwareChoicesVendorFileName) match {
    case Some(resource) =>
      Json.parse(resource)
    case None =>
      throw new InternalServerException(s"[DataService][jsonFile] - ${appConfig.softwareChoicesVendorFileName} not found")
  }

  private val softwareVendors = Json.fromJson[SoftwareVendors](softwareVendorsJson) match {
    case JsSuccess(value, _) =>
      value
    case JsError(errors) =>
      throw new InternalServerException(s"[DataService][softwareVendors] - Json parse failures - ${errors.mkString(",")}")
  }

  def getSoftwareVendors(): SoftwareVendors =
    softwareVendors

}
