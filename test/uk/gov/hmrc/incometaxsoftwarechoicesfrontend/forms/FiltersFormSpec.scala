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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms

import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{Android, AppleIOS, ApplicationBased, BrowserBased, Cognitive, Hearing, Motor, Vat, Visual}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{FiltersFormModel, VendorFilter}

class FiltersFormSpec extends PlaySpec with GuiceOneServerPerSuite {

  "FiltersForm" should {
    "validate a search term" when {
      "the search term is not empty" in {
        val validInput = Map(FiltersForm.searchTerm -> "Software search")
        FiltersForm.form.bind(validInput).value shouldBe Some(FiltersFormModel(Some("Software search")))
      }

      "the search term is not empty and contains spaces to be trimmed" in {
        val validInput = Map(FiltersForm.searchTerm -> " Software search  ")
        FiltersForm.form.bind(validInput).value shouldBe Some(FiltersFormModel(Some("Software search")))
      }

      "the search term contains only a space" in {
        val validInput = Map(FiltersForm.searchTerm -> " ")
        FiltersForm.form.bind(validInput).value shouldBe Some(FiltersFormModel(None))
      }

      "the search term is absent" in {
        val validInput = Map[String, String]()
        FiltersForm.form.bind(validInput).value shouldBe Some(FiltersFormModel(None))
      }
    }

    "invalidate a search term" when {
      "the search term is too long" in {
        val invalidInput = Map(FiltersForm.searchTerm -> "text" * 65)
        FiltersForm.form.bind(invalidInput).value shouldBe None
      }
    }
    "validate a filter" when {
      "the filter name is known" in {
        val validInput = Map("filters[0]" -> VendorFilter.Individual.key)
        FiltersForm.form.bind(validInput).value shouldBe Some(FiltersFormModel(None, List(VendorFilter.Individual)))
      }
      "several filters, all of which are known" in {
        val validInput = Map(
          "filters[0]" -> VendorFilter.Individual.key,
          "filters[1]" -> VendorFilter.Agent.key,
          "filters[2]" -> VendorFilter.FreeTrial.key,
          "filters[3]" -> VendorFilter.FreeVersion.key,
          "filters[4]" -> VendorFilter.MicrosoftWindows.key,
          "filters[5]" -> VendorFilter.MacOS.key,
          "filters[6]" -> VendorFilter.Vat.key,
          "filters[7]" -> VendorFilter.SoleTrader.key,
          "filters[8]" -> VendorFilter.UkProperty.key,
          "filters[9]" -> VendorFilter.OverseasProperty.key,
          "filters[10]" -> Android.key,
          "filters[11]" -> AppleIOS.key,
          "filters[12]" -> BrowserBased.key,
          "filters[13]" -> ApplicationBased.key,
          "filters[14]" -> Vat.key,
          "filters[15]" -> Visual.key,
          "filters[16]" -> Hearing.key,
          "filters[17]" -> Motor.key,
          "filters[18]" -> Cognitive.key,
        )
        FiltersForm.form.bind(validInput).value shouldBe Some(FiltersFormModel(
          None,
          List(
            VendorFilter.Individual,
            VendorFilter.Agent,
            VendorFilter.FreeTrial,
            VendorFilter.FreeVersion,
            VendorFilter.MicrosoftWindows,
            VendorFilter.MacOS,
            VendorFilter.Vat,
            VendorFilter.SoleTrader,
            VendorFilter.UkProperty,
            VendorFilter.OverseasProperty,
            VendorFilter.Android,
            VendorFilter.AppleIOS,
            VendorFilter.BrowserBased,
            VendorFilter.ApplicationBased,
            VendorFilter.Vat,
            VendorFilter.Visual,
            VendorFilter.Hearing,
            VendorFilter.Motor,
            VendorFilter.Cognitive,
          )
        ))
      }
      "the filter name is unknown" in {
        val invalidInput = Map("filters[0]" -> "rubbish")
        FiltersForm.form.bind(invalidInput).value shouldBe Some(FiltersFormModel(None, List()))
      }
    }
  }
}
