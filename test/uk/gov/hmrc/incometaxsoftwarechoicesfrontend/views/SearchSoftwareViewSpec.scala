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
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes.ProductDetailsController
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.viewmodels.SoftwareChoicesResultsViewModel
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwarePage

import java.net.URLEncoder
import java.time.LocalDate

class SearchSoftwareViewSpec extends ViewSpec {

  import SearchSoftwareViewSpec._

  "Search software page" must {
    lazy val document = getDocument(hasResults = false, hasError = false)

    "have a title" in {
      document.title shouldBe s"""${SearchSoftwarePageContent.title} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"""
    }

    "have a heading" in {
      document.mainContent.selectHead("h1").text shouldBe SearchSoftwarePageContent.heading
    }

    "have paragraph" in {
      document.mainContent.selectNth("p", 1).text shouldBe SearchSoftwarePageContent.paragraph
    }

    "have a filter section" which {
      val filterSection = getFilterSection(document)

      "has a role attribute to identify it as a search landmark" in {
        filterSection.attr("role") shouldBe "search"
      }

      "has a heading" in {
        filterSection.selectHead("h2").text shouldBe SearchSoftwarePageContent.Filters.filterHeading
      }

      "has a paragraph" in {
        filterSection.selectHead("p").text shouldBe SearchSoftwarePageContent.Filters.filterParagraph
      }

      "has a clear filters link" in {
        filterSection.selectHead("a").text shouldBe SearchSoftwarePageContent.Filters.clearFilters
      }

      "has an accessibility features section" that {
        val checkboxGroup = getCheckboxGroup(document, 4)

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
        val checkboxGroup = getCheckboxGroup(document, 1)

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

      "has a software for section" that {
        val checkboxGroup = getCheckboxGroup(document, 2)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwarePageContent.Filters.softwareFor
        }

        "contains a Bridging checkbox" in {
          validateCheckboxInGroup(
            checkboxGroup,
            1,
            Bridging.key,
            SearchSoftwarePageContent.bridging,
            Some(SearchSoftwarePageContent.bridgingHint)
          )
        }
      }

      "has a software compatibility section" that {
        val checkboxGroup = getCheckboxGroup(document, 3)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwarePageContent.Filters.softwareCompatibility
        }

        "contains an VAT checkbox" in {
          validateCheckboxInGroup(
            checkboxGroup,
            1,
            Vat.key,
            SearchSoftwarePageContent.vat,
            None
          )
        }
      }

      "has a apply button section" that {
        "contains an apply filters button" in {
          filterSection.selectHead(".apply-filters-button").text shouldBe SearchSoftwarePageContent.Filters.applyFilters
        }
      }
    }

    "have the last updated date" in {
      document.mainContent.selectHead("#lastUpdated").text shouldBe SearchSoftwarePageContent.lastUpdate
    }

    "displays a message when the list is empty" should {
      lazy val document = getDocument(hasResults = false, hasError = false)
      lazy val emptyListMessage = document.mainContent.selectHead("#vendor-count")

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
            val expectedUrl = ProductDetailsController.show(URLEncoder.encode(firstModel.name, "UTF-8"), zeroResults = false).url

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
            secondRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.bridging}"

            val thirdRow = summaryList.selectNth("div", 3)
            thirdRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.suitableFor
            thirdRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.soleTrader}, ${SearchSoftwarePageContent.ukProperty}, ${SearchSoftwarePageContent.overseasProperty}"
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
            val expectedUrl = ProductDetailsController.show(URLEncoder.encode(secondModel.name, "UTF-8"), zeroResults = false).url

            link.attr("href") shouldBe expectedUrl
            link.text should include(secondModel.name)
          }

          "has a list of detail for the software vendor with minimal detail" in {
            val summaryList: Element = secondVendor.selectHead("dl")

            val firstRow: Element = summaryList.selectNth("div", 1)
            firstRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.pricing

            summaryList.selectOptionally("div:nth-of-type(4)") shouldBe None
          }
        }
      }

      "when there are no all-in-one products" should {
        lazy val documentZeroResults = {
          val model = SoftwareChoicesResultsViewModel(
            allInOneVendors = SearchSoftwarePageContent.softwareVendorsNoResults,
            otherVendors   = SearchSoftwarePageContent.softwareVendorsResults,
            zeroResults    = true
          )
          Jsoup.parse(page(model, hasError = false).body)
        }

        "displays the zero-results header" in {
          documentZeroResults.mainContent.selectHead("#vendor-count h2").text shouldBe SearchSoftwarePageContent.noProductsHeading
        }

        "displays the results count" in {
          documentZeroResults.mainContent.selectHead("#vendor-count h3").text shouldBe SearchSoftwarePageContent.noProductsCount
        }

        "renders all available software vendors" in {
          val listings = documentZeroResults.mainContent.select("#software-vendor-list > div")
          listings.size shouldBe SearchSoftwarePageContent.softwareVendorsResults.vendors.length
        }
      }

    }
  }
}

object SearchSoftwareViewSpec extends ViewSpec {

  def page(model: SoftwareChoicesResultsViewModel, hasError: Boolean): HtmlFormat.Appendable =
    searchSoftwarePage(
      model,
      FiltersForm.form.fill(FiltersFormModel()),
      Call("POST", "/test-url"),
      Call("POST", "/test-url"),
      "/test-back-url"
    )

