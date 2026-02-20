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
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.ComponentSpecBase

class ProductDetailsControllerISpec extends ComponentSpecBase with BeforeAndAfterEach {

  val address = "/product-details"

  s"GET /find-making-tax-digital-income-tax-software${address}" when {

    "respond with 200 status for a real software name" in {
      When(s"GET $address is called")
      val response = SoftwareChoicesFrontend.productDetails("101")

      Then("Should return OK with the software search page")
      response should have(
        httpStatus(OK)
      )
    }
    "respond with 404 status for a non existent product id" in {
      When(s"GET $address is called")
      val response = SoftwareChoicesFrontend.productDetails("0")

      Then("Should return Not Found")
      response should have(
        httpStatus(NOT_FOUND)
      )
    }
  }

}
