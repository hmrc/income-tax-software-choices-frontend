/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._

class AdditionalIncomeFormSpec extends PlaySpec {

  "AdditionalIncomeForm" must {
    "transform answers into a sequence of vendor filters" when {
      "bound with one valid answer" in {
        val answers = Map("additionalIncome[]" -> Seq("uk-interest"))

        val boundForm = AdditionalIncomeForm.form.bindFromRequest(answers)

        boundForm.value mustBe Some(Seq(UkInterest))
      }

      "bound with all valid answers" in {
        val answers = Map(
          "additionalIncome[]" -> Seq(
            "uk-interest",
            "construction-industry-scheme",
            "employment",
            "uk-dividends",
            "state-pension-income",
            "private-pension-income",
            "foreign-dividends",
            "foreign-interest"
          )
        )

        val boundForm = AdditionalIncomeForm.form.bindFromRequest(answers)

        boundForm.value mustBe Some(Seq(
          UkInterest,
          ConstructionIndustryScheme,
          Employment,
          UkDividends,
          StatePensionIncome,
          PrivatePensionIncome,
          ForeignDividends,
          ForeignInterest
        ))
      }
    }

    "transform vendor filters into page answers" when {
      "there are no vendor filters" in {
        val vendorFilters = Seq()
        val filled = AdditionalIncomeForm.form.fill(vendorFilters)

        filled.data mustBe Map(
          "additionalIncome[0]" -> AdditionalIncomeForm.noneKey
        )
        filled.hasErrors mustBe false
      }
      "there are all the vendor filters available" in {
        val vendorFilters = Seq(
          UkInterest,
          ConstructionIndustryScheme,
          Employment,
          UkDividends,
          StatePensionIncome,
          PrivatePensionIncome,
          ForeignDividends,
          ForeignInterest
        )
        val filled = AdditionalIncomeForm.form.fill(vendorFilters)

        filled.data mustBe Map(
          "additionalIncome[0]" -> "uk-interest",
          "additionalIncome[1]" -> "construction-industry-scheme",
          "additionalIncome[2]" -> "employment",
          "additionalIncome[3]" -> "uk-dividends",
          "additionalIncome[4]" -> "state-pension-income",
          "additionalIncome[5]" -> "private-pension-income",
          "additionalIncome[6]" -> "foreign-dividends",
          "additionalIncome[7]" -> "foreign-interest"
        )
        filled.hasErrors mustBe false
      }
    }

    "validate the answers" when {

      "no answers are provided" in {
        val answers = Map("additionalIncome" -> Seq.empty[String])
        val bound = AdditionalIncomeForm.form.bindFromRequest(answers)
        bound.value mustBe None
        bound.errors must contain(
          FormError("additionalIncome", "additional.income.source.error-non-empty")
        )
      }

      "none only" in {
        val answers = Map("additionalIncome[]" -> Seq("none"))
        val bound = AdditionalIncomeForm.form.bindFromRequest(answers)
        bound.value mustBe Some(Seq.empty)
        bound.hasErrors mustBe false
      }

      "none with other selections" in {
        val answers = Map("additionalIncome[]" -> Seq("employment", "none"))
        val bound = AdditionalIncomeForm.form.bindFromRequest(answers)
        bound.value mustBe None
        bound.errors must contain(
          FormError("additionalIncome", "additional.income.source.error-none-only")
        )
      }
    }
  }
}
