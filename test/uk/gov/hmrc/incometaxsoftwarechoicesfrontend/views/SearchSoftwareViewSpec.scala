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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, SoftwareVendors, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwarePage

class SearchSoftwareViewSpec extends ViewSpec {

  private val searchSoftwarePage = app.injector.instanceOf[SearchSoftwarePage]
  private val lastUpdateTest = "01/07/2022"
  private val softwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = lastUpdateTest,
    vendors = Seq(
      SoftwareVendorModel(
        name = "test software vendor one",
        url = "/test-vendor-one-url",
        filters = Seq(
          VendorFilter.FreeVersion,
          VendorFilter.FreeTrial,
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
          VendorFilter.Cognitive
        )
      ),
      SoftwareVendorModel(
        name = "test software vendor two",
        url = "/test-vendor-two-url",
        filters = Seq.empty[VendorFilter]
      )
    )
  )

  "Search software page" should {
    "have a breadcrumb menu" which {
      "contains to guidance page" in {
        document.selectNth(".govuk-breadcrumbs__list-item", 1).text shouldBe "Guidance"
      }

      "contains the current page" in {
        document.selectNth(".govuk-breadcrumbs__list-item", 2).text shouldBe "Filter"
      }
    }

    "have an alpha banner" in {
      val banner: Element = document.selectHead(".govuk-phase-banner")
      banner.selectHead(".govuk-phase-banner__content__tag").text shouldBe "alpha"

      val bannerContent: Element = banner.selectHead(".govuk-phase-banner__text")
      bannerContent.text shouldBe "This is a new service – your feedback will help us to improve it."

      val feedbackLink: Element = bannerContent.selectHead("a")
      feedbackLink.text shouldBe "feedback"
      feedbackLink.attr("href") shouldBe "/"
    }

    "have a title" in {
      document.title shouldBe s"""${SearchSoftwarePage.title} - Find software for Making Tax Digital for Income Tax - GOV.UK"""
    }

    "have the last updated date" in {
      document.mainContent.selectNth("p", 1).text shouldBe SearchSoftwarePage.lastUpdate
    }

    "have a heading" in {
      document.mainContent.selectHead("h1").text shouldBe SearchSoftwarePage.heading
    }

    "have paragraph1" in {
      document.mainContent.selectNth("p", 2).text shouldBe SearchSoftwarePage.paragraph1
    }

    "have paragraph2" in {
      document.mainContent.selectNth("p", 3).text shouldBe SearchSoftwarePage.paragraph2
    }

    "have inset text" in {
      document.mainContent.selectHead(".govuk-inset-text").text shouldBe SearchSoftwarePage.insetText
    }

    "have a filter section" which {
      def filterSection: Element = document.mainContent.selectHead("#software-section").selectHead(".filters-section")

      "has a heading" in {
        filterSection.selectHead("h2").text shouldBe SearchSoftwarePage.Filters.filterHeading
      }

      "has a pricing section" in {
        filterSection.selectNth("h3", 1).text shouldBe SearchSoftwarePage.Filters.pricing
      }

      "has a business type section" in {
        filterSection.selectNth("h3", 2).text shouldBe SearchSoftwarePage.Filters.businessType
      }

      "has a compatible with section" in {
        filterSection.selectNth("h3", 3).text shouldBe SearchSoftwarePage.Filters.compatibleWith
      }

      "has a mobile app section" in {
        filterSection.selectNth("h3", 4).text shouldBe SearchSoftwarePage.Filters.mobileApp
      }

      "has a software type section" in {
        filterSection.selectNth("h3", 5).text shouldBe SearchSoftwarePage.Filters.softwareType
      }

      "has a software compatibility section" in {
        filterSection.selectNth("h3", 6).text shouldBe SearchSoftwarePage.Filters.softwareCompatibility
      }

      "has an accessibility needs section" in {
        filterSection.selectNth("h3", 7).text shouldBe SearchSoftwarePage.Filters.accessibilityNeeds
      }
    }

    "have a software vendor section" which {
      def softwareVendorsSection: Element = document
        .mainContent
        .selectHead("#software-section")
        .selectHead(".govuk-grid-column-two-thirds")

      "has a count of the number of software vendors on the page" in {
        softwareVendorsSection.selectHead("p").text shouldBe SearchSoftwarePage.numberOfProviders
      }

      "have a list of software vendors" which {

        "have a software vendor with lots of detail" which {
          def firstVendor: Element = softwareVendorsSection.selectHead("#software-vendor-0")

          "has a heading link for the software vendor" in {
            val headingLink: Element = firstVendor.selectHead("h3").selectHead("a")
            headingLink.attr("target") shouldBe "_blank"
            headingLink.attr("rel") shouldBe "noopener noreferrer"
            headingLink.attr("href") shouldBe softwareVendors.vendors.head.url
            headingLink.text shouldBe s"${softwareVendors.vendors.head.name} (opens in a new tab)"
          }

          "has a list of detail for the software vendor with full detail" in {
            val summaryList: Element = firstVendor.selectHead("dl")

            val firstRow: Element = summaryList.selectNth("div", 1)
            firstRow.selectHead("dt").text shouldBe SearchSoftwarePage.pricing
            firstRow.selectHead("dd").text shouldBe s"${SearchSoftwarePage.freeTrial}/${SearchSoftwarePage.freeVersion}"

            val secondRow: Element = summaryList.selectNth("div", 2)
            secondRow.selectHead("dt").text shouldBe SearchSoftwarePage.businessType
            secondRow.selectHead("dd").text shouldBe s"${SearchSoftwarePage.individual}/${SearchSoftwarePage.agent}"

            val thirdRow: Element = summaryList.selectNth("div", 3)
            thirdRow.selectHead("dt").text shouldBe SearchSoftwarePage.compatibleWith
            thirdRow.selectHead("dd").text shouldBe s"${SearchSoftwarePage.microsoftWindows}/${SearchSoftwarePage.macOS}"

            val forthRow: Element = summaryList.selectNth("div", 4)
            forthRow.selectHead("dt").text shouldBe SearchSoftwarePage.mobileApp
            forthRow.selectHead("dd").text shouldBe s"${SearchSoftwarePage.android}/${SearchSoftwarePage.appleIOS}"

            val fifthRow: Element = summaryList.selectNth("div", 5)
            fifthRow.selectHead("dt").text shouldBe SearchSoftwarePage.softwareType
            fifthRow.selectHead("dd").text shouldBe s"${SearchSoftwarePage.browserBased}/${SearchSoftwarePage.applicationBased}"

            val sixthRow: Element = summaryList.selectNth("div", 6)
            sixthRow.selectHead("dt").text shouldBe SearchSoftwarePage.accessibility
            sixthRow.selectHead("dd").text shouldBe
              s"${SearchSoftwarePage.visual}/${SearchSoftwarePage.hearing}/${SearchSoftwarePage.motor}/${SearchSoftwarePage.cognitive}"
          }
        }
        "have a software vendor with minimal detail" which {
          def secondVendor: Element = softwareVendorsSection.selectHead("#software-vendor-1")

          "has a heading link for the software vendor" in {
            val headingLink: Element = secondVendor.selectHead("h3").selectHead("a")
            headingLink.attr("target") shouldBe "_blank"
            headingLink.attr("rel") shouldBe "noopener noreferrer"
            headingLink.attr("href") shouldBe softwareVendors.vendors(1).url
            headingLink.text shouldBe s"${softwareVendors.vendors(1).name} (opens in a new tab)"
          }

          "has a list of detail for the software vendor with minimal detail" in {
            val summaryList: Element = secondVendor.selectHead("dl")

            val firstRow: Element = summaryList.selectNth("div", 1)
            firstRow.selectHead("dt").text shouldBe SearchSoftwarePage.pricing
            firstRow.selectHead("dd").text shouldBe s"${SearchSoftwarePage.noFreeTrial}/${SearchSoftwarePage.noFreeVersion}"

            val secondRow: Option[Element] = summaryList.selectOptionally("div:nth-of-type(2)")
            secondRow shouldBe None
          }
        }
      }

    }

  }

  object SearchSoftwarePage {
    val title = "Choose the right software for your needs"
    val lastUpdate = "This page was last updated: 01/07/2022"
    val heading = "Choose the right software for your needs"
    val paragraph1: String = "All software on this page has been through HMRC’s recognition process. " +
      "But we do not endorse or recommend any one product or software provider."
    val paragraph2 = "Some software has features suitable if you have accessibility needs, like visual impairment or limited movement."
    val insetText = "If you need help to choose software, contact the software provider before making a decision. We are not able to help you choose software."

    object Filters {
      val filterHeading = "Filters"
      val pricing = "Pricing"
      val businessType = "Business type"
      val compatibleWith = "Compatible with"
      val mobileApp = "Mobile app"
      val softwareType = "Software type"
      val softwareCompatibility = "Software compatibility"
      val accessibilityNeeds = "Accessibility needs"
    }

    val numberOfProviders = "Currently there are 2 software providers"
    val pricing = "Pricing:"
    val freeTrial = "Free trial"
    val freeVersion = "Free version"
    val noFreeTrial = "No free trial"
    val noFreeVersion = "No free version"
    val businessType = "Business type:"
    val individual = "Individual"
    val agent = "Agent"
    val compatibleWith = "Compatible with:"
    val microsoftWindows = "Microsoft Windows"
    val macOS = "Mac OS"
    val mobileApp = "Mobile app:"
    val android = "Android"
    val appleIOS = "Apple iOS"
    val softwareType = "Software type:"
    val browserBased = "Browser based"
    val applicationBased = "Application based"
    val accessibility = "Accessibility:"
    val visual = "Visual"
    val hearing = "Hearing"
    val motor = "Motor"
    val cognitive = "Cognitive"
  }

  private def page = searchSoftwarePage(softwareVendors)

  private def document: Document = Jsoup.parse(page.body)

}
