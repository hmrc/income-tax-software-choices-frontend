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
import org.jsoup.nodes.{Document, Element}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ProductDetailsPage

class ProductDetailsViewSpec extends ViewSpec {

  private val productDetailsPage = app.injector.instanceOf[ProductDetailsPage]

  object ProductDetailsPage {
    val title: String = "Choose the right software for your needs"

    val contactDetailsHeading = "Find out more or get help and support"
    val contactDetailsEmail = "Email"
    val contactDetailsPhone = "Call"
    val contactDetailsWebsite = "Visit website"

    val productDetailsHeading: String = "Product details"

    val pricing: String = "Pricing"
    val freeTrail: String = "Free trial"
    val freeVersion: String = "Free version"

    val incomeType: String = "Income type"
    val soleTrader: String = "Sole trader"
    val ukProperty: String = "UK property"
    val overseasProperty: String = "Overseas property"

    val compatibleWith: String = "Compatible with"
    val microsoftWindows: String = "Microsoft Windows"
    val macOS: String = "Mac OS"

    val mobileApp: String = "Mobile app"
    val android: String = "Android"
    val iOS: String = "iOS"

    val softwareType: String = "Software type"
    val browserBased: String = "Browser based"
    val applicationBased: String = "Application based"

    val businessType: String = "Business type"
    val individual: String = "Individual"
    val agent: String = "Agent"

    val softwareCompatibility: String = "Software compatibility"
    val incomeTax: String = "Income tax"
    val vat: String = "VAT"

    val accessibility: String = "Accessibility"
    val visual: String = "Visual"
    val hearing: String = "Hearing"
    val motor: String = "Motor"
    val cognitive: String = "Cognitive"

    val accessibilityStatement: String = "Accessibility Statement"
  }

  val softwareVendorModelFull: SoftwareVendorModel = SoftwareVendorModel(
    name = "abc",
    url = "/url",
    email = "test@software-vendor-name-three.com",
    phone = "00000 000 000",
    website = "software-vendor-name-three.com",
    filters = Seq(
      VendorFilter.FreeTrial,
      VendorFilter.FreeVersion,
      VendorFilter.SoleTrader,
      VendorFilter.UkProperty,
      VendorFilter.OverseasProperty,
      VendorFilter.Individual,
      VendorFilter.Agent,
      VendorFilter.MicrosoftWindows,
      VendorFilter.MacOS,
      VendorFilter.Android,
      VendorFilter.AppleIOS,
      VendorFilter.BrowserBased,
      VendorFilter.ApplicationBased,
      VendorFilter.Visual,
      VendorFilter.Hearing,
      VendorFilter.Motor,
      VendorFilter.Cognitive,
      VendorFilter.RecordKeeping,
      VendorFilter.Bridging,
      VendorFilter.Vat
    )
  )

  "ProductDetailsPage" must {
    "have a breadcrumb menu" which {
      "contains the guidance page" in {
        val link = document().selectNth(".govuk-breadcrumbs__list-item", 1).selectHead("a")
        link.text shouldBe "Guidance"
        link.attr("href") shouldBe appConfig.guidance
      }

      "contains the filter page" in {
        val link = document().selectNth(".govuk-breadcrumbs__list-item", 2).selectHead("a")
        link.text shouldBe "Filter"
        link.attr("href") shouldBe routes.SearchSoftwareController.show.url
      }

      "contains the current page" in {
        document().selectNth(".govuk-breadcrumbs__list-item", 3).text shouldBe "Details"
      }
    }

    "have a title" in {
      document().title shouldBe s"""${ProductDetailsPage.title} - Find software for Making Tax Digital for Income Tax - GOV.UK"""
    }

    "display the vendor contact details heading" in {
      document().selectNth("h2", 1).text() shouldBe s"${ProductDetailsPage.contactDetailsHeading}"
    }

    "display the vendor email address" in {
      val row: Element = document().selectNth("dl", 1).selectNth(".govuk-summary-list__row", 1)
      row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsEmail}:"

      val link = row.selectHead("dd").selectHead("a")
      link.text shouldBe softwareVendorModelFull.email
      link.attr("href") shouldBe s"mailto:${softwareVendorModelFull.email}"
    }

