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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes.ProductDetailsController
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwarePage

class SearchSoftwareViewSpec extends ViewSpec {

  private val searchSoftwarePage = app.injector.instanceOf[SearchSoftwarePage]
  private val lastUpdateTest = "01/07/2022"
  private val testFormError: FormError = FormError(FiltersForm.searchTerm, "test error message")
  private val testSoftwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = lastUpdateTest,
    vendors = Seq(
      SoftwareVendorModel(
        name = "test software vendor one",
        url = "/test-vendor-one-url",
        email = Some("test@software-vendor-name-one.com"),
        phone = Some("11111 111 111"),
        website = "software-vendor-name-one.com",
        filters = Seq(
          VendorFilter.FreeVersion,
          VendorFilter.FreeTrial,
          VendorFilter.PaidFor,
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
          VendorFilter.Bridging
        ),
        incomeAndDeductions = Seq.empty[IncomeAndDeduction]
      ),
      SoftwareVendorModel(
        name = "test software vendor two",
        url = "/test-vendor-two-url",
        email = Some("test@software-vendor-name-two.com"),
        phone = Some("22222 222 222"),
        website = "software-vendor-name-two.com",
        filters = Seq.empty[VendorFilter],
        incomeAndDeductions = Seq.empty[IncomeAndDeduction]
      )
    )
  )

  "Search software page" when {
    "beta is turned on" should {
      "have a software vendor section" which {
        def softwareVendorsSection: Element = document(beta = true)
          .mainContent
          .selectHead("#software-section")
          .selectHead(".govuk-grid-column-two-thirds")

        "has a count of the number of software vendors on the page" in {
          softwareVendorsSection.selectHead(".software-vendors-num").text shouldBe SearchSoftwarePage.numberOfVendors
        }

        "have a list of software vendors" which {

          "have a line for software vendor one" which {
            def firstVendor: Element = softwareVendorsSection.selectHead("#software-vendor-0")

            "has a heading link for the software vendor" in {
              val headingLink: Element = firstVendor.selectHead("h3").selectHead("a")
              val firstVendorInModel = testSoftwareVendors.vendors.head
              headingLink.attr("href") shouldBe ProductDetailsController.show(Some(firstVendorInModel.name)).url
              headingLink.text shouldBe s"${firstVendorInModel.name}"
            }
          }

          "have a line for software vendor two" which {
            def secondVendor: Element = softwareVendorsSection.selectHead("#software-vendor-1")

            "has a heading link for the software vendor" in {
              val headingLink: Element = secondVendor.selectHead("h3").selectHead("a")
              val secondVendorInModel = testSoftwareVendors.vendors(1)
              headingLink.attr("href") shouldBe ProductDetailsController.show(Some(secondVendorInModel.name)).url
              headingLink.text shouldBe s"${secondVendorInModel.name}"
            }

          }

        }

      }
    }
    "beta is turned off" should {
      "have a breadcrumb menu" which {
        "contains a link to the guidance page" in {
          val link = document().selectNth(".govuk-breadcrumbs__list-item", 1).selectHead("a")
          link.text shouldBe "Guidance"
          link.attr("href") shouldBe appConfig.guidance
        }

        "contains the current page" in {
          document().selectNth(".govuk-breadcrumbs__list-item", 2).text shouldBe "Filter"
        }
      }

      "have a title" in {
        document().title shouldBe s"""${SearchSoftwarePage.title} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"""
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

      "have inset text" in {
        document().mainContent.selectHead(".govuk-inset-text").text shouldBe SearchSoftwarePage.insetText
      }

      "have a filter section" which {
        def filterSection: Element = document().mainContent.selectHead("#software-section").selectHead(".filters-section")

        def getAccordionSectionHeaderAndCheckboxGroup(n: Int): (Element, Element) = {
          val accordionSection = filterSection.selectHead(".govuk-accordion").selectNth(".govuk-accordion__section", n)
          (
            accordionSection.selectHead(".govuk-accordion__section-header"),
            accordionSection.selectHead(".govuk-fieldset")
          )
        }

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
          val (accordionSectionHeader, checkboxGroup)  = getAccordionSectionHeaderAndCheckboxGroup(1)

          "contains a section header" in {
            accordionSectionHeader.text shouldBe SearchSoftwarePage.Filters.pricing
          }

          "contains a fieldset legend" in {
            checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePage.Filters.pricing
          }

          "contains a Free trial checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 1, FreeTrial.key, SearchSoftwarePage.freeTrial)
          }

          "contains a Free version checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 2, FreeVersion.key, SearchSoftwarePage.freeVersion)
          }

          "contains a Paid for checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 3, PaidFor.key, SearchSoftwarePage.paidFor)
          }
        }

        "has an income type section" that {
          val (accordionSectionHeader, checkboxGroup)  = getAccordionSectionHeaderAndCheckboxGroup(2)

          "contains a section header" in {
            accordionSectionHeader.text shouldBe SearchSoftwarePage.Filters.incomeType
          }

          "contains a fieldset legend" in {
            checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePage.Filters.incomeType
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

        "has a compatible with section" that {
          val (accordionSectionHeader, checkboxGroup)  = getAccordionSectionHeaderAndCheckboxGroup(3)

          "contains a section header" in {
            accordionSectionHeader.text shouldBe SearchSoftwarePage.Filters.compatibleWith
          }

          "contains a fieldset legend" in {
            checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePage.Filters.compatibleWith
          }

          "contains an Microsoft Windows checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 1, MicrosoftWindows.key, SearchSoftwarePage.microsoftWindows)
          }

          "contains an Mac OS checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 2, MacOS.key, SearchSoftwarePage.macOS)
          }
        }

        "has a mobile app section" that {
          val (accordionSectionHeader, checkboxGroup)  = getAccordionSectionHeaderAndCheckboxGroup(4)

          "contains a section header" in {
            accordionSectionHeader.text shouldBe SearchSoftwarePage.Filters.mobileApp
          }

          "contains a fieldset legend" in {
            checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePage.Filters.mobileApp
          }

          "contains an Android checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 1, Android.key, SearchSoftwarePage.android)
          }

          "contains an Apple iOS checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 2, AppleIOS.key, SearchSoftwarePage.appleIOS)
          }
        }

        "has a software type section" that {
          val (accordionSectionHeader, checkboxGroup)  = getAccordionSectionHeaderAndCheckboxGroup(5)

          "contains a section header" in {
            accordionSectionHeader.text shouldBe SearchSoftwarePage.Filters.softwareType
          }

          "contains a fieldset legend" in {
            checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePage.Filters.softwareType
          }

          "contains an BrowserBased checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 1, BrowserBased.key, SearchSoftwarePage.browserBased)
          }

          "contains an ApplicationBased checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 2, ApplicationBased.key, SearchSoftwarePage.applicationBased)
          }
        }

        "has a software for section" that {
          val (accordionSectionHeader, checkboxGroup)  = getAccordionSectionHeaderAndCheckboxGroup(6)

          "contains a section header" in {
            accordionSectionHeader.text shouldBe SearchSoftwarePage.Filters.softwareFor
          }

          "contains a fieldset legend" in {
            checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePage.Filters.softwareFor
          }

          "contains a RecordKeeping checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 1, RecordKeeping.key, SearchSoftwarePage.recordKeeping)
          }

          "contains a Bridging checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 2, Bridging.key, SearchSoftwarePage.bridging)
          }
        }

        "has a business type section" that {
          val (accordionSectionHeader, checkboxGroup)  = getAccordionSectionHeaderAndCheckboxGroup(7)

          "contains a section header" in {
            accordionSectionHeader.text shouldBe SearchSoftwarePage.Filters.businessType
          }

          "contains a fieldset legend" in {
            checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePage.Filters.businessType
          }

          "contains an Individual checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 1, Individual.key, SearchSoftwarePage.individual)
          }

          "contains an Agent checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 2, Agent.key, SearchSoftwarePage.agent)
          }
        }

        "has a software compatibility section" that {
          val (accordionSectionHeader, checkboxGroup)  = getAccordionSectionHeaderAndCheckboxGroup(8)

          "contains a section header" in {
            accordionSectionHeader.text shouldBe SearchSoftwarePage.Filters.softwareCompatibility
          }

          "contains a fieldset legend" in {
            checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePage.Filters.softwareCompatibility
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

        "has a language section" that {
          val (accordionSectionHeader, checkboxGroup)  = getAccordionSectionHeaderAndCheckboxGroup(9)

          "contains a section header" in {
            accordionSectionHeader.text shouldBe SearchSoftwarePage.Filters.language
          }

          "contains a Welsh checkbox" in {
            validateCheckboxInGroup(checkboxGroup, 1, Welsh.key, SearchSoftwarePage.welsh)
          }

        }

        "has an accessibility needs section" that {
          val (accordionSectionHeader, checkboxGroup)  = getAccordionSectionHeaderAndCheckboxGroup(10)

          "contains a section header" in {
            accordionSectionHeader.text shouldBe SearchSoftwarePage.Filters.accessibilityNeeds
          }

          "contains a fieldset legend" in {
            checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePage.Filters.accessibilityNeeds
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
          softwareVendorsSection.selectHead("h3").text shouldBe SearchSoftwarePage.numberOfVendorsAlpha
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
              headingLink.attr("href") shouldBe testSoftwareVendors.vendors.head.url
              headingLink.text shouldBe s"${testSoftwareVendors.vendors.head.name} (opens in a new tab)"
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
              headingLink.attr("href") shouldBe testSoftwareVendors.vendors(1).url
              headingLink.text shouldBe s"${testSoftwareVendors.vendors(1).name} (opens in a new tab)"
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

          "displays a message when the list is empty" which {
            val emptyListMessage = document(
              softwareVendors = SoftwareVendors(lastUpdated = lastUpdateTest, vendors = Seq.empty[SoftwareVendorModel])
            ).mainContent.selectHead(".empty-list")

            "contains a paragraph" in {
              emptyListMessage.selectHead("p").text shouldBe SearchSoftwarePage.emptyVendorListMessage
            }

            "contains two bullet points" in {
              emptyListMessage.selectNth("ul li", 1).text shouldBe SearchSoftwarePage.emptyVendorListMessageBullet1
              emptyListMessage.selectNth("ul li", 2).text shouldBe SearchSoftwarePage.emptyVendorListMessageBullet2
            }
          }
        }

      }

    }
  }

  private object SearchSoftwarePage {
    val title = "Choose your software"
    val lastUpdate = "This page was last updated: 01/07/2022"
    val heading = "Choose your software"
    val paragraph1: String = "All software on this page has been through HMRC’s recognition process. " +
      "But we do not endorse or recommend any one product or software provider."
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
      val softwareFor = "Software for"
      val softwareCompatibility = "Software compatibility"
      val language = "Language"
      val accessibilityNeeds = "Accessibility needs"
      val applyFilters = "Apply filters"
      val clearFilters = "Clear filters"
    }

    val numberOfVendorsAlpha = "Currently there are 2 software providers"

    val numberOfVendors = "2 software providers"

    val emptyVendorListMessage = "Your search has returned no results. To increase the number of results, we suggest you:"
    val emptyVendorListMessageBullet1 = "reduce the number of filters you apply"
    val emptyVendorListMessageBullet2 = "make sure the name you have entered into the search bar is correct"
    val pricing = "Pricing:"
    val freeTrial = "Free trial"
    val freeVersion = "Free version"
    val paidFor = "Paid for"
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
    val recordKeeping = "Record keeping"
    val bridging = "Bridging"
    val mobileApp = "Mobile app:"
    val android = "Android"
    val appleIOS = "Apple iOS"
    val softwareType = "Software type:"
    val browserBased = "Browser based"
    val applicationBased = "Application based"
    val vat = "VAT"
    val incomeTax = "Income Tax"
    val accessibility = "Accessibility:"
    val welsh = "Welsh"
    val visual = "Visual"
    val hearing = "Hearing"
    val motor = "Motor"
    val cognitive = "Cognitive"
  }

  private def page(softwareVendors: SoftwareVendors, hasError: Boolean, beta: Boolean) =
    searchSoftwarePage(
      softwareVendors,
      if (hasError) {
        FiltersForm.form.withError(testFormError)
      } else {
        FiltersForm.form.fill(FiltersFormModel(Some("search test")))
      },
      Call("POST", "/test-url"),
      beta
    )

  private def document(softwareVendors: SoftwareVendors = testSoftwareVendors, hasError: Boolean = false, beta: Boolean = false): Document =
    Jsoup.parse(page(softwareVendors, hasError, beta).body)

}
