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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{OverseasProperty, SoleTrader, UkProperty}

class BusinessIncomeFormSpec extends PlaySpec {

  "BusinessIncomeForm" must {
    "transform answers into a sequence of vendor filters" when {
      "bound with one valid answer" in {
        val answers = Map("businessIncome[]" -> Seq("overseas-property"))
        val boundForm = BusinessIncomeForm.form.bindFromRequest(answers)

        boundForm.value mustBe Some(Seq(OverseasProperty))
        boundForm.hasErrors mustBe false
      }
      "bound with multiple valid answers" in {
        val answers = Map("businessIncome[]" -> Seq("sole-trader", "uk-property", "overseas-property"))
        val boundForm = BusinessIncomeForm.form.bindFromRequest(answers)

        boundForm.value mustBe Some(Seq(SoleTrader, UkProperty, OverseasProperty))
        boundForm.hasErrors mustBe false
      }
    }

    "transform vendor filters into page answers" in {
      val vendorFilters = Seq(SoleTrader, UkProperty, OverseasProperty)
      val boundForm = BusinessIncomeForm.form.fill(vendorFilters)

      boundForm.data mustBe Map(
        "businessIncome[0]" -> "sole-trader",
        "businessIncome[1]" -> "uk-property",
        "businessIncome[2]" -> "overseas-property"
      )
      boundForm.hasErrors mustBe false
    }

    "validate the answers" when {
      "no answers are provided" in {
        val answers = Map("businessIncome[]" -> Seq.empty)
        val boundForm = BusinessIncomeForm.form.bindFromRequest(answers)

        boundForm.value mustBe None
        boundForm.errors mustBe Seq(FormError("businessIncome", "business-income.error.nonEmpty"))
      }
      "invalid vendor filter is provided" in {
        val answers = Map("businessIncome[]" -> Seq("not-an-income-source"))
        val boundForm = BusinessIncomeForm.form.bindFromRequest(answers)

        boundForm.value mustBe None
        boundForm.errors mustBe Seq(FormError("businessIncome", "business-income.error.nonEmpty"))
      }
      "both valid and invalid vendor filters are provided" in {
        val answers = Map("businessIncome[]" -> Seq("sole-trader", "not-an-income-source"))
        val boundForm = BusinessIncomeForm.form.bindFromRequest(answers)

        boundForm.value mustBe Some(Seq(SoleTrader))
        boundForm.hasErrors mustBe false
      }
    }
  }


}
