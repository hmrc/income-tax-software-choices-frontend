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
import org.jsoup.select.Elements
import org.scalatest.{Assertion, BeforeAndAfterEach}
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.IntentFeature
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes.ProductDetailsController
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.Available
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.viewmodels.{SoftwareChoicesResultsViewModel, VendorSuitabilityViewModel}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwareView

import java.net.URLEncoder
import java.time.LocalDate
import scala.util.Try

class SearchSoftwareWithIntentViewSpec extends ViewSpec with BeforeAndAfterEach with FeatureSwitching {

  import SearchSoftwareWithIntentViewSpec._

  private def testRow(summaryList: Element, index: Int, key: String, value: String) = {
    val row = summaryList.selectNth("div", index)
    row.selectHead("dt").text shouldBe key
    row.selectHead("dd").text shouldBe value
  }

  private def testCardOne(vendor: Element) = {
    val summaryList = vendor.selectHead("dl")
    testRow(summaryList, 1, SearchSoftwareWithIntentPageContent.pricing, SearchSoftwareWithIntentPageContent.freeVersion)
    testRow(summaryList, 2, SearchSoftwareWithIntentPageContent.softwareFor, s"${SearchSoftwareWithIntentPageContent.recordKeeping}, ${SearchSoftwareWithIntentPageContent.bridging}")
    testRow(summaryList, 3, SearchSoftwareWithIntentPageContent.submissionType, s"${SearchSoftwareWithIntentPageContent.taxReturn}")
    testRow(summaryList, 4, SearchSoftwareWithIntentPageContent.suitableFor, s"${SearchSoftwareWithIntentPageContent.soleTrader}, ${SearchSoftwareWithIntentPageContent.ukProperty}, ${SearchSoftwareWithIntentPageContent.overseasProperty}")
  }

  private def testCardTwo(vendor: Element) = {
    val summaryList = vendor.selectHead("dl")
    testRow(summaryList, 1, SearchSoftwareWithIntentPageContent.pricing, SearchSoftwareWithIntentPageContent.noFreeVersion)
    summaryList.selectOptionally("div:nth-of-type(2)") shouldBe None
  }

  override def beforeEach(): Unit = {
    enable(IntentFeature)
    super.beforeEach()
  }

    "Search software page" must {
    lazy val document = {
        val model = SoftwareChoicesResultsViewModel(
          allInOneVendors = SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
          otherVendors = SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
          vendorsWithIntent = SearchSoftwareWithIntentPageContent.multipleVendorsWithIntent(quarterlyReady = true, eoyReady = true),
          zeroResults = false
        )
        Jsoup.parse(page(model).body)
      }
    "have a title" in {
      document.title shouldBe s"""${SearchSoftwareWithIntentPageContent.title} - ${PageContentBase.title} - GOV.UK"""
    }

    "have a heading" in {
      document.mainContent.selectHead("h1").text shouldBe SearchSoftwareWithIntentPageContent.heading
    }

    "have paragraph" in {
      document.mainContent.selectNth("p", 1).text shouldBe SearchSoftwareWithIntentPageContent.paragraph
    }

    "have a second paragraph" in {
      document.mainContent.selectNth("p", 2).text shouldBe SearchSoftwareWithIntentPageContent.paragraphTwo
    }


    "have a filter section" which {
      val filterSection = getFilterSection(document)

      "has a role attribute to identify it as a search landmark" in {
        filterSection.attr("role") shouldBe "search"
      }

      "has a heading" in {
        filterSection.selectHead("h2").text shouldBe SearchSoftwareWithIntentPageContent.Filters.filterHeading
      }

      "has a paragraph" in {
        filterSection.selectHead("p").text shouldBe SearchSoftwareWithIntentPageContent.Filters.filterParagraph
      }

      "has a clear filters link" in {
        filterSection.selectHead("a").text shouldBe SearchSoftwareWithIntentPageContent.Filters.clearFilters
      }

      "has an accessibility features section" that {
        val checkboxGroup = getCheckboxGroup(document, 4)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwareWithIntentPageContent.Filters.accessibilityFeatures
        }

        "contains an Visual checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 1, Visual.key, SearchSoftwareWithIntentPageContent.visual)
        }

        "contains an Hearing checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 2, Hearing.key, SearchSoftwareWithIntentPageContent.hearing)
        }

