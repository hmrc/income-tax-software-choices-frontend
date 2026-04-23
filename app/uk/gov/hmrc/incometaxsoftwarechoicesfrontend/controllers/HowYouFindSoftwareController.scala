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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.SessionIdentifierAction
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.HowYouFindSoftwareForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.{Check, Find}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.HowYouFindSoftwarePage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.PageAnswersService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.HowYouFindSoftwareView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HowYouFindSoftwareController @Inject()(view: HowYouFindSoftwareView,
                                             pageAnswersService: PageAnswersService,
                                             appConfig: AppConfig,
                                             identify: SessionIdentifierAction)
                                            (implicit ec: ExecutionContext,
                                             mcc: MessagesControllerComponents) extends BaseFrontendController {


  def show(): Action[AnyContent] = identify.async { request =>
    given Request[AnyContent] = request

    pageAnswersService.getPageAnswers(request.sessionId, HowYouFindSoftwarePage).map { answer =>
      Ok(view(
        howYouFindSoftwareForm = HowYouFindSoftwareForm.form.fill(answer),
        postAction = routes.HowYouFindSoftwareController.submit(),
        backUrl = appConfig.guidance
      ))
    }
  }

  def submit(): Action[AnyContent] = identify.async { request =>
    given Request[AnyContent] = request

    HowYouFindSoftwareForm.form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(
          howYouFindSoftwareForm = formWithErrors,
          postAction = routes.HowYouFindSoftwareController.submit(),
          backUrl = appConfig.guidance
        ))),
      journeyType =>
        pageAnswersService.setPageAnswers(request.sessionId, HowYouFindSoftwarePage, journeyType).map {
          case true => redirect(journeyType)
          case false => InternalServerError("[HowYouFindSoftwareController][submit] - Could not save journey type]")
        }
    )
  }
  
  private def redirect(journeyType: JourneyType) = journeyType match {
    case Find => Redirect(routes.UserTypeController.show())
    case Check => Redirect(routes.EnterSoftwareNameController.show())
  }
}
