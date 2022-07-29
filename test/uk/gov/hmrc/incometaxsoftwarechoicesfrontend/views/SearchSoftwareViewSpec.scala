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
import org.scalatest.Assertion
import play.api.data.FormError
import play.api.mvc.Call
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{FiltersFormModel, SoftwareVendorModel, SoftwareVendors, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwarePage

class SearchSoftwareViewSpec extends ViewSpec {

  private val searchSoftwarePage = app.injector.instanceOf[SearchSoftwarePage]
  private val lastUpdateTest = "01/07/2022"
  private val testFormError: FormError = FormError(FiltersForm.searchTerm, "test error message")
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
        document().selectNth(".govuk-breadcrumbs__list-item", 1).text shouldBe "Guidance"
      }

      "contains the current page" in {
        document().selectNth(".govuk-breadcrumbs__list-item", 2).text shouldBe "Filter"
      }
    }

    "have a title" in {
      document().title shouldBe s"""${SearchSoftwarePage.title} - Find software for Making Tax Digital for Income Tax - GOV.UK"""
    }

    "have the last updated date" in {
      document().mainContent.selectNth("p", 1).text shouldBe SearchSoftwarePage.lastUpdate
    }

    "have a heading" in {
      document().mainContent.selectHead("h1").text shouldBe SearchSoftwarePage.heading
    }

    "have paragraph1" in {
      document().mainContent.selectNth("p", 2).text shouldBe SearchSoftwarePage.paragraph1
    }

    "have paragraph2" in {
      document().mainContent.selectNth("p", 3).text shouldBe SearchSoftwarePage.paragraph2
    }

    "have inset text" in {
      document().mainContent.selectHead(".govuk-inset-text").text shouldBe SearchSoftwarePage.insetText
    }

    "have a filter section" which {
      def filterSection: Element = document().mainContent.selectHead("#software-section").selectHead(".filters-section")

      def getCheckboxGroup(n: Int): Element = filterSection.selectNth(".govuk-form-group", n).selectNth(".govuk-fieldset", 1)

      def getCheckboxItem(checkboxGroup: Element, n: Int): Element = checkboxGroup
        .selectNth(".govuk-checkboxes__item", n)

      def getCheckboxInput(checkboxItem: Element): Element = checkboxItem
        .selectHead(".govuk-checkboxes__input")

      def getCheckboxLabel(checkboxItem: Element): Element = checkboxItem
        .selectHead(".govuk-checkboxes__label")

      def validateCheckboxInGroup(checkboxGroup: Element, n: Int, value: String, label: String, name: String = s"${FiltersForm.filters}[]"): Assertion = {
        val checkboxItem = getCheckboxItem(checkboxGroup, n)
        getCheckboxLabel(checkboxItem).text shouldBe label

        val checkbox = getCheckboxInput(checkboxItem)
        checkbox.attr("value") shouldBe value
        checkbox.attr("name") shouldBe name
        checkbox.hasAttr("disabled") shouldBe false
      }

      "has a heading" in {
        filterSection.selectHead("h2").text shouldBe SearchSoftwarePage.Filters.filterHeading
      }

      "has a pricing section" that {
        val checkboxGroup = getCheckboxGroup(1)

        "contains a fieldset legend" in {
          checkboxGroup.selectHead("legend").text shouldBe SearchSoftwarePage.Filters.pricing
        }

        "contains a Free trial checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 1, FreeTrial.key, SearchSoftwarePage.freeTrial)
        }

        "contains a Free version checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 2, FreeVersion.key, SearchSoftwarePage.freeVersion)
        }
      }

      "has an income type section" that {
        val checkboxGroup = getCheckboxGroup(2)

        "contains a fieldset legend" in {
          checkboxGroup.selectHead("legend").text shouldBe SearchSoftwarePage.Filters.incomeType
        }

        "contains a sole trader checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 1, SoleTrader.key, SearchSoftwarePage.soleTrader)
        }

        "contains a UK property checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 2, UkProperty.key, SearchSoftwarePage.ukProperty)
        }

        "contains an overseas property checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 3, OverseasProperty.key, SearchSoftwarePage.overseasProperty)
        }
      }

      "has a business type section" that {
        val checkboxGroup = getCheckboxGroup(3)

        "contains a fieldset legend" in {
          checkboxGroup.selectHead("legend").text shouldBe SearchSoftwarePage.Filters.businessType
        }

        "contains an Individual checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 1, Individual.key, SearchSoftwarePage.individual)
        }

        "contains an Agent checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 2, Agent.key, SearchSoftwarePage.agent)
        }
      }

      "has a compatible with section" that {
        val checkboxGroup = getCheckboxGroup(4)

        "contains a fieldset legend" in {
          checkboxGroup.selectHead("legend").text shouldBe SearchSoftwarePage.Filters.compatibleWith
        }

        "contains an Microsoft Windows checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 1, MicrosoftWindows.key, SearchSoftwarePage.microsoftWindows)
        }

        "contains an Mac OS checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 2, MacOS.key, SearchSoftwarePage.macOS)
        }
      }

      "has a mobile app section" that {
        val checkboxGroup = getCheckboxGroup(5)

        "contains a fieldset legend" in {
          checkboxGroup.selectHead("legend").text shouldBe SearchSoftwarePage.Filters.mobileApp
        }

        "contains an Android checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 1, Android.key, SearchSoftwarePage.android)
        }

        "contains an Apple iOS checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 2, AppleIOS.key, SearchSoftwarePage.appleIOS)
        }
      }

      "has a software compatibility section" that {
        val checkboxGroup = getCheckboxGroup(6)

        "contains a fieldset legend" in {
          checkboxGroup.selectHead("legend").text shouldBe SearchSoftwarePage.Filters.softwareCompatibility
        }

        "contains an VAT checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 1, Vat.key, SearchSoftwarePage.vat)
        }

        "contains an Income Tax checkbox" in {
          val checkboxItem = getCheckboxItem(checkboxGroup, 2)
          val checkbox = getCheckboxInput(checkboxItem)

          checkbox.attr("name") shouldBe s"${FiltersForm.filters}[]"
          checkbox.hasAttr("checked") shouldBe true
          checkbox.hasAttr("disabled") shouldBe true

          getCheckboxLabel(checkboxItem).text shouldBe SearchSoftwarePage.incomeTax
        }
      }

      "has a software type section" that {
        val checkboxGroup = getCheckboxGroup(7)

        "contains a label" in {
          checkboxGroup.selectHead("legend").text shouldBe SearchSoftwarePage.Filters.softwareType
        }

        "contains an BrowserBased checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 1, BrowserBased.key, SearchSoftwarePage.browserBased)
        }

        "contains an ApplicationBased checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 2, ApplicationBased.key, SearchSoftwarePage.applicationBased)
        }
      }

      "has an accessibility needs section" that {
        val checkboxGroup = getCheckboxGroup(8)

        "contains a label" in {
          checkboxGroup.selectHead("legend").text shouldBe SearchSoftwarePage.Filters.accessibilityNeeds
        }

        "contains an Visual checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 1, Visual.key, SearchSoftwarePage.visual)
        }

        "contains an Hearing checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 2, Hearing.key, SearchSoftwarePage.hearing)
        }

        "contains an Motor checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 3, Motor.key, SearchSoftwarePage.motor)
        }

        "contains an Cognitive checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 4, Cognitive.key, SearchSoftwarePage.cognitive)
        }

      }

      "has a apply button section" that {
        "contains an apply filters button" in {
          filterSection.selectHead(".apply-filters-button").text shouldBe SearchSoftwarePage.Filters.applyFilters
        }

        "contains an clear filters button" in {
          filterSection.selectHead(".clear-filters-button").text shouldBe SearchSoftwarePage.Filters.clearFilters
        }
      }
    }

    "have a software vendor section" which {
      def softwareVendorsSection: Element = document()
        .mainContent
        .selectHead("#software-section")
        .selectHead(".govuk-grid-column-two-thirds")

      "has a count of the number of software vendors on the page" in {
        softwareVendorsSection.selectHead("h3").text shouldBe SearchSoftwarePage.numberOfVendors
      }

      "has a search form" which {
        "contains a heading" in {
          document().mainContent.selectHead("form h2 label").text shouldBe SearchSoftwarePage.searchFormHeading
        }

        "contains a text input" in {
          document().mainContent.selectHead("#searchTerm").attr("name") shouldBe "searchTerm"
          document().mainContent.selectHead("#searchTerm").attr("value") shouldBe "search test"
        }

        "contains a submit" in {
          document().mainContent.selectHead("#searchButton").text shouldBe SearchSoftwarePage.searchFormHeading
        }

        "contains an error" in {
          document(hasError = true).mainContent.selectHead(".govuk-error-message").text() shouldBe "test error message"
        }
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
    val searchFormHeading = "Search by software name"

    object Filters {
      val filterHeading = "Filters"
      val pricing = "Pricing"
      val incomeType = "Income type"
      val businessType = "Business type"
      val compatibleWith = "Compatible with"
      val mobileApp = "Mobile app"
      val softwareType = "Software type"
      val softwareCompatibility = "Software compatibility"
      val accessibilityNeeds = "Accessibility needs"
      val applyFilters = "Apply filters"
      val clearFilters = "Clear filters"
    }

    val numberOfVendors = "Currently there are 2 software providers"
    val pricing = "Pricing:"
    val freeTrial = "Free trial"
    val freeVersion = "Free version"
    val noFreeTrial = "No free trial"
    val noFreeVersion = "No free version"
    val soleTrader = "Sole trader"
    val ukProperty = "UK property"
    val overseasProperty = "Overseas property"
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
    val vat = "VAT"
    val incomeTax = "Income Tax"
    val accessibility = "Accessibility:"
    val visual = "Visual"
    val hearing = "Hearing"
    val motor = "Motor"
    val cognitive = "Cognitive"
  }

  private def page(hasError: Boolean) =
    searchSoftwarePage(
      softwareVendors,
      if (hasError) {
        FiltersForm.form.withError(testFormError)
      } else {
        FiltersForm.form.fill(FiltersFormModel(Some("search test")))
      },
      Call("POST", "/test-url")
    )

  private def document(hasError: Boolean = false): Document = Jsoup.parse(page(hasError).body)

}
