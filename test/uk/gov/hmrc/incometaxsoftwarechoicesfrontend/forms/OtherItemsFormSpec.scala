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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.OtherItemsForm._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._

class OtherItemsFormSpec extends PlaySpec {

  "OtherItemsForm" must {
    "transform answers into a sequence of vendor filters" when {
      "bound with one valid answer" in {
        val answers = Map("otherItems[]" -> Seq("payments-into-a-private-pension"))
        val boundForm = OtherItemsForm.form.bindFromRequest(answers)

        boundForm.value mustBe Some(Seq(PaymentsIntoAPrivatePension))
        boundForm.hasErrors mustBe false
      }
      "bound with multiple valid answers" in {
        val answers = Map("otherItems[]" -> Seq(
          "payments-into-a-private-pension",
          "charitable-giving",
          "capital-gains-tax",
          "student-loans",
          "marriage-allowance",
          "voluntary-class-2-national-insurance",
          "high-income-child-benefit-charge"
        ))
        val boundForm = OtherItemsForm.form.bindFromRequest(answers)

        boundForm.value mustBe Some(Seq(
          PaymentsIntoAPrivatePension,
          CharitableGiving,
          CapitalGainsTax,
          StudentLoans,
          MarriageAllowance,
          VoluntaryClass2NationalInsurance,
          HighIncomeChildBenefitCharge))

        boundForm.hasErrors mustBe false
      }
      "bound with None of these valid answer" in {
        val answers = Map("otherItems[]" -> Seq("none"))
        val boundForm = OtherItemsForm.form.bindFromRequest(answers)

        boundForm.value mustBe Some(Seq())
        boundForm.hasErrors mustBe false
      }
    }

    "transform vendor filters into page answers" in {
      val vendorFilters = Seq(
        PaymentsIntoAPrivatePension,
        CharitableGiving,
        CapitalGainsTax,
        StudentLoans,
        MarriageAllowance,
        VoluntaryClass2NationalInsurance,
        HighIncomeChildBenefitCharge)
      val boundForm = OtherItemsForm.form.fill(vendorFilters)

      boundForm.data mustBe Map(
        "otherItems[0]" -> "payments-into-a-private-pension",
        "otherItems[1]" -> "charitable-giving",
        "otherItems[2]" -> "capital-gains-tax",
        "otherItems[3]" -> "student-loans",
        "otherItems[4]" -> "marriage-allowance",
        "otherItems[5]" -> "voluntary-class-2-national-insurance",
        "otherItems[6]" -> "high-income-child-benefit-charge"
      )
      boundForm.hasErrors mustBe false
    }
    "transform None of these into page answers" in {
      val vendorFilters = Seq()
      val boundForm = OtherItemsForm.form.fill(vendorFilters)

      boundForm.data mustBe Map(
        "otherItems[0]" -> "none"
      )
      boundForm.hasErrors mustBe false
    }

    "validate the answers" when {
      "no answers are provided" in {
        val answers = Map("otherItems[]" -> Seq.empty)
        val boundForm = OtherItemsForm.form.bindFromRequest(answers)

        boundForm.value mustBe None
        boundForm.errors mustBe Seq(FormError(formKey, formEmptyErrorKey))
      }
      "invalid combination of vendor filter and None is provided" in {
        val answers = Map("otherItems[]" -> Seq("none", "charitable-giving"))
        val boundForm = OtherItemsForm.form.bindFromRequest(answers)

        boundForm.value mustBe None
        boundForm.errors mustBe Seq(FormError(formKey, formInvalidSelectionErrorKey))
      }
    }
  }


}
