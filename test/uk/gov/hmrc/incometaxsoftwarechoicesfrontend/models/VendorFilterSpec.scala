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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.Agent
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.Individual
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilterGroups.*

class VendorFilterSpec extends PlaySpec {

  "Vendor Filter Groups" should {
    "contain ALL the filters without HMRCAssist" in {
      val filters = Seq(
        userTypeFilters,
        pricingFilters,
        softwareForFilters,
        compatibility,
        accessibilityFilters
      ).flatten
      Seq(false, true).foreach { isAgent =>
        val actual = VendorFilterGroups.allGroups(isAgent, withHMRCAssist = false).flatMap(f => f._1) ++ VendorFilterGroups.featuresProvidedGroup(withHMRCAssist = false)
        val expected = isAgent match {
          case true  => filters
          case false => filters.filter(_ != Agent).filter(_ != Individual)
        }
        expected.map(f => actual.contains(f) mustBe true)
      }
    }
    "contain ALL the filters including HMRCAssist" in {
      val filters = Seq(
        userTypeFilters,
        pricingFilters,
        softwareForFilters,
        compatibility,
        accessibilityFilters,
        extraFeatures
      ).flatten
      Seq(false, true).foreach { isAgent =>
        val actual = VendorFilterGroups.allGroups(isAgent, withHMRCAssist = true).flatMap(f => f._1) ++ VendorFilterGroups.featuresProvidedGroup(withHMRCAssist = true)
        val expected = isAgent match {
          case true  => filters
          case false => filters.filter(_ != Agent).filter(_ != Individual)
        }
        expected.map(f => actual.contains(f) mustBe true)
      }
    }
  }

}
