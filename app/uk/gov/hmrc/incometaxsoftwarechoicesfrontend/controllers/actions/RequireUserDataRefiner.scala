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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions

import play.api.mvc.Results.Redirect
import play.api.mvc._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.requests.{SessionDataRequest, SessionRequest}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RequireUserDataRefiner @Inject()(userFiltersRepository: UserFiltersRepository)
                                      (implicit val executionContext: ExecutionContext)
  extends ActionRefiner[SessionRequest, SessionDataRequest] {

  override protected def refine[A](request: SessionRequest[A]): Future[Either[Result, SessionDataRequest[A]]] = {
    userFiltersRepository.get(request.sessionId) map {
      case Some(userFilters) => Right(SessionDataRequest(request, request.sessionId, userFilters))
      case None => Left(Redirect(routes.IndexController.index))
    }
  }

}


