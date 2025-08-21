/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import play.api.http.Status.OK
import play.api.mvc.Result
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.mocks.MockSessionIdentifierAction
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository

import scala.concurrent.Future

class KeepAliveControllerSpec extends ControllerBaseSpec with MockSessionIdentifierAction {

  "keepAlive" must {
    "keeps the session alive and return OK" in new Setup {
      val result: Future[Result] = controller.keepAlive()(fakeRequest)

      status(result) shouldBe OK
      verify(repo, times(1)).keepAlive(any())
    }

    "return OK without keeping the session alive" in new Setup(false) {
      val result: Future[Result] = controller.keepAlive()(fakeRequest)

      status(result) shouldBe OK
      verify(repo, times(1)).keepAlive(any())
    }
  }

  class Setup(success: Boolean = true) {
    val repo: UserFiltersRepository = mock[UserFiltersRepository]

    val controller: KeepAliveController = new KeepAliveController(
      repo,
      fakeSessionIdentifierAction
    )

    when(repo.keepAlive(any())).thenReturn(
      Future.successful(success)
    )
  }
}
