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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import play.api.data.FormError
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes.ProductDetailsController
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwarePage

import java.net.URLEncoder
import java.time.LocalDate

class SearchSoftwareViewSpec extends ViewSpec {

  import SearchSoftwareViewSpec._

  "Search software page" must {
    lazy val document = getDocument(hasResults = false, hasError = false)

    "have a back link to the guidance page" in {
      val link = document.selectHead(".govuk-back-link")
      link.text shouldBe "Back"
      link.attr("href") shouldBe appConfig.guidance
    }

    "have a title" in {
      document.title shouldBe s"""${SearchSoftwarePageContent.title} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"""
    }

    "have a heading" in {
      document.mainContent.selectHead("h1").text shouldBe SearchSoftwarePageContent.heading
    }

    "have paragraph1" in {
      document.mainContent.selectNth("p", 1).text shouldBe SearchSoftwarePageContent.paragraph1
    }

    "have inset text" in {
      document.mainContent.selectHead(".govuk-inset-text").text shouldBe SearchSoftwarePageContent.insetText
    }

    "have What Kind of Software is available? details and content" which {

      "has a heading" in {
        document.mainContent.selectHead(".govuk-details").selectHead("span").text shouldBe SearchSoftwarePageContent.whatSoftwareHeading
      }

      "has a Record Keeping Software section" in {
        document.mainContent.selectHead(".govuk-details").selectNth("h2", 1).text shouldBe SearchSoftwarePageContent.whatSoftwareRecordKeepingHeading
      }

      "has first bullet point in Record Keeping Software section" in {
        document.mainContent.selectHead(".govuk-details").selectNth("li", 1).text shouldBe SearchSoftwarePageContent.whatSoftwareRecordKeepingBullet1
      }

      "has second bullet point in Record Keeping Software section" in {
        document.mainContent.selectHead(".govuk-details").selectNth("li", 2).text shouldBe SearchSoftwarePageContent.whatSoftwareRecordKeepingBullet2
      }

      "has a Bridging section" in {
        document.mainContent.selectHead(".govuk-details").selectNth("h2", 2).text shouldBe SearchSoftwarePageContent.whatSoftwareBridgingHeading
      }

      "has first bullet point in Bridging section" in {
        document.mainContent.selectHead(".govuk-details").selectNth("ul", 2).selectNth("li", 1).text shouldBe SearchSoftwarePageContent.whatSoftwareBridgingBullet1
      }

      "has second bullet point in Bridging section" in {
        document.mainContent.selectHead(".govuk-details").selectNth("ul", 2).selectNth("li", 2).text shouldBe SearchSoftwarePageContent.whatSoftwareBridgingBullet2
      }

    }

    "has a skip to results link" in {
      document.mainContent.selectHead(".govuk-skip-link").text shouldBe SearchSoftwarePageContent.skiptoresults
    }

    "have a filter section" which {
      val filterSection = getFilterSection(document)

      "has a role attribute to identify it as a search landmark" in {
        filterSection.attr("role") shouldBe "search"
      }

      "has a heading" in {
        filterSection.selectHead("h2").text shouldBe SearchSoftwarePageContent.Filters.filterHeading
      }

      "has an accessibility features section" that {
        val checkboxGroup = getCheckboxGroup(document, 1)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwarePageContent.Filters.accessibilityFeatures
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

      "has a pricing section" that {
        val checkboxGroup = getCheckboxGroup(document, 2)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwarePageContent.Filters.pricing
        }

        "contains a Free version checkbox" in {
          validateCheckboxInGroup(
            checkboxGroup,
            1,
            FreeVersion.key,
            SearchSoftwarePageContent.freeVersion,
            Some(SearchSoftwarePageContent.freeVersionHint)
          )
        }
      }

      "has an suitable for section" that {
        val checkboxGroup = getCheckboxGroup(document, 3)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwarePageContent.Filters.suitableFor
        }

        "contains a sole trader checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 1, SoleTrader.key, SearchSoftwarePageContent.soleTrader)
        }

        "contains a UK property checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 2, UkProperty.key, SearchSoftwarePageContent.ukProperty)
        }
        //      test for foreign property checkbox in the filters section
        //        "contains an overseas property checkbox" in {
        //          validateCheckboxInGroup(checkboxGroup, 3, OverseasProperty.key, SearchSoftwarePageContent.overseasProperty)
        //        }
      }

      "has a compatible with section" that {
        val checkboxGroup = getCheckboxGroup(document, 4)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwarePageContent.Filters.operatingSystem
        }

        "contains an Microsoft Windows checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 1, MicrosoftWindows.key, SearchSoftwarePageContent.microsoftWindows)
        }

        "contains an Mac OS checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 2, MacOS.key, SearchSoftwarePageContent.macOS)
        }
      }

      "has a mobile app section" that {
        val checkboxGroup = getCheckboxGroup(document, 5)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwarePageContent.Filters.mobileApp
        }

        "contains an Android checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 1, Android.key, SearchSoftwarePageContent.android)
        }

        "contains an Apple iOS checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 2, AppleIOS.key, SearchSoftwarePageContent.appleIOS)
        }
      }

      "has a software type section" that {
        val checkboxGroup = getCheckboxGroup(document, 6)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwarePageContent.Filters.softwareType
        }

        "contains an BrowserBased checkbox" in {
          validateCheckboxInGroup(
            checkboxGroup,
            1,
            BrowserBased.key,
            SearchSoftwarePageContent.browserBased,
            Some(SearchSoftwarePageContent.browserBasedHint)
          )
        }

        "contains an ApplicationBased checkbox" in {
          validateCheckboxInGroup(
            checkboxGroup,
            2,
            ApplicationBased.key,
            SearchSoftwarePageContent.applicationBased,
            Some(SearchSoftwarePageContent.applicationBasedHint)
          )
        }
      }

      "has a software for section" that {
        val checkboxGroup = getCheckboxGroup(document, 7)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwarePageContent.Filters.softwareFor
        }

        "contains a RecordKeeping checkbox" in {
          validateCheckboxInGroup(
            checkboxGroup,
            1,
            RecordKeeping.key,
            SearchSoftwarePageContent.recordKeeping,
            Some(SearchSoftwarePageContent.recordKeepingHint)
          )
        }

        "contains a Bridging checkbox" in {
          validateCheckboxInGroup(
            checkboxGroup,
            2,
            Bridging.key,
            SearchSoftwarePageContent.bridging,
            Some(SearchSoftwarePageContent.bridgingHint)
          )
        }
      }

      "has a business type section" that {
        val checkboxGroup = getCheckboxGroup(document, 8)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwarePageContent.Filters.businessType
        }

        "contains an Individual checkbox and hint" in {
          validateCheckboxInGroup(checkboxGroup, 1, Individual.key, SearchSoftwarePageContent.individual, Some(SearchSoftwarePageContent.individualHint))
        }

        "contains an Agent checkbox and hint" in {
          validateCheckboxInGroup(checkboxGroup, 2, Agent.key, SearchSoftwarePageContent.agent, Some(SearchSoftwarePageContent.agentHint))
        }
      }

      "has a software compatibility section" that {
        val checkboxGroup = getCheckboxGroup(document, 9)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwarePageContent.Filters.softwareCompatibility
        }

        "contains an VAT checkbox" in {
          validateCheckboxInGroup(
            checkboxGroup,
            1,
            Vat.key,
            SearchSoftwarePageContent.vat,
            Some(SearchSoftwarePageContent.vatHint)
          )
        }

        "contains an Income Tax checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 2, "", SearchSoftwarePageContent.incomeTax, disabled = true, checked = true)
        }
      }

      "has a language section" that {
        val checkboxGroup = getCheckboxGroup(document, 10)

        "contains a Welsh checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 1, Welsh.key, SearchSoftwarePageContent.welsh)
        }

        "contains an English checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 2, "", SearchSoftwarePageContent.english, disabled = true, checked = true)
        }

      }

      "has a apply button section" that {
        "contains an apply filters button" in {
          filterSection.selectHead(".apply-filters-button").text shouldBe SearchSoftwarePageContent.Filters.applyFilters
        }

      }
    }

    "has a search section" which {
      "contains a heading" in {
        document.mainContent.selectNth(".filters-section", 1).selectHead("h2").text shouldBe SearchSoftwarePageContent.SearchSoftwareSection.searchFormHeading
      }

      "contains a text input" in {
        val input: Element = document.mainContent.selectHead("#searchTerm")
        input.attr("name") shouldBe "searchTerm"
        input.attr("role") shouldBe "search"
        input.attr("aria-label") shouldBe SearchSoftwarePageContent.SearchSoftwareSection.searchFormHeading

        document.mainContent.selectHead("#searchTerm").attr("value") shouldBe "search test"
      }

      "contains a submit" in {
        document.mainContent.selectHead("#searchButton").text shouldBe SearchSoftwarePageContent.SearchSoftwareSection.searchFormHeading
      }

    }

    "have the last updated date" in {
      document.mainContent.selectHead("#lastUpdated").text shouldBe SearchSoftwarePageContent.lastUpdate
    }

    "displays a message when the list is empty" which {
      val emptyListMessage = document.mainContent.selectHead("#vendor-count")

      "contains a header" in {
        emptyListMessage.selectHead("h2").text shouldBe SearchSoftwarePageContent.emptyVendorListMessageHeading
      }

      "contains a paragraph" in {
        emptyListMessage.selectHead("p").text shouldBe SearchSoftwarePageContent.emptyVendorListMessageParagraph
      }

      "contains two bullet points" in {
        emptyListMessage.selectNth("ul li", 1).text shouldBe SearchSoftwarePageContent.emptyVendorListMessageBullet1
        emptyListMessage.selectNth("ul li", 2).text shouldBe SearchSoftwarePageContent.emptyVendorListMessageBullet2
      }
    }


    "have a software vendor section" which {
      lazy val documentWithVendors = getDocument(hasResults = true, hasError = false)
      lazy val softwareVendorsSection = getSoftwareVendorsSection(documentWithVendors)

      "has a header" in {
        softwareVendorsSection.selectNth("h2", 1).text shouldBe SearchSoftwarePageContent.vendorsHeading
      }

      "has a count of the number of software vendors on the page" in {
        softwareVendorsSection.selectNth("p", 1).text shouldBe SearchSoftwarePageContent.numberOfVendors
      }

      "has a list of software vendors" which {

        "has a software vendor with lots of detail" which {
          def firstVendor: Element = softwareVendorsSection.selectHead("#software-vendor-0")

          val firstModel = SearchSoftwarePageContent.softwareVendorsResults.vendors.head

          "has a heading for the software vendor" in {
            val heading: Element = firstVendor.selectHead("h3")
            heading.text shouldBe firstModel.name
          }

          "has a link for the software vendor" in {
            val link: Element = firstVendor.selectHead("a")
            val expectedUrl = ProductDetailsController.show(URLEncoder.encode(firstModel.name, "UTF-8")).url

            link.attr("href") shouldBe expectedUrl
            link.text should include(firstModel.name)
          }

          "has a list of detail for the software vendor with full detail" in {
            val summaryList: Element = firstVendor.selectHead("dl")

            val firstRow = summaryList.selectNth("div", 1)
            firstRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.pricing
            firstRow.selectHead("dd").text shouldBe SearchSoftwarePageContent.freeVersion

            val secondRow = summaryList.selectNth("div", 2)
            secondRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.softwareFor
            secondRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.recordKeeping}, ${SearchSoftwarePageContent.bridging}"

            val thirdRow = summaryList.selectNth("div", 3)
            thirdRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.suitableFor
            thirdRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.soleTrader}, ${SearchSoftwarePageContent.ukProperty}"
          }
        }

        "has a software vendor with minimal detail" which {
          def secondVendor: Element = softwareVendorsSection.selectHead("#software-vendor-1")

          val secondModel = SearchSoftwarePageContent.softwareVendorsResults.vendors(1)

          "has a heading for the software vendor" in {
            val heading: Element = secondVendor.selectHead("h3")
            heading.text shouldBe secondModel.name
          }

          "has a link for the software vendor" in {
            val link: Element = secondVendor.selectHead("a")
            val expectedUrl = ProductDetailsController.show(URLEncoder.encode(secondModel.name, "UTF-8")).url

            link.attr("href") shouldBe expectedUrl
            link.text should include(secondModel.name)
          }

          "has a list of detail for the software vendor with minimal detail" in {
            val summaryList: Element = secondVendor.selectHead("dl")

            val firstRow: Element = summaryList.selectNth("div", 1)
            firstRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.pricing
            firstRow.selectHead("dd").text shouldBe SearchSoftwarePageContent.noFreeVersion

            summaryList.selectOptionally("div:nth-of-type(4)") shouldBe None
          }
        }
      }
    }
  }
}

