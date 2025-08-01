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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.UserTypeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.UserTypeForm.userTypeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.{Agent, SoleTraderOrLandlord}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.UserTypePage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.PageAnswersService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.UserTypePage

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserTypeController @Inject()(view: UserTypePage,
                                   pageAnswersService: PageAnswersService,
                                   appConfig: AppConfig
                                  )(implicit ec: ExecutionContext,
                                    mcc: MessagesControllerComponents) extends BaseFrontendController(mcc) {

  def show(): Action[AnyContent] = Action.async { implicit request =>
    val sessionId = request.session.get("sessionId").getOrElse("")
    for (
      pageAnswers <- pageAnswersService.getPageAnswers(sessionId, UserTypePage)
    ) yield {
      Ok(view(
        userTypeForm = UserTypeForm.userTypeForm.fill(pageAnswers),
        postAction = routes.UserTypeController.submit(),
        backUrl = appConfig.guidance
      ))
    }
  }

  def submit(): Action[AnyContent] = Action.async { implicit request =>
    userTypeForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(
          BadRequest(view(
            userTypeForm = formWithErrors,
            postAction = routes.UserTypeController.submit(),
            backUrl = appConfig.guidance
          ))
        )
      },
      typeOfUser => {
        val sessionId = request.session.get("sessionId").getOrElse("")
        typeOfUser match {
          case SoleTraderOrLandlord =>
            pageAnswersService.setPageAnswers(sessionId, UserTypePage, typeOfUser).flatMap {
              case true => Future.successful(Redirect(routes.BusinessIncomeController.show()))
              case false => throw new InternalServerException("[UserTypeController][submit] - Could not save sole trader or landlord user type")
            }
          case Agent =>
            for {
              resetUserAnswers <- pageAnswersService.resetUserAnswers(sessionId)
              setPageAnswers <- pageAnswersService.setPageAnswers(sessionId, UserTypePage, typeOfUser)
              saveFiltersFromAnswers <- pageAnswersService.saveFiltersFromAnswers(sessionId)
            } yield {
              if (resetUserAnswers && setPageAnswers && saveFiltersFromAnswers.nonEmpty) {
                Redirect(routes.SearchSoftwareController.show())
              } else {
                throw new InternalServerException("[UserTypeController][submit] - Could not save agent user type")
              }
            }
        }
      }
    )
  }

}