        "contains an Motor checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 3, Motor.key, SearchSoftwareWithIntentPageContent.motor)
        }

        "contains an Cognitive checkbox" in {
          validateCheckboxInGroup(checkboxGroup, 4, Cognitive.key, SearchSoftwareWithIntentPageContent.cognitive)
        }
      }

      "has a pricing section" that {
        val checkboxGroup = getCheckboxGroup(document, 1)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwareWithIntentPageContent.Filters.pricing
        }

        "contains a Free version checkbox" in {
          validateCheckboxInGroup(
            checkboxGroup,
            1,
            FreeVersion.key,
            SearchSoftwareWithIntentPageContent.freeVersion,
            Some(SearchSoftwareWithIntentPageContent.freeVersionHint)
          )
        }
      }

      "has a software for section" that {
        val checkboxGroup = getCheckboxGroup(document, 2)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwareWithIntentPageContent.Filters.softwareFor
        }

        "contains a Bridging checkbox" in {
          validateCheckboxInGroup(
            checkboxGroup,
            1,
            Bridging.key,
            SearchSoftwareWithIntentPageContent.bridging
          )
        }
      }

      "has a software compatibility section" that {
        val checkboxGroup = getCheckboxGroup(document, 3)

        "contains a fieldset legend" in {
          checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwareWithIntentPageContent.Filters.softwareCompatibility
        }

        "contains an VAT checkbox" in {
          validateCheckboxInGroup(
            checkboxGroup,
            1,
            Vat.key,
            SearchSoftwareWithIntentPageContent.vat,
            None
          )
        }
      }

      "has a apply button section" that {
        "contains an apply filters button" in {
          filterSection.selectHead(".apply-filters-button").text shouldBe SearchSoftwareWithIntentPageContent.Filters.applyFilters
        }
      }
    }
/*
    "have a single software vendor section for zero all in one products" which {
      val titles = Map(
        1 -> "No products",
        2 -> "One product",
        3 -> "More than one product"
      )

      Map(
        1 -> SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
        2 -> SearchSoftwareWithIntentPageContent.softwareVendorsOneResult,
        3 -> SearchSoftwareWithIntentPageContent.softwareVendorsResults
      ).foreach { entry =>
        val index = entry._1
        val title = titles.getOrElse(index, "")

        lazy val documentZeroResults = {
          val model = SoftwareChoicesResultsViewModel(
            allInOneVendors = SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
            otherVendors = entry._2,
            zeroResults = true
          )
          Jsoup.parse(page(model).body)
        }

        s"$title: displays the zero-results header" in {
          Some(documentZeroResults.mainContent.selectHead("#vendor-count h2").text) shouldBe
            SearchSoftwareWithIntentPageContent.noProductsHeading.get(index)
        }

        s"$title: displays inset text" in {
          Some(documentZeroResults.mainContent.select("#vendor-count > div").text) shouldBe
            SearchSoftwareWithIntentPageContent.noProductsCount.get(index)
        }

        s"$title: renders all available software vendors if there are more than one" in {
          if (index == 3) {
            val listings = documentZeroResults.mainContent.select("#software-vendor-list > div")
            listings.size shouldBe SearchSoftwareWithIntentPageContent.softwareVendorsResults.vendors.length
          } else {
            Try {
              documentZeroResults.mainContent.select("#software-vendor-list > div")
              fail()
            }.getOrElse(succeed)
          }
        }
      }
    }
*/
      /*
    "have a single software vendor section for agents" which  {
      lazy val document = getDocument(hasResults = true, isAgent = true)

      "has the correct heading and inset text" when {
        "there are multiple results" in {
          document.selectHead("#vendor-count h2").text shouldBe SearchSoftwareWithIntentPageContent.agentHeadingMany
          document.selectHead("#vendor-count .govuk-inset-text").text shouldBe SearchSoftwareWithIntentPageContent.agentInsetTextMany
        }
        "there is 1 result" in {
          lazy val documentOneResult = {
            val model = SoftwareChoicesResultsViewModel(
              SearchSoftwareWithIntentPageContent.softwareVendorsOneResult,
              SearchSoftwareWithIntentPageContent.softwareVendorsOneResult,
              isAgent = true)
            Jsoup.parse(page(model).body)
          }
          documentOneResult.selectHead("#vendor-count h2").text shouldBe SearchSoftwareWithIntentPageContent.agentHeadingOne
          documentOneResult.selectHead("#vendor-count .govuk-inset-text").text shouldBe SearchSoftwareWithIntentPageContent.agentInsetTextOne
        }
        "there are 0 results" in {
          lazy val documentNoResults = getDocument(hasResults = false, isAgent = true)
          documentNoResults.selectHead("#vendor-count h2").text shouldBe SearchSoftwareWithIntentPageContent.agentHeadingNone
          documentNoResults.selectHead("#vendor-count .govuk-inset-text").text shouldBe SearchSoftwareWithIntentPageContent.agentInsetTextNone
        }
      }

      "has a list of software vendors" which {
        "has a software vendor with lots of detail" which {
          def firstVendor: Element = document.selectHead("#software-vendor-0")

          val firstModel = SearchSoftwareWithIntentPageContent.softwareVendorsResults.vendors.head

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
            testCardOne(firstVendor)
          }
        }

        "has a software vendor with minimal detail" which {
          def secondVendor: Element = document.selectHead("#software-vendor-1")

          val secondModel = SearchSoftwareWithIntentPageContent.softwareVendorsResults.vendors(1)

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
            testCardTwo(secondVendor)
          }
        }
      }
    }

 */
  }
}

