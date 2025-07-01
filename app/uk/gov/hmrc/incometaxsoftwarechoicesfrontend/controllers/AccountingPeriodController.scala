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

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.AccountingPeriodForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.AccountingPeriodForm.accountingPeriodForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.AccountingPeriodPage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.PageAnswersService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.AccountingPeriodPage

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountingPeriodController @Inject()(view: AccountingPeriodPage,
                                           pageAnswersService: PageAnswersService)
                                          (implicit val ec: ExecutionContext,
                                           mcc: MessagesControllerComponents) extends BaseFrontendController(mcc) with I18nSupport {

  def show(editMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    val sessionId = request.session.get("sessionId").getOrElse("")
    for (
      pageAnswers <- pageAnswersService.getPageAnswers(sessionId, AccountingPeriodPage)
    ) yield {
      Ok(view(
        accountingPeriodForm = AccountingPeriodForm.accountingPeriodForm.fill(pageAnswers),
        postAction = routes.AccountingPeriodController.submit(editMode),
        backUrl = backUrl(editMode)
      ))
    }
  }

  def submit(editMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    accountingPeriodForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(
          BadRequest(view(
            accountingPeriodForm = formWithErrors,
            postAction = routes.AccountingPeriodController.submit(editMode),
            backUrl = backUrl(editMode)
          ))
        )
      },
      selectedPeriod => {
        val sessionId = request.session.get("sessionId").getOrElse("")
        pageAnswersService.setPageAnswers(sessionId, AccountingPeriodPage, selectedPeriod).flatMap {
          case true =>
            selectedPeriod match {
              case SixthAprilToFifthApril => Future.successful(Redirect(routes.CheckYourAnswersController.show()))
              case FirstAprilToThirtyFirstMarch => Future.successful(Redirect(routes.CheckYourAnswersController.show()))
              case OtherAccountingPeriod => Future.successful(Redirect(routes.UnsupportedAccountingPeriodController.show))
            }
          case false => throw new InternalServerException("[AccountingPeriodController][submit] - Could not save accounting period")
        }
      }
    )
  }

  def backUrl(editMode: Boolean): String = {
    if (editMode) routes.CheckYourAnswersController.show().url
    else routes.OtherItemsController.show().url
  }
}
