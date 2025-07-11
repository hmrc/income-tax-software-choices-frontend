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
  val nonEmptySeq: String => Constraint[Seq[_]] = msgKey =>
    constraint(seq => if (seq.nonEmpty) Valid else Invalid(msgKey))

  val nonEmptySeqOrNone: (String, String, String) => Constraint[Seq[_]] = (noneKey, emptyMsgKey, invalidSelectionMsgKey) =>
    constraint(seq => {
      val isNoneSelected = !seq.filter(_.equals(noneKey)).isEmpty
      isNoneSelected match {
        case false if seq.size > 0 => Valid // filter selections only
        case true if seq.size == 1 => Valid // none only
        case true if seq.size > 1 => Invalid(invalidSelectionMsgKey) // none and others
        case _ => Invalid(emptyMsgKey) // empty
      }
    })
}
