/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.libs.json.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.{FutureVendor, Spreadsheet}

class OtherSoftwareSpec extends PlaySpec {

  val fullJson: JsObject = Json.obj(
    "software" -> Json.arr(
      Json.obj("productId" -> 1, "name" -> "software vendor name 1", "softwareType" -> "spreadsheet"),
      Json.obj("productId" -> 2, "name" -> "software vendor name 2", "softwareType" -> "future-vendor")
    )
  )

  val errorJson: JsObject = Json.obj(
    "software" -> Json.arr(
      Json.obj("name" -> "software vendor name 1", "softwareType" -> "spreadsheet"),
      Json.obj("productId" -> 2, "name" -> "software vendor name 2", "softwareType" -> "future-vendor")
    )
  )

  val fullModel: OtherSoftwareList = OtherSoftwareList(Seq(
    SoftwareProduct(1, "software vendor name 1", Spreadsheet),
    SoftwareProduct(2, "software vendor name 2", FutureVendor)
  ))

  "OtherSoftwareList" must {
    "read from json correctly" when {
      "the json is complete" in {
        Json.fromJson[OtherSoftwareList](fullJson) mustBe JsSuccess(fullModel)
      }
    }

    "fail to read json" when {
      "a json is empty" in {
        val json: JsObject = fullJson - "software"
        Json.fromJson[OtherSoftwareList](json) mustBe JsError(JsPath \ "software", "error.path.missing")
      }

      "a field is missing" in {
        Json.fromJson[OtherSoftwareList](errorJson) mustBe JsError(JsPath \ "software" \ (0) \ "productId", "error.path.missing")
      }

    }
  }

}
