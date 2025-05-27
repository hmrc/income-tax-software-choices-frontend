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

class VendorFilterSpec extends PlaySpec {

  "Vendor Filter Groups" should {

    "contain extra pricing elements when displayExtraPricingOptions is true" in {
      val filters = VendorFilterGroups.pricingFilters(true)

      filters.contains(VendorFilter.FreeTrial) mustBe true
      filters.contains(VendorFilter.PaidFor) mustBe true

      filters.contains(VendorFilter.FreeVersion) mustBe true
    }

    "not contain extra pricing elements when displayExtraPricingOptions is false" in {
      val filters = VendorFilterGroups.pricingFilters(false)

      filters.contains(VendorFilter.FreeTrial) mustBe false
      filters.contains(VendorFilter.PaidFor) mustBe false

      filters.contains(VendorFilter.FreeVersion) mustBe true
    }

    "contain ALL the filters (when displayExtraPricingOptions is true)" in {
      val filters = VendorFilterGroups.allGroups(true, true).flatMap(f => f._1) ++ VendorFilterGroups.featuresProvidedGroup.toSeq
      val allFilters = VendorFilter.filterKeyToFilter.values
      allFilters.map(f => filters.contains(f) mustBe true)
    }
  }

}
