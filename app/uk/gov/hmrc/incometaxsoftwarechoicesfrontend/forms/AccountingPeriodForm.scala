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

import play.api.data.Forms.*
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.utils.Constraints.nonEmptySeq
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod.{FirstAprilToThirtyFirstMarch, OtherAccountingPeriod, SixthAprilToFifthApril}

object AccountingPeriodForm {

  val formKey: String = "accounting-period"
  val noneKey: String = OtherAccountingPeriod.key

  private val initialPeriod: Mapping[Seq[String]] =
    seq(text)
      .verifying(nonEmptySeq("accounting-period.error"))
      .verifying("accounting-period.error", page => !(page.contains(noneKey) && page.size > 1))

  val accountingPeriodForm: Form[Seq[AccountingPeriod]] = Form(
    single(
      formKey ->
        initialPeriod
          .transform(toAccountingPeriods, fromAccountingPeriods)
    )
  )

  private def toAccountingPeriods(seq: Seq[String]): Seq[AccountingPeriod] =
    seq.flatMap(s => Seq(SixthAprilToFifthApril, FirstAprilToThirtyFirstMarch, OtherAccountingPeriod).find(_.key == s))

  private def fromAccountingPeriods(periods: Seq[AccountingPeriod]): Seq[String] =
    if (periods.isEmpty) Seq(noneKey) else periods.map(_.key)
}