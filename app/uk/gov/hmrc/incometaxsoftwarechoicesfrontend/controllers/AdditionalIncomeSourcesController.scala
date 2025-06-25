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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.AdditionalIncomeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.AdditionalIncomeSourcesPage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.PageAnswersService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.AdditionalIncomeSourcePage

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdditionalIncomeSourcesController @Inject()(view: AdditionalIncomeSourcePage,
                                                  pageAnswersService: PageAnswersService
                                                 )(implicit ec: ExecutionContext,
                                                   mcc: MessagesControllerComponents) extends BaseFrontendController(mcc) {

  def show(editMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    val sessionId = request.session.get("sessionId").getOrElse("")
    pageAnswersService.getPageAnswers(sessionId, AdditionalIncomeSourcesPage)
      .map { maybeAnswers =>
        Ok(view(
          AdditionalIncomeForm.form.fill(maybeAnswers),
          postAction = routes.AdditionalIncomeSourcesController.submit(editMode),
          backUrl    = backUrl(editMode)
        ))
      }
  }

  def submit(editMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    AdditionalIncomeForm.form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(
          BadRequest(view(
            additionalIncomeForm = formWithErrors,
            postAction = routes.AdditionalIncomeSourcesController.submit(editMode),
            backUrl    = backUrl(editMode)
          ))
        ),
      answers => {
        val sessionId = request.session.get("sessionId").getOrElse("")
        pageAnswersService.setPageAnswers(sessionId, AdditionalIncomeSourcesPage, answers).flatMap {
            case true  =>
              if (editMode) Future.successful(Redirect(routes.CheckYourAnswersController.show))
              else Future.successful(Redirect(routes.OtherItemsController.show()))
            case false => Future.failed(new InternalServerException("[AdditionalIncomeSourcesController][submit] – could not save additional income sources"))
          }
      }
    )
  }

  def backUrl(editMode: Boolean): String = {
    if (editMode) routes.CheckYourAnswersController.show.url
    else routes.BusinessIncomeController.show().url
  }
}
