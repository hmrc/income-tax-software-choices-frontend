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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models

import org.scalatestplus.play.PlaySpec
import play.api.libs.json._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.TestModels.fullSoftwareVendorModel

class SoftwareVendorModelSpec extends PlaySpec {

  val fullJson: JsObject = Json.obj(
    "name" -> "software vendor name",
    "email" -> "test@software-vendor-name.com",
    "phone" -> "00000 000 000",
    "website" -> "software-vendor-name.com",
    "filters" -> Json.arr(
      VendorFilter.FreeVersion.key,
      VendorFilter.Visual.key,
      VendorFilter.Hearing.key,
      VendorFilter.Motor.key,
      VendorFilter.Cognitive.key
    ),
    "accessibilityStatementLink" -> "software-vendor-accessibility.com"
  )

  "SoftwareVendorModel" must {
    "read from json correctly" when {
      "the json is complete" in {
        Json.fromJson[SoftwareVendorModel](fullJson) mustBe JsSuccess(fullSoftwareVendorModel)
      }
      "the json has no contact details" in {
        Json.fromJson[SoftwareVendorModel](
          fullJson - "email" - "phone"
        ) mustBe JsSuccess(fullSoftwareVendorModel.copy(email = None, phone = None))
      }
      "the json has no filter options" in {
        Json.fromJson[SoftwareVendorModel](
          fullJson - "filters" ++ Json.obj("filters" -> Json.arr())
        ) mustBe JsSuccess(fullSoftwareVendorModel.copy(filters = Seq.empty[VendorFilter]))
      }
      "there is no accessibility statement link" in {
        Json.fromJson[SoftwareVendorModel](
          fullJson - "accessibilityStatementLink"
        ) mustBe JsSuccess(fullSoftwareVendorModel.copy(accessibilityStatementLink = None))
      }
    }
    "fail to read json" when {
      "name is missing" in {
        val json: JsObject = fullJson - "name"
        Json.fromJson[SoftwareVendorModel](json) mustBe JsError(JsPath \ "name", "error.path.missing")
      }
      "website is missing" in {
        val json: JsObject = fullJson - "website"
        Json.fromJson[SoftwareVendorModel](json) mustBe JsError(JsPath \ "website", "error.path.missing")
      }
      "filters is missing" in {
        val json: JsObject = fullJson - "filters"
        Json.fromJson[SoftwareVendorModel](json) mustBe JsError(JsPath \ "filters", "error.path.missing")
      }
    }


    "search for filters and order the results correctly" should {
      def getVendorModel(vendorFilters: Seq[VendorFilter]): SoftwareVendorModel = SoftwareVendorModel(
        name = "name",
        email = None,
        phone = None,
        website = "website",
        filters = vendorFilters,
        accessibilityStatementLink = None
      )

      "find no matches when a selection of elements is present in the list but not in the vendor" in {
        val vendorFilters = Seq(VendorFilter.Visual, VendorFilter.Hearing)
        val searchFilters: Set[VendorFilter] = Set(VendorFilter.Motor, VendorFilter.Cognitive)
        val model: SoftwareVendorModel = getVendorModel(vendorFilters)
        model.orderedFilterSubset(searchFilters) mustBe Seq.empty
      }

      "find a match when a single element is present both in the vendor and in the list" in {
        val vendorFilters = Seq(VendorFilter.Motor, VendorFilter.Cognitive, VendorFilter.Visual)
        val searchFilters: Set[VendorFilter] = Set(VendorFilter.Visual, VendorFilter.Hearing)
        val model: SoftwareVendorModel = getVendorModel(vendorFilters)
        model.orderedFilterSubset(searchFilters) mustBe Seq(VendorFilter.Visual)
      }

      "find and correctly order a selection of elements which are present in the vendor and  the list" in {
        val vendorFilters = Seq(VendorFilter.Motor, VendorFilter.Cognitive, VendorFilter.Visual, VendorFilter.Hearing).sortBy(_ => Math.random())
        val searchFilters: Set[VendorFilter] = Set(VendorFilter.Motor, VendorFilter.Cognitive, VendorFilter.Visual, VendorFilter.Hearing)
        val model: SoftwareVendorModel = getVendorModel(vendorFilters)
        val expectedResults = Seq(VendorFilter.Motor, VendorFilter.Cognitive, VendorFilter.Visual, VendorFilter.Hearing).sortBy(_.priority)
        model.orderedFilterSubset(searchFilters) mustBe expectedResults
      }
    }
  }
}
