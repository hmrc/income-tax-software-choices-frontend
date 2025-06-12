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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.utils

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationResult}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.utils.ConstraintUtil.constraint

object Constraints {
  val maxLength: (Int, String) => Constraint[Option[String]] = (length, msgKey) => constraint(oTerm =>
    oTerm.fold[ValidationResult](Valid) { term =>
      if (term.length <= length) Valid else Invalid(msgKey, length)
    }
  )

  val nonEmptySeq: String => Constraint[Seq[_]] = msgKey =>
    constraint(seq => if (seq.nonEmpty) Valid else Invalid(msgKey))
}