object SearchSoftwareViewSpec extends ViewSpec {

  def page(softwareVendors: SoftwareVendors, hasError: Boolean, beta: Boolean, pricing: Boolean, overseas: Boolean): HtmlFormat.Appendable =
    searchSoftwarePage(
      softwareVendors,
      if (hasError) {
        FiltersForm.form.withError(testFormError)
      } else {
        FiltersForm.form.fill(FiltersFormModel(Some("search test")))
      },
      Call("POST", "/test-url"),
      beta,
      pricing,
      overseas
    )

  def getDocument(hasResults: Boolean, hasError: Boolean): Document = {
    val results = if (hasResults) SearchSoftwarePageContent.softwareVendorsResults else SearchSoftwarePageContent.softwareVendorsNoResults
    Jsoup.parse(page(results, hasError, false, false, false).body)
  }

  def getCheckboxItem(checkboxGroup: Element, n: Int): Element = checkboxGroup
    .selectNth(".govuk-checkboxes__item", n)

  def getCheckboxInput(checkboxItem: Element): Element = checkboxItem
    .selectHead(".govuk-checkboxes__input")

  def getCheckboxLabel(checkboxItem: Element): Element = checkboxItem
    .selectHead(".govuk-checkboxes__label")

  def getCheckboxHint(checkboxItem: Element): Element = checkboxItem
    .selectHead(".govuk-checkboxes__hint")

