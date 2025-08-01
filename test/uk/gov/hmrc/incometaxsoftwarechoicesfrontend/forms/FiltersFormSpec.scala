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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{Cognitive, Hearing, Motor, Vat, Visual}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{FiltersFormModel, VendorFilter}

class FiltersFormSpec extends PlaySpec {

  "FiltersForm" should {
    "validate a filter" when {
      "several filters, all of which are known" in {
        val validInput = Map(
          "filters[0]" -> VendorFilter.FreeTrial.key,
          "filters[1]" -> VendorFilter.FreeVersion.key,
          "filters[2]" -> VendorFilter.Vat.key,
          "filters[3]" -> VendorFilter.SoleTrader.key,
          "filters[4]" -> VendorFilter.UkProperty.key,
          "filters[5]" -> VendorFilter.OverseasProperty.key,
          "filters[6]" -> Vat.key,
          "filters[7]" -> Visual.key,
          "filters[8]" -> Hearing.key,
          "filters[9]" -> Motor.key,
          "filters[10]" -> Cognitive.key,
        )
        FiltersForm.form.bind(validInput).value shouldBe Some(FiltersFormModel(
          List(
            VendorFilter.FreeVersion,
            VendorFilter.Vat,
            VendorFilter.SoleTrader,
            VendorFilter.UkProperty,
            VendorFilter.OverseasProperty,
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
        FiltersForm.form.bind(invalidInput).value shouldBe Some(FiltersFormModel(List()))
      }
    }
  }
}
