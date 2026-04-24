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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.OK
import play.api.mvc.Result
import play.api.test.Helpers.{HTML, contentType, defaultAwaitTimeout, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.mocks.{MockRequireUserDataRefiner, MockSessionIdentifierAction}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.NeedAdditionalSoftwareView

import scala.concurrent.Future

class NeedAdditionalSoftwareControllerSpec extends ControllerBaseSpec
  with MockSessionIdentifierAction
  with MockRequireUserDataRefiner {

  val mockNeedAdditionalSoftwareView: NeedAdditionalSoftwareView = mock[NeedAdditionalSoftwareView]

  val controller: NeedAdditionalSoftwareController = new NeedAdditionalSoftwareController(
    view         = mockNeedAdditionalSoftwareView,
    identify     = fakeSessionIdentifierAction,
    requireData  = fakeRequireUserDataRefiner()
  )

  "show" must {
    "return OK and display the need additional software page" in {
      when(mockNeedAdditionalSoftwareView(any(), any())(any(), any()))
        .thenReturn(HtmlFormat.empty)

      val result: Future[Result] = controller.show()(fakeRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some(HTML)
    }
  }
}
