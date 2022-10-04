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

  import SearchSoftwareViewSpec._

  "Search software page" when {
    Seq(true, false).foreach { betaFeatureSwitch =>
      s"beta is turned ${if (betaFeatureSwitch) "on" else "off"}" when {
        Seq(true, false).foreach { pricingSwitch =>
          s"extra pricing options is turned ${if (pricingSwitch) "on" else "off"}" when {
            Seq(true, false).foreach { hasResults =>
              s"there ${if (hasResults) "are" else "are no"} results" when {
                Seq(true, false).foreach { hasError =>
                  s"there ${if (hasError) "is" else "is not"} an error" should {
                    val document = getDocument(hasResults, hasError = hasError, beta = betaFeatureSwitch, pricing = pricingSwitch)

                    "have a breadcrumb menu containing a link to the guidance page" in {
                      val link = document.selectNth(".govuk-breadcrumbs__list-item", 1).selectHead("a")
                      link.text shouldBe "Guidance"
                      link.attr("href") shouldBe appConfig.guidance
                    }

                    "have a title" in {
                      document.title shouldBe s"""${if (hasError) "Error: " else ""}${SearchSoftwarePageContent.title} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"""
                    }

                    "have the last updated date" in {
                      document.mainContent.selectNth("p", 1).text shouldBe SearchSoftwarePageContent.lastUpdate
                    }

                    "have a heading" in {
                      document.mainContent.selectHead("h1").text shouldBe SearchSoftwarePageContent.heading
                    }

                    "have paragraph1" in {
                      document.mainContent.selectNth("p", 2).text shouldBe SearchSoftwarePageContent.paragraph1
                    }

                    "have inset text" in {
                      document.mainContent.selectHead(".govuk-inset-text").text shouldBe SearchSoftwarePageContent.insetText
                    }

                    "have a filter section" which {
                      val filterSection = getFilterSection(document)

                      "has a heading" in {
                        filterSection.selectHead("h2").text shouldBe SearchSoftwarePageContent.Filters.filterHeading
                      }

                      "has a pricing section" that {
                        val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 1)

                        if (betaFeatureSwitch) {
                          "contains a section header" in {
                            accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.pricing
                          }
                        }

                        "contains a fieldset legend" in {
                          checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePageContent.Filters.pricing
                        }

                        if (pricingSwitch) {
                          "contains a Free trial checkbox" in {
                            validateCheckboxInGroup(checkboxGroup, 1, FreeTrial.key, SearchSoftwarePageContent.freeTrial)
                          }
                        }

                        "contains a Free version checkbox" in {
                          val box = if (pricingSwitch) 2 else 1
                          validateCheckboxInGroup(checkboxGroup, box, FreeVersion.key, SearchSoftwarePageContent.freeVersion)
                        }

                        if (pricingSwitch) {
                          "contains a paid for checkbox" in {
                            validateCheckboxInGroup(checkboxGroup, 3, PaidFor.key, SearchSoftwarePageContent.paidFor)
                          }
                        }
                      }

                      "has an income type section" that {
                        val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 2)

                        if (betaFeatureSwitch) {
                          "contains a section header" in {
                            accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.incomeType
                          }
                        }

                        "contains a fieldset legend" in {
                          checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePageContent.Filters.incomeType
                        }

                        "contains a sole trader checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 1, SoleTrader.key, SearchSoftwarePageContent.soleTrader)
                        }

                        "contains a UK property checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 2, UkProperty.key, SearchSoftwarePageContent.ukProperty)
                        }

                        "contains an overseas property checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 3, OverseasProperty.key, SearchSoftwarePageContent.overseasProperty)
                        }
                      }

                      "has a compatible with section" that {
                        val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 3)
                        if (betaFeatureSwitch) {
                          "contains a section header" in {
                            accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.compatibleWith
                          }
                        }

                        "contains a fieldset legend" in {
                          checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePageContent.Filters.compatibleWith
                        }

                        "contains an Microsoft Windows checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 1, MicrosoftWindows.key, SearchSoftwarePageContent.microsoftWindows)
                        }

                        "contains an Mac OS checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 2, MacOS.key, SearchSoftwarePageContent.macOS)
                        }
                      }

                      "has a mobile app section" that {
                        val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 4)

                        if (betaFeatureSwitch) {
                          "contains a section header" in {
                            accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.mobileApp
                          }
                        }

                        "contains a fieldset legend" in {
                          checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePageContent.Filters.mobileApp
                        }

                        "contains an Android checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 1, Android.key, SearchSoftwarePageContent.android)
                        }

                        "contains an Apple iOS checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 2, AppleIOS.key, SearchSoftwarePageContent.appleIOS)
                        }
                      }

                      "has a software type section" that {
                        val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 5)

                        if (betaFeatureSwitch) {
                          "contains a section header" in {
                            accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.softwareType
                          }
                        }

                        "contains a fieldset legend" in {
                          checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePageContent.Filters.softwareType
                        }

                        "contains an BrowserBased checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 1, BrowserBased.key, SearchSoftwarePageContent.browserBased)
                        }

                        "contains an ApplicationBased checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 2, ApplicationBased.key, SearchSoftwarePageContent.applicationBased)
                        }
                      }

                      "has a software for section" that {
                        val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 6)

                        if (betaFeatureSwitch) {
                          "contains a section header" in {
                            accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.softwareFor
                          }
                        }

                        "contains a fieldset legend" in {
                          checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePageContent.Filters.softwareFor
                        }

                        "contains a RecordKeeping checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 1, RecordKeeping.key, SearchSoftwarePageContent.recordKeeping)
                        }

                        "contains a Bridging checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 2, Bridging.key, SearchSoftwarePageContent.bridging)
                        }
                      }

                      "has a business type section" that {
                        val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 7)

                        if (betaFeatureSwitch) {
                          "contains a section header" in {
                            accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.businessType
                          }
                        }

                        "contains a fieldset legend" in {
                          checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePageContent.Filters.businessType
                        }

                        "contains an Individual checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 1, Individual.key, SearchSoftwarePageContent.individual)
                        }

                        "contains an Agent checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 2, Agent.key, SearchSoftwarePageContent.agent)
                        }
                      }

                      "has a software compatibility section" that {
                        val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 8)

                        if (betaFeatureSwitch) {
                          "contains a section header" in {
                            accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.softwareCompatibility
                          }
                        }

                        "contains a fieldset legend" in {
                          checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePageContent.Filters.softwareCompatibility
                        }

                        "contains an VAT checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 1, Vat.key, SearchSoftwarePageContent.vat)
                        }

                        "contains an Income Tax checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 2, "", SearchSoftwarePageContent.incomeTax, disabled = true, checked = true)
                        }
                      }

                      "has a language section" that {
                        val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 9)

                        if (betaFeatureSwitch) {
                          "contains a section header" in {
                            accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.language
                          }
                        }

                        "contains a Welsh checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 1, Welsh.key, SearchSoftwarePageContent.welsh)
                        }

                        "contains an English checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 2, "", SearchSoftwarePageContent.english, disabled = true, checked = true)
                        }

                      }

                      "has an accessibility features section" that {
                        val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 10)

                        if (betaFeatureSwitch) {
                          "contains a section header" in {
                            accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.accessibilityFeatures
                          }
                        }

                        "contains a fieldset legend" in {
                          checkboxGroup.getElementsByTag("legend").text() shouldBe SearchSoftwarePageContent.Filters.accessibilityFeatures
                        }

                        "contains an Visual checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 1, Visual.key, SearchSoftwarePageContent.visual)
                        }

                        "contains an Hearing checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 2, Hearing.key, SearchSoftwarePageContent.hearing)
                        }

                        "contains an Motor checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 3, Motor.key, SearchSoftwarePageContent.motor)
                        }

                        "contains an Cognitive checkbox" in {
                          validateCheckboxInGroup(checkboxGroup, 4, Cognitive.key, SearchSoftwarePageContent.cognitive)
                        }
                      }

                      "has a apply button section" that {
                        "contains an apply filters button" in {
                          filterSection.selectHead(".apply-filters-button").text shouldBe SearchSoftwarePageContent.Filters.applyFilters
                        }

                        "contains an clear filters button" in {
                          filterSection.selectHead(".clear-filters-button").text shouldBe SearchSoftwarePageContent.Filters.clearFilters
                        }

                      }
                    }

                    "has a search form" which {
                      "contains a heading" in {
                        document.mainContent.selectHead("form h2 label").text shouldBe SearchSoftwarePageContent.searchFormHeading
                      }

                      "contains a text input" in {
                        document.mainContent.selectHead("#searchTerm").attr("name") shouldBe "searchTerm"
                        if (!hasError)
                          document.mainContent.selectHead("#searchTerm").attr("value") shouldBe "search test"
                      }

                      "contains a submit" in {
                        document.mainContent.selectHead("#searchButton").text shouldBe SearchSoftwarePageContent.searchFormHeading
                      }

                    }

                    if (hasError) "contains an error" in {
                      document.mainContent.selectHead(".govuk-error-message").text() shouldBe "test error message"
                    }


                    if (!hasResults) "displays a message when the list is empty" which {
                      val emptyListMessage = document.mainContent.selectHead(".empty-list")

                      "contains a paragraph" in {
                        emptyListMessage.selectHead("p").text shouldBe SearchSoftwarePageContent.emptyVendorListMessage
                      }

                      "contains two bullet points" in {
                        emptyListMessage.selectNth("ul li", 1).text shouldBe SearchSoftwarePageContent.emptyVendorListMessageBullet1
                        emptyListMessage.selectNth("ul li", 2).text shouldBe SearchSoftwarePageContent.emptyVendorListMessageBullet2
                      }
                    } else if (betaFeatureSwitch) "have a beta software vendor section" which {
                      val softwareVendorsSection = getSoftwareVendorsSection(document)

                      "has a count of the number of software vendors on the page" in {
                        softwareVendorsSection.selectHead(".software-vendors-num").text shouldBe SearchSoftwarePageContent.numberOfVendors
                      }

                      "have a list of software vendors" which {

                        "have a line for software vendor one" which {
                          def firstVendor: Element = softwareVendorsSection.selectHead("#software-vendor-0")

                          "has a heading link for the software vendor" in {
                            val headingLink: Element = firstVendor.selectHead("h3").selectHead("a")
                            val firstVendorInModel = SearchSoftwarePageContent.softwareVendorsResults.vendors.head
                            headingLink.attr("href") shouldBe ProductDetailsController.show(Some(firstVendorInModel.name)).url
                            headingLink.text shouldBe s"${firstVendorInModel.name}"
                          }
                        }

                        "have a line for software vendor two" which {
                          def secondVendor: Element = softwareVendorsSection.selectHead("#software-vendor-1")

                          "has a heading link for the software vendor" in {
                            val headingLink: Element = secondVendor.selectHead("h3").selectHead("a")
                            val secondVendorInModel = SearchSoftwarePageContent.softwareVendorsResults.vendors(1)
                            headingLink.attr("href") shouldBe ProductDetailsController.show(Some(secondVendorInModel.name)).url
                            headingLink.text shouldBe s"${secondVendorInModel.name}"
                          }
                        }
                      }
                    } else "have a alpha software vendor section" which {
                      val softwareVendorsSection = getSoftwareVendorsSection(document)

                      "has a count of the number of software vendors on the page" in {
                        softwareVendorsSection.selectHead("h3").text shouldBe SearchSoftwarePageContent.numberOfVendorsAlpha
                      }

                      "has a list of software vendors" which {

                        "has a software vendor with lots of detail" which {
                          def firstVendor: Element = softwareVendorsSection.selectHead("#software-vendor-0")

                          "has a heading link for the software vendor" in {
                            val headingLink: Element = firstVendor.selectHead("h3").selectHead("a")
                            headingLink.attr("target") shouldBe "_blank"
                            headingLink.attr("rel") shouldBe "noopener noreferrer"
                            headingLink.attr("href") shouldBe SearchSoftwarePageContent.softwareVendorsResults.vendors.head.url
                            headingLink.text shouldBe s"${SearchSoftwarePageContent.softwareVendorsResults.vendors.head.name} (opens in a new tab)"
                          }

                          "has a list of detail for the software vendor with full detail" in {
                            val summaryList: Element = firstVendor.selectHead("dl")

                            val firstRow: Element = summaryList.selectNth("div", 1)
                            firstRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.pricing
                            val pricingText = if (pricingSwitch)
                              s"${SearchSoftwarePageContent.freeTrial}/${SearchSoftwarePageContent.freeVersion}"
                            else
                              SearchSoftwarePageContent.freeVersion
                            firstRow.selectHead("dd").text shouldBe pricingText

                            val secondRow: Element = summaryList.selectNth("div", 2)
                            secondRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.businessType
                            secondRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.individual}/${SearchSoftwarePageContent.agent}"

                            val thirdRow: Element = summaryList.selectNth("div", 3)
                            thirdRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.compatibleWith
                            thirdRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.microsoftWindows}/${SearchSoftwarePageContent.macOS}"

                            val forthRow: Element = summaryList.selectNth("div", 4)
                            forthRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.mobileApp
                            forthRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.android}/${SearchSoftwarePageContent.appleIOS}"

                            val fifthRow: Element = summaryList.selectNth("div", 5)
                            fifthRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.softwareType
                            fifthRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.browserBased}/${SearchSoftwarePageContent.applicationBased}"

                            val sixthRow: Element = summaryList.selectNth("div", 6)
                            sixthRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.accessibility
                            sixthRow.selectHead("dd").text shouldBe
                              s"${SearchSoftwarePageContent.visual}/${SearchSoftwarePageContent.hearing}/${SearchSoftwarePageContent.motor}/${SearchSoftwarePageContent.cognitive}"
                          }
                        }

                        "has a software vendor with minimal detail" which {
                          def secondVendor: Element = softwareVendorsSection.selectHead("#software-vendor-1")

                          "has a heading link for the software vendor" in {
                            val headingLink: Element = secondVendor.selectHead("h3").selectHead("a")
                            headingLink.attr("target") shouldBe "_blank"
                            headingLink.attr("rel") shouldBe "noopener noreferrer"
                            headingLink.attr("href") shouldBe SearchSoftwarePageContent.softwareVendorsResults.vendors(1).url
                            headingLink.text shouldBe s"${SearchSoftwarePageContent.softwareVendorsResults.vendors(1).name} (opens in a new tab)"
                          }

                          "has a list of detail for the software vendor with minimal detail" in {
                            val summaryList: Element = secondVendor.selectHead("dl")

                            val firstRow: Element = summaryList.selectNth("div", 1)
                            firstRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.pricing

                            val pricingText = if (pricingSwitch)
                              s"${SearchSoftwarePageContent.noFreeTrial}/${SearchSoftwarePageContent.noFreeVersion}"
                            else
                              SearchSoftwarePageContent.noFreeVersion
                            firstRow.selectHead("dd").text shouldBe pricingText

                            val secondRow: Option[Element] = summaryList.selectOptionally("div:nth-of-type(2)")
                            secondRow shouldBe None
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

object SearchSoftwareViewSpec extends ViewSpec {

  def page(softwareVendors: SoftwareVendors, hasError: Boolean, beta: Boolean, pricing: Boolean) =
    searchSoftwarePage(
      softwareVendors,
      if (hasError) {
        FiltersForm.form.withError(testFormError)
      } else {
        FiltersForm.form.fill(FiltersFormModel(Some("search test")))
      },
      Call("POST", "/test-url"),
      beta,
      pricing
    )

  def getDocument(hasResults: Boolean, hasError: Boolean, beta: Boolean, pricing: Boolean): Document = {
    val results = if (hasResults) SearchSoftwarePageContent.softwareVendorsResults else SearchSoftwarePageContent.softwareVendorsNoResults
    Jsoup.parse(page(results, hasError, beta, pricing).body)
  }

  def getCheckboxItem(checkboxGroup: Element, n: Int): Element = checkboxGroup
    .selectNth(".govuk-checkboxes__item", n)

  def getCheckboxInput(checkboxItem: Element): Element = checkboxItem
    .selectHead(".govuk-checkboxes__input")

  def getCheckboxLabel(checkboxItem: Element): Element = checkboxItem
    .selectHead(".govuk-checkboxes__label")

  def validateCheckboxInGroup(
                               checkboxGroup: Element,
                               n: Int,
                               value: String,
                               label: String,
                               name: String = s"${FiltersForm.filters}[]",
                               disabled: Boolean = false,
                               checked: Boolean = false): Assertion = {
    val checkboxItem = getCheckboxItem(checkboxGroup, n)
    getCheckboxLabel(checkboxItem).text shouldBe label

    val checkbox = getCheckboxInput(checkboxItem)
    checkbox.attr("value") shouldBe value
    checkbox.attr("name") shouldBe name
    checkbox.hasAttr("disabled") shouldBe disabled
    checkbox.hasAttr("checked") shouldBe checked
  }

  def getSoftwareVendorsSection(document: Document): Element = {
    document
      .mainContent
      .selectHead("#software-section")
      .selectHead(".govuk-grid-column-two-thirds")
  }

  def getAccordionSectionHeaderAndCheckboxGroup(document: Document, betaFeatureSwitch: Boolean, n: Int): (Option[Element], Element) = {
    if (betaFeatureSwitch) {
      val accordionSection = getFilterSection(document)
        .selectHead(".govuk-accordion")
        .selectNth(".govuk-accordion__section", n)
      (
        Some(accordionSection.selectHead(".govuk-accordion__section-header")),
        accordionSection.selectHead(".govuk-fieldset")
      )
    } else {
      (
        None,
        getFilterSection(document)
          .selectNth(".govuk-form-group", n)
          .selectNth(".govuk-fieldset", 1)
      )
    }
  }

  def getFilterSection(document: Document): Element = document.mainContent.selectHead("#software-section").selectHead(".filters-section")

  private val searchSoftwarePage = app.injector.instanceOf[SearchSoftwarePage]
  private val testFormError: FormError = FormError(FiltersForm.searchTerm, "test error message")

}

private object SearchSoftwarePageContent {
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
    val accessibilityFeatures = "Accessibility features"
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
  val english = "English"
  val visual = "Visual"
  val hearing = "Hearing"
  val motor = "Motor"
  val cognitive = "Cognitive"

  private val lastUpdateTest = "01/07/2022"

  val softwareVendorsResults: SoftwareVendors = SoftwareVendors(
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

  val softwareVendorsNoResults = SoftwareVendors(lastUpdated = lastUpdateTest, vendors = Seq.empty[SoftwareVendorModel])


}
