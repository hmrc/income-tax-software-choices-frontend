/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FiltersFormModel

class FiltersFormSpec extends PlaySpec with GuiceOneServerPerSuite {
  "FiltersForm" should {
    "validate a search term" when {
      "the search term is not empty" in {
        val validInput = Map(FilterFields.searchTerm.toString -> "Software search")
        FiltersForm.form.bind(validInput).value shouldBe Some(FiltersFormModel(Some("Software search")))
      }

      "the search term is not empty and contains spaces to be trimmed" in {
        val validInput = Map(FilterFields.searchTerm.toString -> " Software search  ")
        FiltersForm.form.bind(validInput).value shouldBe Some(FiltersFormModel(Some("Software search")))
      }

      "the search term contains only a space" in {
        val validInput = Map(FilterFields.searchTerm.toString -> " ")
        FiltersForm.form.bind(validInput).value shouldBe Some(FiltersFormModel(None))
      }

      "the search term is absent" in {
        val validInput = Map[String, String]()
        FiltersForm.form.bind(validInput).value shouldBe Some(FiltersFormModel(None))
      }
    }

    "invalidate a search term" when {
      "the search term is too long" in {
        val invalidInput = Map(FilterFields.searchTerm.toString -> "text" * 65)
        FiltersForm.form.bind(invalidInput).value shouldBe None
      }
    }
  }
}
