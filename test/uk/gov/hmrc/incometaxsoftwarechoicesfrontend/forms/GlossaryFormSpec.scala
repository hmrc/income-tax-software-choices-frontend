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

import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.GlossaryFormModel

class GlossaryFormSpec extends PlaySpec {

  "form.bind" must {
    "return an empty glossary form model" when {
      "no data was provided to the form" in {
        val data: Map[String, String] = Map.empty
        val boundForm: Form[GlossaryFormModel] = GlossaryForm.form.bind(data)

        boundForm.hasErrors mustBe false
        boundForm.value mustBe Some(GlossaryFormModel())
      }
      "an empty string was provided to the form" in {
        val data: Map[String, String] = Map(GlossaryForm.searchTerm -> "")
        val boundForm: Form[GlossaryFormModel] = GlossaryForm.form.bind(data)

        boundForm.hasErrors mustBe false
        boundForm.value mustBe Some(GlossaryFormModel())
      }
      "only whitespace is provided to the form" in {
        val data: Map[String, String] = Map(GlossaryForm.searchTerm -> "   ")
        val boundForm: Form[GlossaryFormModel] = GlossaryForm.form.bind(data)

        boundForm.hasErrors mustBe false
        boundForm.value mustBe Some(GlossaryFormModel())
      }
    }
    "return a complete glossary form model" when {
      "text is provided to the form" in {
        val data: Map[String, String] = Map(GlossaryForm.searchTerm -> "search")
        val boundForm: Form[GlossaryFormModel] = GlossaryForm.form.bind(data)

        boundForm.hasErrors mustBe false
        boundForm.value mustBe Some(GlossaryFormModel(searchTerm = Some("search")))
      }
      "text with whitespace is provided to the form" in {
        val data: Map[String, String] = Map(GlossaryForm.searchTerm -> "   search    ")
        val boundForm: Form[GlossaryFormModel] = GlossaryForm.form.bind(data)

        boundForm.hasErrors mustBe false
        boundForm.value mustBe Some(GlossaryFormModel(searchTerm = Some("search")))
      }
      "text with multiple words is provided to the form" in {
        val data: Map[String, String] = Map(GlossaryForm.searchTerm -> "search for words")
        val boundForm: Form[GlossaryFormModel] = GlossaryForm.form.bind(data)

        boundForm.hasErrors mustBe false
        boundForm.value mustBe Some(GlossaryFormModel(searchTerm = Some("search for words")))
      }
    }
  }

  "form.fill" must {
    "turn a model into form data" when {
      "a model with data is provided" in {
        val form: Form[GlossaryFormModel] = GlossaryForm.form.fill(GlossaryFormModel(searchTerm = Some("search term")))
        form.data mustBe Map(GlossaryForm.searchTerm -> "search term")
      }
      "a model with an empty search is provided" in {
        val form: Form[GlossaryFormModel] = GlossaryForm.form.fill(GlossaryFormModel(searchTerm = Some("")))
        form.data mustBe Map(GlossaryForm.searchTerm -> "")
      }
      "a model with no search term is provided" in {
        val form: Form[GlossaryFormModel] = GlossaryForm.form.fill(GlossaryFormModel(searchTerm = None))
        form.data mustBe Map()
      }
    }

  }

}

/*private val trimmedOptionalText: Mapping[Option[String]] =
    optional(text)
      .transform(_.flatMap { value =>
        val trimmed = value.trim
        if (trimmed.isEmpty) None else Some(trimmed)
      }, identity)

  val form: Form[GlossaryFormModel] = Form(
    mapping(
      searchTerm -> trimmedOptionalText
    )(GlossaryFormModel.apply)(GlossaryFormModel.unapply)
  )*/
