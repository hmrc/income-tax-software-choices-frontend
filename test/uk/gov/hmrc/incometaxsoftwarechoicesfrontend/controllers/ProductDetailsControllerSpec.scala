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

import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.SoftwareChoicesService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ProductDetailsView

import java.net.URLEncoder

class ProductDetailsControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val productDetailsPage = app.injector.instanceOf[ProductDetailsView]
  private val softwareChoicesService = app.injector.instanceOf[SoftwareChoicesService]

  "Show" when {
    "a valid param has been passed" should {
      "return OK status with the product details page" in withController { controller =>
        val result = controller.show(URLEncoder.encode("test software vendor name one", "UTF-8"), zeroResults = false)(fakeRequest)

        status(result) shouldBe Status.OK
      }
    }
    "an invalid param has been passed" should {
      "return OK status with the product details page" in withController { controller =>
        intercept[NotFoundException](await(controller.show("dummy", zeroResults = false)(fakeRequest))).message should be(ProductDetailsController.NotFound)
      }
    }
  }

  private def withController(testCode: ProductDetailsController => Any): Unit = {
    val controller = new ProductDetailsController(
      softwareChoicesService,
      productDetailsPage
    )

    testCode(controller)
  }

}