object SearchSoftwareWithIntentViewSpec extends ViewSpec {

  def page(model: SoftwareChoicesResultsViewModel): HtmlFormat.Appendable =
    searchSoftwarePage(
      model,
      FiltersForm.form.fill(FiltersFormModel()),
      Call("POST", "/test-url"),
      Call("POST", "/test-url"),
      "/test-back-url"
    )

  def getDocument(numResults: Int,
                  isAgent: Boolean = false,
                  isQuarterlyReady: Boolean = true,
                  isEoyReady: Boolean = true): Document = {
    val results = SearchSoftwareWithIntentPageContent.getVendorResultsWithIntent(numResults)
    val model = SoftwareChoicesResultsViewModel(
      allInOneVendors = SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
      otherVendors = SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
      vendorsWithIntent = results,
      zeroResults = false,
      isAgent = isAgent)
    Jsoup.parse(page(model).body)
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

  def getTabsList(document: Document): Elements = {
    getSoftwareVendorsSection(document)
      .select("ul > li")
  }

  def getAllInOneTabContent(document: Document): Element = {
    document
      .selectHead("#all-in-one-software")
  }

  def getOtherTabContent(document: Document): Element = {
    document
      .selectHead("#other-software")
  }

  def getFilterSection(document: Document): Element = document.mainContent.selectHead("#software-section").selectNth(".filters-section", 1)

  private val searchSoftwarePage = app.injector.instanceOf[SearchSoftwareView]

}

private object SearchSoftwareWithIntentPageContent {
  val title = "Software for Making Tax Digital for Income Tax"
  val lastUpdate = "This page was last updated: 2 Dec 2022"
  val heading = "Software for Making Tax Digital for Income Tax"
  val paragraph = "All of this software has been through a recognition process where HMRC checks it’s capable of filing your taxes. HMRC does not endorse or recommend any one product or software provider."
  val paragraphTwo = "You’ll need to pay for most of the software listed, though some versions are free or have a free trial."
  val paragraphThree = "Software is in development, and more options will be available over time. This may include an all-in-one software product."

  object SearchSoftwareSection {
    val searchFormHeading = "Search by software name"
  }

  object Filters {
    val filterHeading = "Filter options"
    val filterParagraph = "You can use filters to find specific software. All fields are optional."
    val clearFilters = "Clear filters"
    val pricing = "Price"
    val suitableFor = "Income sources"
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

  val allInOneTabTitle = "All-in-one software"
  val allInOneTabHeadingMany = "Based on your answers we’ve found 2 results"
  val allInOneInsetTextMany = "Each of these software products can help you complete your quarterly updates and tax return in full."
  val allInOneTabHeadingOne = "Based on your answers we’ve found 1 result"
  val allInOneInsetTextOne = "This software product can help you complete your quarterly updates and tax return in full."
  val allInOneTabHeadingNone = "There are no matching results"
  val allInOneInsetTextNone = "Improve your results by removing filters."
  val otherTabTitle: String = "Other software"
  val otherTabHeadingMany = "Based on your answers we’ve found 2 results"
  val otherTabInsetTextMany = "You may need to combine several pieces of software to fully complete your quarterly updates and tax return."
  val otherTabHeadingOne = "Based on your answers we’ve found 1 result"
  val otherTabInsetTextOne = "There are no software products you can combine to meet your tax obligations."
  val otherTabHeadingNone = "There are no matching results"
  val otherTabInsetTextNone = "There are no software products you can combine to meet your tax obligations."

  val vendorsHeading = "These single products are compatible software that meets all your needs"
  val numberOfVendors = "There are 2 software providers that can send quarterly updates, submit tax return and meet your selected requirements."

  val emptyVendorListMessage = "Your search has returned no results. To increase the number of results, we suggest you:"
  val emptyVendorListMessageHeading = "Your search has returned no results."
  val emptyVendorListMessageParagraph = "To increase the number of results, we suggest you:"
  val emptyVendorListMessageBullet1 = "reduce the number of filters you apply"
  val emptyVendorListMessageBullet2 = "make sure the name you have entered into the search bar is correct"

