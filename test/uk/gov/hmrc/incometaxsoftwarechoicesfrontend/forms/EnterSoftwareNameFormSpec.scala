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

class EnterSoftwareNameFormSpec extends PlaySpec {

  "EnterSoftwareNameForm" when {
    "bound with a valid answer" must {
      "transform answer to Integer" in {
        val answer = Map("enter-software-name" -> Seq("101"))
        val boundForm = EnterSoftwareNameForm.form.bindFromRequest(answer)

        boundForm.value mustBe Some(101)
        boundForm.hasErrors mustBe false
      }
    }
    "transform Int into a page answer" in {
      val boundForm = EnterSoftwareNameForm.form.fill(102)

      boundForm.data mustBe Map("enter-software-name" -> "102")
      boundForm.hasErrors mustBe false
    }
    "validate the form" when {
      "no answer is provided" in {
        val answers = Map("enter-software-name" -> Seq.empty)
        val boundForm = EnterSoftwareNameForm.form.bindFromRequest(answers)

        boundForm.value mustBe None
        boundForm.errors mustBe Seq(FormError(EnterSoftwareNameForm.formKey, EnterSoftwareNameForm.formEmptyErrorKey))
      }
      "invalid answer is provided" in {
        val answer = Map("enter-software-name" -> Seq("invalid-answer"))

        try {
          EnterSoftwareNameForm.form.bindFromRequest(answer)
        } catch {
          case (ex: Exception) => ex.getMessage mustBe IllegalArgumentException("Invalid software product id: invalid-answer").getMessage

        }
      }
    }
  }

}
