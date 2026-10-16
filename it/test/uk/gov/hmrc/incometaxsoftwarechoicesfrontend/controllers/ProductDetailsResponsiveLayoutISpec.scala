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

import com.microsoft.playwright.{Browser, Playwright, Route}
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, IntegrationTestConstants}

import scala.io.Source
import scala.util.Using

class ProductDetailsResponsiveLayoutISpec extends ComponentSpecBase {

  private val applicationStylesheet =
    Using.resource(Source.fromResource("stylesheets/application.css"))(_.mkString)

  "GET /product-details" should {
    "render the status table columns equally below 641px and in thirds from 641px" in {
      val playwright = Playwright.create()
      Using.resource(playwright) { playwright =>
        Using.resource(playwright.chromium().launch()) { browser =>
          Seq(
            640 -> 0.5,
            641 -> (1.0 / 3.0)
          ).foreach { case (viewportWidth, expectedOneThirdColumnRatio) =>
            val page = browser.newPage(new Browser.NewPageOptions().setViewportSize(viewportWidth, 900))
            try {
              page.route(
                "**/assets/stylesheets/application.css",
                (route: Route) => route.fulfill(
                  new Route.FulfillOptions().setContentType("text/css").setBody(applicationStylesheet)
                )
              )
              page.navigate(
                s"http://localhost:$port${IntegrationTestConstants.baseURI}/product-details?productId=101"
              )

              val statusTable = page.locator(".govuk-table").first()
              val tableWidth = statusTable.boundingBox().width
              val firstColumnWidth = statusTable.locator("""[class~="govuk-!-width-one-third-override"]""").first().boundingBox().width
              val secondColumnWidth = statusTable.locator("""[class~="govuk-!-width-two-thirds-override"]""").first().boundingBox().width

              withClue(s"at viewport width $viewportWidth: ") {
                math.abs(firstColumnWidth / tableWidth - expectedOneThirdColumnRatio) should be < 0.02
                math.abs(secondColumnWidth / tableWidth - (1.0 - expectedOneThirdColumnRatio)) should be < 0.02
              }
            } finally {
              page.close()
            }
          }
        }
      }
    }
  }
}