  val agentHeadingMany = "We’ve found 2 results"
  val agentHeadingOne = "We’ve found 1 result"
  val agentHeadingNone = "There are no matching results."
  val agentInsetTextMany = "You can use the filter options to narrow down your search."
  val agentInsetTextOne = "You can improve your results by removing filters."
  val agentInsetTextNone = "Improve your results by removing filters."

  val noProductsHeading = Map(
    1 -> "There are no matching results.",
    2 -> "There are no software products you can combine to meet your tax obligations.",
    3 -> "Based on your answers we’ve found 2 results."
  )

  val noProductsCount = Map(
    1 -> "You may be able to improve your results by removing filters.",
    2 -> "You may be able to improve your results by removing filters.",
    3 -> "You will need to combine several pieces of software to fully complete your quarterly updates and tax return."
  )

  val pricing = "Price"
  val freeVersion = "Free version"
  val noFreeVersion = "Paid version"
  val freeVersionHint = "Check the company’s website for information on their pricing structure."

  val suitableFor = "Income sources"
  val soleTrader = "Sole trader"
  val ukProperty = "UK property"
  val overseasProperty = "Foreign property"

  val softwareFor = "Type of software"
  val recordKeeping = "Software that creates digital records"
  val bridging = "Software that connects to your records (bridging software)"

  val submissionType = "Submission type"
  val quarterlyUpdates = "Quarterly updates"
  val taxReturn = "Tax return"

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

  def getVendorResultsWithIntent(numResults: Int): Seq[VendorSuitabilityViewModel] = {
    Seq.empty
  }

  val softwareVendorsResults: SoftwareVendors = SoftwareVendors(
    lastUpdated = lastUpdateTest,
    vendors = Seq(
      SoftwareVendorModel(
        name = "test software vendor one",
        email = Some("test@software-vendor-name-one.com"),
        phone = Some("11111 111 111"),
        website = "software-vendor-name-one.com",
        filters = Seq(
          FreeVersion,
          FreeTrial,
          SoleTrader,
          UkProperty,
          OverseasProperty,
          RecordKeeping,
          Bridging,
          Vat,
          Visual,
          Hearing,
          Motor,
          Cognitive,
          TaxReturn
        ).map(vf => vf -> Available).toMap
      ),
      SoftwareVendorModel(
        name = "test software vendor two",
        email = Some("test@software-vendor-name-two.com"),
        phone = Some("22222 222 222"),
        website = "software-vendor-name-two.com",
        filters = Map.empty
      )
    )
  )

  val softwareVendorsOneResult: SoftwareVendors = SoftwareVendors(
    lastUpdated = lastUpdateTest,
    vendors = Seq(
      SoftwareVendorModel(
        name = "test software vendor one",
        email = Some("test@software-vendor-name-one.com"),
        phone = Some("11111 111 111"),
        website = "software-vendor-name-one.com",
        filters = Seq(
          FreeVersion,
          FreeTrial,
          SoleTrader,
          UkProperty,
          OverseasProperty,
          RecordKeeping,
          Bridging,
          Vat,
          Visual,
          Hearing,
          Motor,
          Cognitive,
          TaxReturn
        ).map(vf => vf -> Available).toMap
      )
    )
  )

  val softwareVendorsNoResults: SoftwareVendors = SoftwareVendors(lastUpdated = lastUpdateTest, vendors = Seq.empty[SoftwareVendorModel])

  def multipleVendorsWithIntent(quarterlyReady: Boolean, eoyReady: Boolean): Seq[VendorSuitabilityViewModel] = Seq(
    VendorSuitabilityViewModel(
      vendor = SoftwareVendorModel(
        name = "test software vendor one",
        email = Some("test@software-vendor-name-one.com"),
        phone = Some("11111 111 111"),
        website = "software-vendor-name-one.com",
        filters = Seq(
          FreeVersion,
          FreeTrial,
          SoleTrader,
          UkProperty,
          OverseasProperty,
          RecordKeeping,
          Bridging,
          Vat,
          Visual,
          Hearing,
          Motor,
          Cognitive,
          TaxReturn
        ).map(vf => vf -> Available).toMap
      ),
      quarterlyReady = Some(quarterlyReady),
      eoyReady = Some(eoyReady)
    ),
    VendorSuitabilityViewModel(
      vendor = SoftwareVendorModel(
        name = "test software vendor two",
        email = Some("test@software-vendor-two.com"),
        phone = Some("22222 222 222"),
        website = "software-vendor-name-two.com",
        filters = Seq(SoleTrader
        ).map(vf => vf -> Available).toMap
      ),
      quarterlyReady = Some(quarterlyReady),
      eoyReady = Some(eoyReady)
    )
  )
}
