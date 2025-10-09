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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.viewmodels

import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.Available
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareVendorModel
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{SoleTrader, UkProperty}

class VendorSuitabilityViewModelSpec extends PlaySpec {

  "VendorSuitabilityViewModel" should {
    "create an empty default model" which {
      val vendor = SoftwareVendorModel(
        name = "name",
        email = None,
        phone = None,
        website = "website",
        filters = Map(SoleTrader -> Available, UkProperty -> Available),
        accessibilityStatementLink = None
      )
      val model = VendorSuitabilityViewModel(vendor)

      "has correct vendor" in {
        model.vendor mustBe vendor
      }

      "has empty quarterly flag" in {
        model.quarterlyReady mustBe None
      }

      "has empty EOY flag" in {
        model.eoyReady mustBe None
      }
    }
  }

}
