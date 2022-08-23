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
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.BetaFeatures
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.ComponentSpecBase

class ProductDetailsControllerISpec extends ComponentSpecBase with BeforeAndAfterEach {

  protected override def beforeEach(): Unit = {
    disable(BetaFeatures)
  }
  val address = "/product-details"

  s"GET /making-tax-digital-income-tax-software${address}" when {
    "feature switch is off" should {
      "respond with 200 status" in {
        When(s"GET ${address} is called")
        val response = SoftwareChoicesFrontend.productDetails()

        Then("Should return OK with the software search page")
        response should have(
          httpStatus(NOT_FOUND)
        )
      }
    }

    "feature switch is on" should {
      "respond with 200 status" in {
        When(s"GET ${address} is called")
        enable(BetaFeatures)
        val response = SoftwareChoicesFrontend.productDetails()

        Then("Should return OK with the software search page")
        response should have(
          httpStatus(OK)
        )
      }
    }
  }

}
