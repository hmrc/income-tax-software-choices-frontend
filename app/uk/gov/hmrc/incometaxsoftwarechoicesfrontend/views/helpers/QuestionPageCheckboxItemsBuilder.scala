/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.helpers

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.ExclusiveCheckbox
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod

object QuestionPageCheckboxItemsBuilder {
  trait KeyProvider[T]:
    def key(t: T): String

  object KeyProvider:
    given KeyProvider[AccountingPeriod] with
      def key(a: AccountingPeriod) = a.key

    given KeyProvider[VendorFilter] with
      def key(v: VendorFilter) = v.key


  def buildCheckboxes[T](checkboxItems: Seq[T], messagesPrefix: String, addNoneOfThese: Boolean = false)(implicit messages: Messages, kp:KeyProvider[T]): Seq[CheckboxItem] = {

    checkboxItems.map { item =>
      CheckboxItem(
        content = Text(messages(s"$messagesPrefix.${kp.key(item)}")),
        value = kp.key(item)
      )
    }
    ++
    (if (addNoneOfThese) {
      Seq(
        CheckboxItem(divider = Some(messages("base.or"))),
        CheckboxItem(
        content = Text(messages("base.none-of-these")),
        value = "none",
        behaviour = Some(ExclusiveCheckbox)
        )
      )
    } else {
      Seq.empty
    })
  }
}