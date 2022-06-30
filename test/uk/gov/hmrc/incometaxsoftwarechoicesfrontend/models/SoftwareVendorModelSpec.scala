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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models

import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class SoftwareVendorModelSpec extends PlaySpec {

  val fullJson: JsObject = Json.obj(
    "name" -> "software vendor name",
    "url" -> "/test-url",
    "filters" -> Json.arr(
      Filter.FreeVersion.key,
      Filter.FreeTrail.key,
      Filter.Individual.key,
      Filter.Agent.key,
      Filter.MicrosoftWindows.key,
      Filter.MacOS.key,
      Filter.Android.key,
      Filter.AppleIOS.key,
      Filter.BrowserBased.key,
      Filter.ApplicationBased.key,
      Filter.Visual.key,
      Filter.Hearing.key,
      Filter.Motor.key,
      Filter.Cognitive.key
    )
  )

  val fullModel: SoftwareVendorModel = SoftwareVendorModel(
    name = "software vendor name",
    url = "/test-url",
    filters = Seq(
      Filter.FreeVersion,
      Filter.FreeTrail,
      Filter.Individual,
      Filter.Agent,
      Filter.MicrosoftWindows,
      Filter.MacOS,
      Filter.Android,
      Filter.AppleIOS,
      Filter.BrowserBased,
      Filter.ApplicationBased,
      Filter.Visual,
      Filter.Hearing,
      Filter.Motor,
      Filter.Cognitive
    )
  )

  "SoftwareVendorModel" must {
    "read from json correctly" when {
      "the json is complete" in {
        Json.fromJson[SoftwareVendorModel](fullJson) mustBe JsSuccess(fullModel)
      }
      "the json has no filter options" in {
        Json.fromJson[SoftwareVendorModel](
          fullJson - "filters" ++ Json.obj("filters" -> Json.arr())
        ) mustBe JsSuccess(fullModel.copy(filters = Seq.empty[Filter]))
      }
    }
    "fail to read json" when {
      "name is missing" in {
        val json: JsObject = fullJson - "name"
        Json.fromJson[SoftwareVendorModel](json) mustBe JsError(JsPath \ "name", "error.path.missing")
      }
      "url is missing" in {
        val json: JsObject = fullJson - "url"
        Json.fromJson[SoftwareVendorModel](json) mustBe JsError(JsPath \ "url", "error.path.missing")
      }
      "filters is missing" in {
        val json: JsObject = fullJson - "filters"
        Json.fromJson[SoftwareVendorModel](json) mustBe JsError(JsPath \ "filters", "error.path.missing")
      }
    }
  }

}
