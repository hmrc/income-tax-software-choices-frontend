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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.helpers

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserAnswers
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages._

trait SummaryListBuilder {
  def buildSummaryList(userAnswersOpt: Option[UserAnswers])(implicit messages: Messages): SummaryList = {
    val userAnswers = userAnswersOpt.getOrElse(throw new InternalServerException("[SummaryListBuilder][buildSummaryList] - User answers is empty found"))
    SummaryList(
      Seq(
        businessIncomeSummaryListRow(userAnswers),
        otherIncomeSummaryListRow(userAnswers),
        otherItemsSummaryListRow(userAnswers),
        accountingPeriodSummaryListRow(userAnswers)
      )
    )
  }

  private def businessIncomeSummaryListRow(userAnswers: UserAnswers)(implicit messages: Messages): SummaryListRow = {
    val filterList: String = userAnswers.get(BusinessIncomePage) match {
      case Some(vf) if vf.nonEmpty => vf.map(f => messages(s"business-income.${f.key}")).mkString("<br>")
      case _ => throw new InternalServerException("[SummaryListBuilder][businessIncomeSummaryListRow] - Business income sources data not found")
    }

    summaryListRow(filterList, routes.BusinessIncomeController.show(editMode = true).url, "business-income")
  }

  private def otherIncomeSummaryListRow(userAnswers: UserAnswers)(implicit messages: Messages): SummaryListRow = {
    val filterList: String = userAnswers.get(AdditionalIncomeSourcesPage) match {
      case Some(vf) if vf.isEmpty => messages(s"check-your-answers.none-selected")
      case Some(vf) => vf.map(f => messages(s"additional.income.source-${f.key}")).mkString("<br>")
      case None => throw new InternalServerException("[SummaryListBuilder][otherIncomeSummaryListRow] - Other income sources data not found")
    }

    summaryListRow(filterList, routes.AdditionalIncomeSourcesController.show(editMode = true).url, "additional-income")
  }

  private def otherItemsSummaryListRow(userAnswers: UserAnswers)(implicit messages: Messages): SummaryListRow = {
    val filterList: String = userAnswers.get(OtherItemsPage) match {
      case Some(vf) if vf.isEmpty => messages(s"check-your-answers.none-selected")
      case Some(vf) => vf.map(f => messages(s"other-items.${f.key}")).mkString("<br>")
      case None => throw new InternalServerException("[SummaryListBuilder][otherItemsSummaryListRow] - Other items data not found")
    }

    summaryListRow(filterList, routes.OtherItemsController.show(editMode = true).url, "other-items")
  }

  private def accountingPeriodSummaryListRow(userAnswers: UserAnswers)(implicit messages: Messages): SummaryListRow = {
    val filterList: String = userAnswers.get(AccountingPeriodPage) match {
      case Some(accountingPeriod) => messages(s"accounting-period.${accountingPeriod.key}")
      case None => throw new InternalServerException("[SummaryListBuilder][accountingPeriodSummaryListRow] - Accounting period data not found")
    }

    summaryListRow(filterList, routes.AccountingPeriodController.show(editMode = true).url, "accounting-period")
  }

  private def summaryListRow(value: String, changeLink: String, messageKey: String)
                            (implicit messages: Messages): SummaryListRow = {
    SummaryListRow(
      key = Key(
        content = Text(messages(s"check-your-answers.$messageKey")),
        classes = "govuk-!-static-padding-right-1"
      ),
      value = Value(
        content = HtmlContent(value),
        classes = "govuk-!-static-padding-right-1"
      ),
      actions = Some(Actions(
        items = Seq(
          ActionItem(
            href = changeLink,
            content = Text(messages("base.change")),
            visuallyHiddenText = Some(s"${messages(s"check-your-answers.$messageKey")}")
          )
        )
      ))
    )
  }

}
