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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers

import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.PageAnswersService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.CheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(view: CheckYourAnswersView,
                                           pageAnswersService: PageAnswersService)
                                          (implicit val ec: ExecutionContext,
                                           mcc: MessagesControllerComponents) extends BaseFrontendController(mcc) {

  def show: Action[AnyContent] = Action.async { implicit request =>
    val sessionId = request.session.get("sessionId").getOrElse("")
    for (
      summaryList <- buildSummaryList(sessionId)
    ) yield {
      Ok(view(
        summaryList = summaryList,
        postAction = routes.CheckYourAnswersController.submit,
        backLink = routes.CheckYourAnswersController.show
      ))
    }
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
      val sessionId = request.session.get("sessionId").getOrElse("")
      Future.successful(Redirect(routes.CheckYourAnswersController.show))
  }

  private def buildSummaryList(sessionId: String)(implicit messages: Messages): Future[SummaryList] = {
    Future.sequence(Seq(
      businessIncomeSummaryListRow(sessionId),
      otherIncomeSummaryListRow(sessionId),
      otherItemsSummaryListRow(sessionId),
      accountingPeriodSummaryListRow(sessionId)
    )).map(SummaryList(_))
  }

  private def businessIncomeSummaryListRow(sessionId: String)(implicit messages: Messages): Future[SummaryListRow] = {
    val filterList = pageAnswersService.getPageAnswers(sessionId, BusinessIncomePage).map(_ match {
      case Some(vf) => vf.map(f => messages(s"check-your-answers.${f.key}")).mkString("<br>")
      case None => ""
    })

    filterList.map(filterList => summaryListRow(filterList, routes.BusinessIncomeController.show(editMode = true).url, "business-income"))
  }

  private def otherIncomeSummaryListRow(sessionId: String)(implicit messages: Messages): Future[SummaryListRow] = {
    val filterList = pageAnswersService.getPageAnswers(sessionId, AdditionalIncomeSourcesPage).map(_ match {
      case Some(vf) if vf.isEmpty => messages(s"check-your-answers.none-selected")
      case Some(vf) => vf.map(f => messages(s"check-your-answers.${f.key}")).mkString("<br>")
      case None => ""
    })

    filterList.map(filterList => summaryListRow(filterList, routes.AdditionalIncomeSourcesController.show(editMode = true).url, "additional-income"))
  }

  private def otherItemsSummaryListRow(sessionId: String)(implicit messages: Messages): Future[SummaryListRow] = {
    val filterList = pageAnswersService.getPageAnswers(sessionId, OtherItemsPage).map(_ match {
      case Some(vf) if vf.isEmpty => messages(s"check-your-answers.none-selected")
      case Some(vf) => vf.map(f => messages(s"check-your-answers.${f.key}")).mkString("<br>")
      case None => ""
    })

    filterList.map(filterList => summaryListRow(filterList, routes.OtherItemsController.show(editMode = true).url, "other-items"))
  }

  private def accountingPeriodSummaryListRow(sessionId: String)(implicit messages: Messages): Future[SummaryListRow] = {
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
        content = Text(messages(s"check-your-answers.$messageKey"))
      ),
      value = Value(
        content = HtmlContent(value)
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
