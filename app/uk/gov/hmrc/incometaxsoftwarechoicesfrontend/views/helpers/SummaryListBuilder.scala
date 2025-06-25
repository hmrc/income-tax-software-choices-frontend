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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.PageAnswersService

import scala.concurrent.{ExecutionContext, Future}

trait SummaryListBuilder {
  def buildSummaryList(sessionId: String)
                              (implicit messages: Messages, ec: ExecutionContext, pageAnswersService: PageAnswersService): Future[SummaryList] = {
    Future.sequence(Seq(
      businessIncomeSummaryListRow(sessionId),
      otherIncomeSummaryListRow(sessionId),
      otherItemsSummaryListRow(sessionId),
      accountingPeriodSummaryListRow(sessionId)
    )).map(SummaryList(_))
  }

  private def businessIncomeSummaryListRow(sessionId: String)
                                          (implicit messages: Messages, ec: ExecutionContext, pageAnswersService: PageAnswersService): Future[SummaryListRow] = {
    val filterList = pageAnswersService.getPageAnswers(sessionId, BusinessIncomePage).map(_ match {
      case Some(vf) => vf.map(f => messages(s"check-your-answers.${f.key}")).mkString("<br>")
      case None => ""
    })

    filterList.map(filterList => summaryListRow(filterList, routes.BusinessIncomeController.show(editMode = true).url, "business-income"))
  }

  private def otherIncomeSummaryListRow(sessionId: String)
                                       (implicit messages: Messages, ec: ExecutionContext, pageAnswersService: PageAnswersService): Future[SummaryListRow] = {
    val filterList = pageAnswersService.getPageAnswers(sessionId, AdditionalIncomeSourcesPage).map(_ match {
      case Some(vf) if vf.isEmpty => messages(s"check-your-answers.none-selected")
      case Some(vf) => vf.map(f => messages(s"check-your-answers.${f.key}")).mkString("<br>")
      case None => ""
    })

    filterList.map(filterList => summaryListRow(filterList, routes.AdditionalIncomeSourcesController.show(editMode = true).url, "additional-income"))
  }

  private def otherItemsSummaryListRow(sessionId: String)
                                      (implicit messages: Messages, ec: ExecutionContext, pageAnswersService: PageAnswersService): Future[SummaryListRow] = {
    val filterList = pageAnswersService.getPageAnswers(sessionId, OtherItemsPage).map(_ match {
      case Some(vf) if vf.isEmpty => messages(s"check-your-answers.none-selected")
      case Some(vf) => vf.map(f => messages(s"check-your-answers.${f.key}")).mkString("<br>")
      case None => ""
    })

    filterList.map(filterList => summaryListRow(filterList, routes.OtherItemsController.show(editMode = true).url, "other-items"))
  }

  private def accountingPeriodSummaryListRow(sessionId: String)
                                            (implicit messages: Messages, ec: ExecutionContext, pageAnswersService: PageAnswersService): Future[SummaryListRow] = {
    val filterList = pageAnswersService.getPageAnswers("???", OtherItemsPage).map(_ match {
      case Some(vf) => vf.map(f => messages(s"check-your-answers.${f.key}")).mkString("<br>")
      case None => "NONE"
    })

    filterList.map(filterList => summaryListRow(filterList, routes.OtherItemsController.show(editMode = true).url, "accounting-period"))
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
            visuallyHiddenText = Some(s"${messages("base.change")} ${messages(s"check-your-answers.$messageKey")}")
          )
        )
      ))
    )
  }

}