  def validateCheckboxInGroup(
                               checkboxGroup: Element,
                               n: Int,
                               value: String,
                               label: String,
                               maybeHint: Option[String] = None,
                               name: String = s"${FiltersForm.filters}[]",
                               disabled: Boolean = false,
                               checked: Boolean = false): Assertion = {
    val checkboxItem = getCheckboxItem(checkboxGroup, n)
    getCheckboxLabel(checkboxItem).text shouldBe label
    maybeHint.map { hint =>
      getCheckboxHint(checkboxItem).text shouldBe hint
    }

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

  def getCheckboxGroup(document: Document, n: Int): Element = {
    getFilterSection(document)
      .selectNth(".govuk-form-group", n)
      .selectNth(".govuk-fieldset", 1)
  }

  def getFilterSection(document: Document): Element = document.mainContent.selectHead("#software-section").selectNth(".filters-section", 2)

  private val searchSoftwarePage = app.injector.instanceOf[SearchSoftwarePage]
  private val testFormError: FormError = FormError(FiltersForm.searchTerm, "test error message")

}

private object SearchSoftwarePageContent {
  val title = "Find software for Making Tax Digital for Income Tax"
  val lastUpdate = "This page was last updated: 2 Dec 2022"
  val heading = "Find software for Making Tax Digital for Income Tax"
  val paragraph1 = "HMRC does not make recommendations for software. However, all software on this page has passed our recognition criteria."
  val insetText = "If you need help to choose software, contact the software provider or your tax agent before making a decision."
  val whatSoftwareHeading = "What kind of software is available?"
  val whatSoftwareRecordKeepingHeading = "Record Keeping Software"
  val whatSoftwareRecordKeepingBullet1 = "updates and stores your records digitally"
  val whatSoftwareRecordKeepingBullet2 = "works directly with HMRC systems allowing you to file a Income tax"
  val whatSoftwareBridgingHeading = "Bridging software"
  val whatSoftwareBridgingBullet1 = "works with non-compatible software like spreadsheets, accounting systems and other digital bookkeeping products"
  val whatSoftwareBridgingBullet2 = "lets you send the required information digitally to HMRC in the correct format"
  val skiptoresults = "Skip to results."

