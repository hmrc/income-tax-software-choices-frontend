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
import play.api.mvc.Result
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.RequireUserDataRefiner
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserFilters
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.requests.{SessionDataRequest, SessionRequest}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockRequireUserDataRefiner extends MockitoSugar {

  val fakeRequireUserDataRefiner: RequireUserDataRefiner = new RequireUserDataRefiner(mock[UserFiltersRepository]) {
    override def refine[A](request: SessionRequest[A]): Future[Either[Result, SessionDataRequest[A]]] = {
      Future.successful(Right(SessionDataRequest(
        request = request,
        sessionId = request.sessionId,
        userFilters = UserFilters(
          id = request.sessionId
        )
      )))
    }
  }

}
