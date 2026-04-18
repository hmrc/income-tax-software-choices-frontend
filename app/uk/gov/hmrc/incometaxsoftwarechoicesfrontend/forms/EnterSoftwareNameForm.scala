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

import play.api.data.Form
import play.api.data.Forms.*
import play.api.data.validation.Constraint
import play.api.data.validation.Constraints.nonEmpty

object EnterSoftwareNameForm {
  val formKey: String = "enter-software-name"
  val formEmptyErrorKey: String = "enter-software-name.error.empty"

  val nonEmptyString: Constraint[String] = nonEmpty(formEmptyErrorKey)

  val form: Form[Int] = Form[Int](
    single(
      formKey -> text.verifying(nonEmptyString).transform[Int](
        str => str.toIntOption.getOrElse(throw new IllegalArgumentException(s"Invalid software product id: $str")),
        int => int.toString
      )
    )
  )



}