  object SearchSoftwareSection {
    val searchFormHeading = "Search by software name"
  }

  object Filters {
    val filterHeading = "Filters"
    val pricing = "Pricing"
    val suitableFor = "Suitable for"
    val businessType = "Business type"
    val operatingSystem = "Operating system"
    val mobileApp = "Mobile app"
    val softwareType = "Software type"
    val softwareFor = "Software for"
    val softwareCompatibility = "Making Tax Digital Compatibility"
    val language = "Language"
    val accessibilityFeatures = "Accessibility features"
    val applyFilters = "Apply filters"
  }

  val vendorsHeading = "These single products are compatible software that meets all your needs"
  val numberOfVendors = "There are 2 software providers that can send quarterly updates, submit tax return and meet your selected requirements."

  val emptyVendorListMessage = "Your search has returned no results. To increase the number of results, we suggest you:"
  val emptyVendorListMessageHeading = "Your search has returned no results."
  val emptyVendorListMessageParagraph = "To increase the number of results, we suggest you:"
  val emptyVendorListMessageBullet1 = "reduce the number of filters you apply"
  val emptyVendorListMessageBullet2 = "make sure the name you have entered into the search bar is correct"

  val pricing = "Pricing"
  val freeTrial = "Free trial"
  val freeVersion = "Free version"
  val freeVersionHint = "These are usually only free for a limited time, or have restricted features"
  val paidFor = "Paid for"
  val noFreeTrial = "No free trial"
  val noFreeVersion = "No free version"

