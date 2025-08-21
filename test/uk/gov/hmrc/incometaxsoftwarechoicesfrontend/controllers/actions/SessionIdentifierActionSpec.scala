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

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.OK
import play.api.mvc.{BodyParsers, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout, status}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.http.SessionKeys.sessionId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionIdentifierActionSpec extends PlaySpec with GuiceOneAppPerSuite {

  val sessionIdentifierAction = new SessionIdentifierAction(
    app.injector.instanceOf[BodyParsers.Default]
  )

  val testSessionId: String = "test-session-id"

  "SessionIdentifierAction" when {
    "there is no session id available" must {
      "throw an InternalServerException" in {
        intercept[InternalServerException](
          await(sessionIdentifierAction { _ =>
            Results.Ok
          }(FakeRequest()))
        ).message mustBe "[SessionIdentifierAction] - Session id could not be retrieved"
      }
    }
    "there is a session id available" must {
      "continue the request with session id available" in {
        val result: Future[Result] = sessionIdentifierAction { request =>
          request.sessionId mustBe testSessionId
          Results.Ok
        }(FakeRequest().withSession(sessionId -> testSessionId))

        status(result) mustBe OK
      }
    }
  }

}
