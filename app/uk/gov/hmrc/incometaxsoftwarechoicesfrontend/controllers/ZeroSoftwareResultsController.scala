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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.PageAnswersService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ZeroSoftwareResultsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ZeroSoftwareResultsController @Inject()(view: ZeroSoftwareResultsView,
                                              pageAnswersService: PageAnswersService)
                                             (implicit val ec: ExecutionContext,
                                         mcc: MessagesControllerComponents) extends BaseFrontendController(mcc) {


  def show(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(view(
      postAction = routes.ZeroSoftwareResultsController.submit(),
      backLink = routes.CheckYourAnswersController.show()
    )))
  }

  def submit(): Action[AnyContent] = Action.async { implicit request =>
    val sessionId = request.session.get("sessionId").getOrElse("")
    pageAnswersService.removePageFilters(sessionId).map {
      case true => Redirect(routes.SearchSoftwareController.show(zeroResults = true))
      case false => throw new InternalServerException("[ZeroSoftwareResultsController][submit] - Could not remove user filters")
    }
  }

}
