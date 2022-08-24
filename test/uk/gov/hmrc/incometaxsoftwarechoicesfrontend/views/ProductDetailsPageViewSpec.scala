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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareVendorModel
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{FreeTrial, FreeVersion}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ProductDetailsPage

class ProductDetailsPageViewSpec extends ViewSpec {
  private val productDetailsPage = app.injector.instanceOf[ProductDetailsPage]
  private val testSoftwareVendor = SoftwareVendorModel(
    "test software vendor three",
    "/test-url-three",
    "test@software-vendor-name-three.com",
    "00000 000 000",
    "software-vendor-name-three.com",
    Seq(FreeTrial, FreeVersion)
  )

  "Product Details Page" should {

    "display the vendor contact details heading" in {
      document.selectHead("h2.govuk-heading-l").text() shouldBe s"${ProductDetailsPage.contactDetailsHeading}"
    }

    "display the vendor email address" in {
      document.selectNth(".contact-details .govuk-summary-list__row", 1).text() shouldBe
        s"${ProductDetailsPage.contactDetailsEmail}: ${testSoftwareVendor.email}"
    }

    "display the vendor phone number" in {
      document.selectNth(".contact-details .govuk-summary-list__row", 2).text() shouldBe
        s"${ProductDetailsPage.contactDetailsPhone}: ${testSoftwareVendor.phone}"
    }

    "display the vendor website" in {
      document.selectNth(".contact-details .govuk-summary-list__row", 3).text() shouldBe
        s"${ProductDetailsPage.contactDetailsWebsite}: ${testSoftwareVendor.website} (opens in a new tab)"
    }

  }

  private object ProductDetailsPage {
    val contactDetailsHeading = "Find out more or get help and support"
    val contactDetailsEmail = "Email"
    val contactDetailsPhone = "Call"
    val contactDetailsWebsite = "Visit website"
  }

  private def document: Document =
    Jsoup.parse(
      productDetailsPage(
        testSoftwareVendor
      ).body
    )
}