  val suitableFor = "Business income sources"
  val soleTrader = "Sole trader"
  val ukProperty = "UK property"
  val overseasProperty = "Overseas property"

  val businessType = "Business type:"
  val individual = "Individual"
  val individualHint = "Suitable for people submitting their own records"
  val agent = "Agent"
  val agentHint = "Software for accountants and bookkeepers"

  val operatingSystem = "Operating system:"
  val microsoftWindows = "Microsoft Windows"
  val macOS = "Mac OS"

  val softwareFor = "Type of software"
  val recordKeeping = "Record keeping"
  val recordKeepingHint = "Software to store and submit your tax records"
  val bridging = "Bridging"
  val bridgingHint = "Submit records with selected non-compatible software, like spreadsheets"

  val submissionType = "Submission type"
  val quarterlyUpdates = "Quarterly updates"
  val taxReturn = "Self Assessment tax return"

  val mobileApp = "Mobile app:"
  val android = "Android"
  val appleIOS = "Apple iOS"

  val softwareType = "Software type:"
  val browserBased = "Browser based"
  val browserBasedHint = "Software accessed online with your internet browser"
  val applicationBased = "Application based"
  val applicationBasedHint = "Software installed on your device"

  val softwareCompatibility = "Making Tax Digital Compatibility:"
  val vat = "VAT"
  val vatHint = "Software which you can also use to submit VAT records"
  val incomeTax = "Income Tax"

  val language = "Language:"
  val welsh = "Welsh"
  val english = "English"

  val accessibility = "Accessibility:"
  val visual = "Impaired vision and blindness"
  val hearing = "Deafness and impaired hearing"
  val motor = "Motor difficulties"
  val cognitive = "Cognitive impairments"

  private val lastUpdateTest = LocalDate.of(2022, 12, 2)

  val softwareVendorsResults: SoftwareVendors = SoftwareVendors(
    lastUpdated = lastUpdateTest,
    vendors = Seq(
      SoftwareVendorModel(
        name = "test software vendor one",
        email = Some("test@software-vendor-name-one.com"),
        phone = Some("11111 111 111"),
        website = "software-vendor-name-one.com",
        filters = Seq(
          VendorFilter.FreeVersion,
          VendorFilter.FreeTrial,
          VendorFilter.PaidFor,
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
          VendorFilter.RecordKeeping,
          VendorFilter.Bridging,
          VendorFilter.Vat,
          VendorFilter.Welsh,
          VendorFilter.Visual,
          VendorFilter.Hearing,
          VendorFilter.Motor,
          VendorFilter.Cognitive
        ),
        incomeAndDeductions = Seq.empty[IncomeAndDeduction]
      ),
      SoftwareVendorModel(
        name = "test software vendor two",
        email = Some("test@software-vendor-name-two.com"),
        phone = Some("22222 222 222"),
        website = "software-vendor-name-two.com",
        filters = Seq.empty[VendorFilter],
        incomeAndDeductions = Seq.empty[IncomeAndDeduction]
      )
    )
  )

  val softwareVendorsNoResults: SoftwareVendors = SoftwareVendors(lastUpdated = lastUpdateTest, vendors = Seq.empty[SoftwareVendorModel])
}