  def getDocument(hasResults: Boolean, hasError: Boolean): Document = {
    val results = if (hasResults) SearchSoftwarePageContent.softwareVendorsResults else SearchSoftwarePageContent.softwareVendorsNoResults
    val model = SoftwareChoicesResultsViewModel(allInOneVendors = results)
    Jsoup.parse(page(model, hasError).body)
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

  def getFilterSection(document: Document): Element = document.mainContent.selectHead("#software-section").selectNth(".filters-section", 1)

  private val searchSoftwarePage = app.injector.instanceOf[SearchSoftwarePage]

}

private object SearchSoftwarePageContent {
  val title = "Software for Making Tax Digital for Income Tax"
  val lastUpdate = "This page was last updated: 2 Dec 2022"
  val heading = "Software for Making Tax Digital for Income Tax"
  val paragraph = "All of this software has been through a recognition process where HMRC checks it’s capable of filing your taxes. HMRC does not endorse or recommend any one product or software provider."

  object SearchSoftwareSection {
    val searchFormHeading = "Search by software name"
  }

  object Filters {
    val filterHeading = "Filter options"
    val filterParagraph = "You can use filters to find specific software. All fields are optional."
    val clearFilters = "Clear filters"
    val pricing = "Price"
    val suitableFor = "Business income sources"
    val softwareFor = "Type of software"
    val softwareCompatibility = "Making Tax Digital Compatibility"
    val accessibilityFeatures = "Accessibility features"
    val accountingPeriod = "Accounting period"
    val personalIncomeSources = "Personal income sources"
    val deductions = "Deductions"
    val pensions = "Pensions"
    val allowances = "Allowances"
    val applyFilters = "Apply filters"
  }

  val vendorsHeading = "These single products are compatible software that meets all your needs"
  val numberOfVendors = "There are 2 software providers that can send quarterly updates, submit tax return and meet your selected requirements."

  val emptyVendorListMessage = "Your search has returned no results. To increase the number of results, we suggest you:"
  val emptyVendorListMessageHeading = "Your search has returned no results."
  val emptyVendorListMessageParagraph = "To increase the number of results, we suggest you:"
  val emptyVendorListMessageBullet1 = "reduce the number of filters you apply"
  val emptyVendorListMessageBullet2 = "make sure the name you have entered into the search bar is correct"

  val noProductsHeading = "You’ll need to combine several of these pieces of software to fully complete your quarterly updates and tax return"
  val noProductsCount = "2 results found"

  val pricing = "Price"
  val freeVersion = "Free version"
  val freeVersionHint = "Check the company’s website for information on their pricing structure."

  val suitableFor = "Business income sources"
  val soleTrader = "Self-employment"
  val ukProperty = "UK property"
  val overseasProperty = "Foreign property"

  val softwareFor = "Type of software"
  val recordKeeping = "Record keeping"
  val recordKeepingHint = "Creates digital records"
  val bridging = "Bridging"
  val bridgingHint = "Connects to digital records like spreadsheets"

  val submissionType = "Submission type"
  val quarterlyUpdates = "Quarterly updates"
  val taxReturn = "Self Assessment tax return"

  val softwareCompatibility = "Making Tax Digital Compatibility:"
  val vat = "VAT"

  val accessibility = "Accessibility:"
  val visual = "Blindness or impaired vision"
  val hearing = "Deafness or impaired hearing"
  val motor = "Motor or physical difficulties"
  val cognitive = "Cognitive impairments"

  val accountingPeriod = "Accounting period"
  val apr6 = "6th April to 5th April"
  val apr1 = "1st April to 31st March"
  val apr1Hint = "This supports calendar update periods"

  val personalIncomeSources = "Personal income sources"
  val constructionIndustryScheme = "Construction Industry Scheme (CIS)"
  val capitalGainsTax = "Capital Gains Tax"
  val employment = "Employment"
  val foreignIncome = "Foreign Income"
  val ukDividends = "UK Dividends"
  val ukInterest = "UK Interest"
  val foreignDividends = "Foreign Dividends"
  val foreignInterest = "Foreign Interest"


  val Deductions = "Deductions"
  val charitableGiving = "Charitable giving"
  val highIncomeChildBenefit = "High Income Child Benefit Charge"
  val studentLoans = "Student loans"
  val volunteer = "Voluntary Class 2 National Insurance"

  val pensions = "Pensions"
  val statePension = "State pension income"
  val privatePension = "Private pension income"
  val payments = "Payments into a private pension"

  val allowances = "Allowances"
  val marriageAllowace = "Marriage Allowance"

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
          VendorFilter.SoleTrader,
          VendorFilter.UkProperty,
          VendorFilter.OverseasProperty,
          VendorFilter.RecordKeeping,
          VendorFilter.Bridging,
          VendorFilter.Vat,
          VendorFilter.Visual,
          VendorFilter.Hearing,
          VendorFilter.Motor,
          VendorFilter.Cognitive
        )
      ),
      SoftwareVendorModel(
        name = "test software vendor two",
        email = Some("test@software-vendor-name-two.com"),
        phone = Some("22222 222 222"),
        website = "software-vendor-name-two.com",
        filters = Seq.empty[VendorFilter]
      )
    )
  )

  val softwareVendorsNoResults: SoftwareVendors = SoftwareVendors(lastUpdated = lastUpdateTest, vendors = Seq.empty[SoftwareVendorModel])
}
