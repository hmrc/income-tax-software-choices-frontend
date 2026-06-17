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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.SessionIdentifierAction
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.TimeoutType
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.TimeoutType.Expired
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SessionExpiredView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SessionExpiredController @Inject()(repo: UserFiltersRepository,
                                         view: SessionExpiredView,
                                         identify: SessionIdentifierAction)
                                        (implicit ec: ExecutionContext,
                                         mcc: MessagesControllerComponents) extends BaseFrontendController {

  def show(timeoutType: String): Action[AnyContent] = identify.async { request =>
    given Request[AnyContent] = request
    val resolvedType = TimeoutType.fromKey(timeoutType).getOrElse(Expired)
    for {
      _ <- repo.delete(request.sessionId)
    } yield {
      Ok(view(
        postAction = routes.SessionExpiredController.submit(),
        timeoutType = resolvedType
      ))
    }
  }

  def submit(): Action[AnyContent] = Action { _ =>
    Redirect(routes.IndexController.index)
  }
}
