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

import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.BetaFeatures
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ProductDetailsPage

class ProductDetailsControllerSpec extends ControllerBaseSpec with FeatureSwitching with BeforeAndAfterEach {

  private val mcc = app.injector.instanceOf[MessagesControllerComponents]
  val appConfig = app.injector.instanceOf[AppConfig]
  private val productDetailsPage = app.injector.instanceOf[ProductDetailsPage]

  protected override def beforeEach(): Unit = {
    disable(BetaFeatures)
  }

  "Show" when {
    "beta features are enabled" should {
      "return OK status with the product details page" in withController { controller =>
        enable(BetaFeatures)
        val result = controller.show(None)(fakeRequest)

        status(result) shouldBe Status.OK
      }
    }
    "beta features are not enabled" should {
      "throw not found exception" in withController { controller =>
        intercept[NotFoundException](await(controller.show(None)(fakeRequest))).message should be ("[ProductDetailsController][show] - Beta features is not enabled")
      }
    }
  }

  private def withController(testCode: ProductDetailsController => Any): Unit = {
    val controller = new ProductDetailsController(
      mcc,
      appConfig,
      productDetailsPage
    )

    testCode(controller)
  }

}