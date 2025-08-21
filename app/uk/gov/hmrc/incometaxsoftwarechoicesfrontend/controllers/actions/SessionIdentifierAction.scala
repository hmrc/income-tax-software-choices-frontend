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

import play.api.mvc._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.requests.SessionRequest
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionIdentifierAction @Inject()(val parser: BodyParsers.Default)
                                       (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[SessionRequest, AnyContent]
    with ActionFunction[Request, SessionRequest] {

  override def invokeBlock[A](request: Request[A], block: SessionRequest[A] => Future[Result]): Future[Result] = {
    block(SessionRequest(
      request = request,
      sessionId = HeaderCarrierConverter.fromRequestAndSession(request, request.session).sessionId.getOrElse(
        throw new InternalServerException("[SessionIdentifierAction] - Session id could not be retrieved")
      ).value
    ))
  }

}
