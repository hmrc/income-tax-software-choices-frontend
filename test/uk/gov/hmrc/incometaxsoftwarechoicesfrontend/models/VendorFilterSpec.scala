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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{Agent, FullyReady, Individual}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilterGroups.*

class VendorFilterSpec extends PlaySpec {

  "Preference filters" should {
    val allFilters = Seq(
      userTypeFilters,
      pricingFilters,
      readinessFilters,
      softwareForFilters,
      compatibility,
      accessibilityFilters,
      applicationTypeFilters,
      extraFeatures,
      languageFeature
    ).flatten.distinct

    "contain ALL required filters for an unguided user" in {
        val actual = VendorFilterGroups.preferenceFilters(isUnguided = true).flatMap(f => f._1)
        val expected = allFilters.filter(_ != FullyReady)
        expected.map(f => actual.contains(f) mustBe true)
    }

    "contain ALL required filters for a user NOT in the unguided journey" in {
        val individual = VendorFilterGroups.preferenceFilters(isUnguided = false).flatMap(f => f._1)
        val agent = VendorFilterGroups.preferenceFilters(isUnguided = false).flatMap(f => f._1)
        val expected = allFilters.filter(_ != Agent)
                                  .filter(_ != Individual)
        expected.map(f => individual.contains(f) mustBe true)
        expected.map(f => agent.contains(f) mustBe true)
    }
  }
}
