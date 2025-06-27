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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.OtherItemsForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.OtherItemsPage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.PageAnswersService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.OtherItemsPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherItemsController @Inject()(view: OtherItemsPage,
                                     pageAnswersService: PageAnswersService)
                                    (implicit val ec: ExecutionContext,
                                         mcc: MessagesControllerComponents) extends BaseFrontendController(mcc) {


  def show(editMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    val sessionId = request.session.get("sessionId").getOrElse("")
    for (
      pageAnswers <- pageAnswersService.getPageAnswers(sessionId, OtherItemsPage)
    ) yield {
      Ok(view(
        otherItemsForm = OtherItemsForm.form.fill(pageAnswers),
        postAction = routes.OtherItemsController.submit(editMode),
        backLink = backUrl(editMode)
      ))
    }
  }

  def submit(editMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    OtherItemsForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(
          BadRequest(view(
            otherItemsForm = formWithErrors,
            postAction = routes.OtherItemsController.submit(editMode),
            backLink = backUrl(editMode)
          ))
        )
      },
      answers => {
        val sessionId = request.session.get("sessionId").getOrElse("")
        pageAnswersService.setPageAnswers(sessionId, OtherItemsPage, answers).flatMap {
          case true =>
            if (editMode) Future.successful(Redirect(routes.CheckYourAnswersController.show()))
            else Future.successful(Redirect(routes.AccountingPeriodController.show(editMode)))
          case false => throw new InternalServerException("[OtherItemsController][submit] - Could not save other items income sources")
        }
      }
    )
  }

  def backUrl(editMode: Boolean): String = {
    if (editMode) routes.CheckYourAnswersController.show().url
    else routes.AdditionalIncomeSourcesController.show().url
  }

}
