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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.TestModels.fullSoftwareVendorsModel

class SoftwareVendorsSpec extends PlaySpec {


  val fullJson: JsObject = Json.obj(
    "lastUpdated" -> "2022-12-02",
    "vendors" -> Json.arr(
      Json.obj(
        "name" -> "software vendor name",
        "url" -> "/test-url",
        "email" -> "test@software-vendor-name.com",
        "phone" -> "00000 000 000",
        "website" -> "software-vendor-name.com",
        "filters" -> Json.arr(),
        "incomeAndDeductions" -> Json.arr(),
        "accessibilityStatementLink" -> "software-vendor-accessibility.com"
      )
    )
  )

  "SoftwareVendors" must {
    "read from json correctly" when {
      "the json is complete" in {
        Json.fromJson[SoftwareVendors](fullJson) mustBe JsSuccess(fullSoftwareVendorsModel)
      }
      "the json has no vendors" in {
        Json.fromJson[SoftwareVendors](
          fullJson - "vendors" ++ Json.obj("vendors" -> Json.arr())
        ) mustBe JsSuccess(fullSoftwareVendorsModel.copy(vendors = Seq.empty[SoftwareVendorModel]))
      }
    }
    "fail to read json" when {
      "lastUpdated is missing" in {
        val json: JsObject = fullJson - "lastUpdated"
        Json.fromJson[SoftwareVendors](json) mustBe JsError(JsPath \ "lastUpdated", "error.path.missing")
      }
      "vendors is missing" in {
        val json: JsObject = fullJson - "vendors"
        Json.fromJson[SoftwareVendors](json) mustBe JsError(JsPath \ "vendors", "error.path.missing")
      }
    }
  }

}
