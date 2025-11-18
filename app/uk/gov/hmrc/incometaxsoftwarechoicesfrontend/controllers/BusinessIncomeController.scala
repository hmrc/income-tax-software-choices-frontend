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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.{RequireUserDataRefiner, SessionIdentifierAction}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.BusinessIncomeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.BusinessIncomePage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.PageAnswersService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.BusinessIncomeView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessIncomeController @Inject()(view: BusinessIncomeView,
                                         pageAnswersService: PageAnswersService,
                                         identify: SessionIdentifierAction,
                                         requireData: RequireUserDataRefiner)
                                        (implicit ec: ExecutionContext,
                                         mcc: MessagesControllerComponents) extends BaseFrontendController {


  def show(editMode: Boolean): Action[AnyContent] = (identify andThen requireData) { request =>
    given Request[AnyContent] = request

    val pageAnswers = pageAnswersService.getPageAnswers(request.userFilters.answers, BusinessIncomePage)
    Ok(view(
      businessIncomeForm = BusinessIncomeForm.form.fill(pageAnswers),
      postAction = routes.BusinessIncomeController.submit(editMode),
      backUrl = backUrl(editMode)
    ))
  }

  def submit(editMode: Boolean): Action[AnyContent] = (identify andThen requireData).async { request =>
    given Request[AnyContent] = request
    BusinessIncomeForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(
          BadRequest(view(
            businessIncomeForm = formWithErrors,
            postAction = routes.BusinessIncomeController.submit(editMode),
            backUrl = backUrl(editMode)
          ))
        )
      },
      answers => {
        pageAnswersService.setPageAnswers(request.userFilters, BusinessIncomePage, answers).flatMap {
          case true =>
            if (editMode) Future.successful(Redirect(routes.CheckYourAnswersController.show()))
            else Future.successful(Redirect(routes.AdditionalIncomeSourcesController.show()))
          case false => throw new InternalServerException("[BusinessIncomeController][submit] - Could not save business income sources")
        }
      }
    )
  }

  def backUrl(editMode: Boolean): String = {
    if (editMode) routes.CheckYourAnswersController.show().url else routes.UserTypeController.show().url
  }

}
