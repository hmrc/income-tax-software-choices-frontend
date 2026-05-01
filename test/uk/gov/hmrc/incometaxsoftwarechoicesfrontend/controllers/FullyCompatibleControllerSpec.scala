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

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.mvc.Result
import play.api.test.Helpers.{HTML, contentType, defaultAwaitTimeout, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.mocks.{MockRequireUserDataRefiner, MockSessionIdentifierAction}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareProduct
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.Recognised
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.FullyCompatibleView

import scala.concurrent.Future

class FullyCompatibleControllerSpec extends ControllerBaseSpec
  with MockSessionIdentifierAction
  with MockRequireUserDataRefiner {

  val mockFullyCompatibleView: FullyCompatibleView = mock[FullyCompatibleView]

  def controller(product: Option[SoftwareProduct]): FullyCompatibleController = new FullyCompatibleController(
    view        = mockFullyCompatibleView,
    identify    = fakeSessionIdentifierAction,
    requireData = fakeRequireUserDataRefiner(product = product)
  )

  "show" must {
    "return OK and display the fully compatible page" in {
      when(mockFullyCompatibleView(any(), any(), any())(any(), any()))
        .thenReturn(HtmlFormat.empty)
      val testProduct = SoftwareProduct(1234, "test-software", Recognised)

      val result: Future[Result] = controller(Some(testProduct)).show()(fakeRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some(HTML)
    }

    "return INTERNAL_SERVER_ERROR when the software product is missing" in {
      when(mockFullyCompatibleView(any(), any(), any())(any(), any()))
        .thenReturn(HtmlFormat.empty)

      val result: Future[Result] = controller(None).show()(fakeRequest)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
    
  }
}