    "display the vendor phone number" in {
      val row: Element = document().selectNth("dl", 1).selectNth(".govuk-summary-list__row", 2)
      row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsPhone}:"
      row.selectHead("dd").text shouldBe softwareVendorModelFull.phone
    }

    "display the vendor website" in {
      val row: Element = document().selectNth("dl", 1).selectNth(".govuk-summary-list__row", 3)
      row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsWebsite}:"

      val link = row.selectHead("dd").selectHead("a")
      link.text shouldBe softwareVendorModelFull.website
      link.attr("href") shouldBe softwareVendorModelFull.website
    }

    "have a product details heading" in {
      document().selectNth("h2", 2).text shouldBe ProductDetailsPage.productDetailsHeading
    }

  }

  "Product details page" when {
    "the software vendor has full product details" should {
      "display the pricing row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 1)
        row.selectHead("dt").text shouldBe ProductDetailsPage.pricing

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.freeTrail
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.freeVersion
      }
      "display the income type row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 2)
        row.selectHead("dt").text shouldBe ProductDetailsPage.incomeType

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.soleTrader
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.ukProperty
        detail.selectNth("p", 3).text shouldBe ProductDetailsPage.overseasProperty
      }
      "display the compatible with row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 3)
        row.selectHead("dt").text shouldBe ProductDetailsPage.compatibleWith

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.microsoftWindows
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.macOS
      }
      "display the mobile app row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 4)
        row.selectHead("dt").text shouldBe ProductDetailsPage.mobileApp

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.android
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.iOS
      }
      "display the software type row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 5)
        row.selectHead("dt").text shouldBe ProductDetailsPage.softwareType

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.browserBased
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.applicationBased
      }
      "display the business type row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 6)
        row.selectHead("dt").text shouldBe ProductDetailsPage.businessType

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.individual
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.agent
      }
      "display the software compatibility row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 7)
        row.selectHead("dt").text shouldBe ProductDetailsPage.softwareCompatibility

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.incomeTax
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.vat
      }
      "display the accessibility row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 8)
        row.selectHead("dt").text shouldBe ProductDetailsPage.accessibility

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.visual
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.hearing
        detail.selectNth("p", 3).text shouldBe ProductDetailsPage.motor
        detail.selectNth("p", 4).text shouldBe ProductDetailsPage.cognitive
      }
    }
    "the software vendor has minimal product details" should {
      "display the software compatibility row" in {
        val row: Element = document(softwareVendorModelFull.copy(filters = Seq.empty[VendorFilter]))
          .selectNth("dl", 2)
          .selectNth("div", 1)

        row.selectHead("dt").text shouldBe ProductDetailsPage.softwareCompatibility
        row.selectHead("dd").text shouldBe ProductDetailsPage.incomeTax
      }
      "display no other rows" in {
        document(softwareVendorModelFull.copy(filters = Seq.empty[VendorFilter]))
          .selectNth("dl", 2)
          .selectOptionally("div:nth-of-type(2)") shouldBe None
      }
    }
    "the software vendor has an accessibility statement" should {
      "display the link to the statement" in {
        val linkText = "accessibility statement"
        val row: Element = document(softwareVendorModelFull.copy(accessibilityStatementLink = Some(linkText)))
          .selectNth("dl", 3)
        row.selectHead("dt").text shouldBe ProductDetailsPage.accessibilityStatement
        row.selectHead("dd").text shouldBe linkText
        row.selectHead("dd").selectHead("a").getElementsByAttribute("href").text() shouldBe linkText
      }
    }
  }

  private def page(vendorModel: SoftwareVendorModel) = productDetailsPage(
    vendorModel
  )

  private def document(vendorModel: SoftwareVendorModel = softwareVendorModelFull): Document = Jsoup.parse(page(vendorModel).body)

}
