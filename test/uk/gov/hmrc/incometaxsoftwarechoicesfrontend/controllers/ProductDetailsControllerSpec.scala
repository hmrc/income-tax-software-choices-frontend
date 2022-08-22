/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.http.Status
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ProductDetailsPage

class ProductDetailsControllerSpec extends ControllerBaseSpec {

  private val mcc = app.injector.instanceOf[MessagesControllerComponents]
  private val productDetailsPage = app.injector.instanceOf[ProductDetailsPage]

  "Show" should {
    "return OK status with the product details page" in withController { controller =>
      val result = controller.show(None)(fakeRequest)

      status(result) shouldBe Status.OK
    }
  }

  private def withController(testCode: ProductDetailsController => Any): Unit = {
    val controller = new ProductDetailsController(
      mcc,
      productDetailsPage
    )

    testCode(controller)
  }


}
