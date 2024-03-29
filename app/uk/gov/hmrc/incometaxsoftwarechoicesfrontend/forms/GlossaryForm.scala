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

import play.api.data.Forms.{mapping, optional, text}
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.GlossaryFormModel

object GlossaryForm {

  val searchTerm = "searchTerm"
  val sortTerm = "sortTerm"

  private val trimmedOptionalText: Mapping[Option[String]] =
    optional(text)
      .transform(_.flatMap { value =>
        val trimmed = value.trim
        if (trimmed.isEmpty) None else Some(trimmed)
      }, identity)

  val form: Form[GlossaryFormModel] = Form(
    mapping(
      searchTerm -> trimmedOptionalText,
      sortTerm -> trimmedOptionalText
    )(GlossaryFormModel.apply)(GlossaryFormModel.unapply)
  )
}

