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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.mocks

import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{BodyParsers, Request, Result}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.SessionIdentifierAction
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.requests.SessionRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockSessionIdentifierAction extends MockitoSugar {

  val sessionId: String = "test-session-id"

  val fakeSessionIdentifierAction: SessionIdentifierAction = new SessionIdentifierAction(mock[BodyParsers.Default]) {
    override def invokeBlock[A](request: Request[A], block: SessionRequest[A] => Future[Result]): Future[Result] = {
      block(SessionRequest(request, sessionId))
    }
  }

}
