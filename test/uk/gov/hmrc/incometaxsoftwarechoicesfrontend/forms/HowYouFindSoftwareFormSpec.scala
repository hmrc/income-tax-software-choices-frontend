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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.{Check, Find}

class HowYouFindSoftwareFormSpec extends PlaySpec {

  "HowYouFindSoftwareForm" when {
    "bound with a valid answer" must {
      "transform answer to Find JourneyType" in {
        val answer = Map("how-you-find-software" -> Seq("find"))
        val boundForm = HowYouFindSoftwareForm.form.bindFromRequest(answer)

        boundForm.value mustBe Some(Find)
        boundForm.hasErrors mustBe false
      }
      "transform answer to Check JourneyType" in {
        val answer = Map("how-you-find-software" -> Seq("check"))
        val boundForm = HowYouFindSoftwareForm.form.bindFromRequest(answer)

        boundForm.value mustBe Some(Check)
        boundForm.hasErrors mustBe false
      }
    }
    "transform JourneyType into a page answer" in {
      val boundForm = HowYouFindSoftwareForm.form.fill(Find)

      boundForm.data mustBe Map("how-you-find-software" -> "find")
      boundForm.hasErrors mustBe false
    }
    "validate the form" when {
      "no answer is provided" in {
        val answers = Map("how-you-find-software" -> Seq.empty)
        val boundForm = HowYouFindSoftwareForm.form.bindFromRequest(answers)

        boundForm.value mustBe None
        boundForm.errors mustBe Seq(FormError(HowYouFindSoftwareForm.fieldName, HowYouFindSoftwareForm.errorKey))
      }
      "invalid answer is provided" in {
        val answer = Map("how-you-find-software" -> Seq("invalid-answer"))
        val boundForm = HowYouFindSoftwareForm.form.bindFromRequest(answer)

        boundForm.value mustBe None
        boundForm.errors mustBe Seq(FormError(HowYouFindSoftwareForm.fieldName, HowYouFindSoftwareForm.errorKey))
      }
    }
  }

}
