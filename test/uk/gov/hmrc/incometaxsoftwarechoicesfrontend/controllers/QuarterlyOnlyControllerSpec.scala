/*
 * Copyright 2026 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.QuarterlyOnlyView

import scala.concurrent.Future

class QuarterlyOnlyControllerSpec extends ControllerBaseSpec
  with MockSessionIdentifierAction
  with MockRequireUserDataRefiner {

  val mockQuarterlyOnlyView: QuarterlyOnlyView = mock[QuarterlyOnlyView]

  def controller(product: Option[SoftwareProduct]): QuarterlyOnlyController = new QuarterlyOnlyController(
    view        = mockQuarterlyOnlyView,
    identify    = fakeSessionIdentifierAction,
    requireData = fakeRequireUserDataRefiner(product = product)
  )

  "show" must {
    "return OK and display the quarterly only page" in {
      when(mockQuarterlyOnlyView(any(), any(), any(), any())(any(), any()))
        .thenReturn(HtmlFormat.empty)
      val testProduct = SoftwareProduct(1234, "test-software", Recognised)

      val result: Future[Result] = controller(Some(testProduct)).show()(fakeRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some(HTML)
    }

    "return INTERNAL_SERVER_ERROR when the software product is missing" in {
      when(mockQuarterlyOnlyView(any(), any(), any(), any())(any(), any()))
        .thenReturn(HtmlFormat.empty)

      val result: Future[Result] = controller(None).show()(fakeRequest)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
    
  }
}
