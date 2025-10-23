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

import org.apache.pekko.util.ccompat.JavaConverters.ListHasAsScala
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
    testRow(summaryList, 3, SearchSoftwareWithIntentPageContent.incomeSources, s"${SearchSoftwareWithIntentPageContent.soleTrader}, ${SearchSoftwareWithIntentPageContent.ukProperty}, ${SearchSoftwareWithIntentPageContent.overseasProperty}")
    testRow(summaryList, 4, SearchSoftwareWithIntentPageContent.quarterlyUpdates, s"${SearchSoftwareWithIntentPageContent.readyNow}")
    testRow(summaryList, 5, SearchSoftwareWithIntentPageContent.taxReturn, s"${SearchSoftwareWithIntentPageContent.readyNow}")
  }

  private def testCardTwo(vendor: Element) = {
    val summaryList = vendor.selectHead("dl")
    testRow(summaryList, 1, SearchSoftwareWithIntentPageContent.pricing, SearchSoftwareWithIntentPageContent.noFreeVersion)
    testRow(summaryList, 2, SearchSoftwareWithIntentPageContent.incomeSources, s"${SearchSoftwareWithIntentPageContent.soleTrader}")
    testRow(summaryList, 3, SearchSoftwareWithIntentPageContent.quarterlyUpdates, s"${SearchSoftwareWithIntentPageContent.readyNow}")
    testRow(summaryList, 4, SearchSoftwareWithIntentPageContent.taxReturn, s"${SearchSoftwareWithIntentPageContent.inDevelopment}")
    summaryList.selectOptionally("div:nth-of-type(5)") shouldBe None
  }

  private def testCardThree(vendor: Element) = {
    val summaryList = vendor.selectHead("dl")
    testRow(summaryList, 1, SearchSoftwareWithIntentPageContent.pricing, SearchSoftwareWithIntentPageContent.freeVersion)
    testRow(summaryList, 2, SearchSoftwareWithIntentPageContent.softwareFor, s"${SearchSoftwareWithIntentPageContent.recordKeeping}")
    testRow(summaryList, 3, SearchSoftwareWithIntentPageContent.incomeSources, s"${SearchSoftwareWithIntentPageContent.soleTrader}, ${SearchSoftwareWithIntentPageContent.ukProperty}, ${SearchSoftwareWithIntentPageContent.overseasProperty}")
    testRow(summaryList, 4, SearchSoftwareWithIntentPageContent.quarterlyUpdates, s"${SearchSoftwareWithIntentPageContent.readyNow}")
    testRow(summaryList, 5, SearchSoftwareWithIntentPageContent.taxReturn, s"${SearchSoftwareWithIntentPageContent.notIncluded}")
  }

  override def beforeEach(): Unit = {
    enable(IntentFeature)
    super.beforeEach()
  }

    "Search software page" must {
      enable(IntentFeature)

    lazy val document = {
        val model = SoftwareChoicesResultsViewModel(
          allInOneVendors = SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
          otherVendors = SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
          vendorsWithIntent = SearchSoftwareWithIntentPageContent.multipleVendorsWithIntent,
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
      
    "have a single software vendor section for result" which  {
      "has the correct heading" when {
        "there are multiple results" in {
          document.selectHead("#vendor-count h2").text shouldBe SearchSoftwareWithIntentPageContent.intentHeadingMany
        }
        "there is 1 result" in {
          lazy val documentOneResult = {
            val model = SoftwareChoicesResultsViewModel(
              allInOneVendors = SearchSoftwareWithIntentPageContent.softwareVendorsOneResult,
              otherVendors = SearchSoftwareWithIntentPageContent.softwareVendorsOneResult,
              vendorsWithIntent = SearchSoftwareWithIntentPageContent.singleVendorWithIntent(quarterlyReady = true, eoyReady = true),
              zeroResults = false
            )
            Jsoup.parse(page(model).body)
          }
          documentOneResult.selectHead("#vendor-count h2").text shouldBe SearchSoftwareWithIntentPageContent.intentHeadingOne
        }
        "there are 0 results" in {
          lazy val documentNoResults = {
            val model = SoftwareChoicesResultsViewModel(
              allInOneVendors = SearchSoftwareWithIntentPageContent.softwareVendorsOneResult,
              otherVendors = SearchSoftwareWithIntentPageContent.softwareVendorsOneResult,
              vendorsWithIntent = Seq.empty,
              zeroResults = false
            )
            Jsoup.parse(page(model).body)
          }
          documentNoResults.selectHead("#vendor-count h2").text shouldBe SearchSoftwareWithIntentPageContent.intentHeadingNone
        }
      }

      "has a list of software vendors" which {
        "has a software vendor with lots of detail in Ready now status" which {
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

        "has a software vendor with minimal detail and In Development status" which {
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

        "has a software vendor with Not included status" which {
          def thirdVendor: Element = document.selectHead("#software-vendor-2")

          val thirdModel = SearchSoftwareWithIntentPageContent.multipleVendorsWithIntent(2).vendor

          "has a heading for the software vendor" in {
            val heading: Element = thirdVendor.selectHead("h3")
            heading.text shouldBe thirdModel.name
          }

          "has a link for the software vendor" in {
            val link: Element = thirdVendor.selectHead("a")
            val expectedUrl = ProductDetailsController.show(URLEncoder.encode(thirdModel.name, "UTF-8"), zeroResults = false).url

            link.attr("href") shouldBe expectedUrl
            link.text should include(thirdModel.name)
          }

          "has a list of detail for the software vendor with minimal detail" in {
            testCardThree(thirdVendor)
          }
        }
      }
    }

    "have a single software vendor section for agents" which {
      lazy val document = {
        val model = SoftwareChoicesResultsViewModel(
          allInOneVendors = SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
          otherVendors = SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
          vendorsWithIntent = SearchSoftwareWithIntentPageContent.multipleVendorsWithIntent,
          zeroResults = false, isAgent = true
        )
        Jsoup.parse(page(model).body)
      }
      "has the correct heading and no inset text" when {
        "there are multiple results" in {
          document.selectHead("#vendor-count h2").text shouldBe SearchSoftwarePageContent.agentHeadingMany
          document.select("#vendor-count .govuk-inset-text").asScala.headOption shouldBe None
        }
        "there is 1 result" in {
          lazy val documentOneResult = {
            val model = SoftwareChoicesResultsViewModel(
              allInOneVendors = SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
              otherVendors = SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
              vendorsWithIntent = SearchSoftwareWithIntentPageContent.singleVendorWithIntent(quarterlyReady = true, eoyReady = true),
              zeroResults = false, isAgent = true
            )
            Jsoup.parse(page(model).body)
          }
          documentOneResult.selectHead("#vendor-count h2").text shouldBe SearchSoftwarePageContent.agentHeadingOne
          document.select("#vendor-count .govuk-inset-text").asScala.headOption shouldBe None
        }
        "there are 0 results" in {
          lazy val documentNoResults = {
            val model = SoftwareChoicesResultsViewModel(
              allInOneVendors = SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
              otherVendors = SearchSoftwareWithIntentPageContent.softwareVendorsNoResults,
              vendorsWithIntent = Seq.empty,
              zeroResults = false, isAgent = true
            )
            Jsoup.parse(page(model).body)
          }
          documentNoResults.selectHead("#vendor-count h2").text shouldBe SearchSoftwarePageContent.agentHeadingNone
          document.select("#vendor-count .govuk-inset-text").asScala.headOption shouldBe None
        }
      }

      "has a list of software vendors" which {
        "has a software vendor with lots of detail" which {
          def firstVendor: Element = document.selectHead("#software-vendor-0")

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
            testCardOne(firstVendor)
          }
        }

        "has a software vendor with minimal detail" which {
          def secondVendor: Element = document.selectHead("#software-vendor-1")

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
            testCardTwo(secondVendor)
          }
        }
      }
    }
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

  private val searchSoftwarePage = app.injector.instanceOf[SearchSoftwareView]

}

private object SearchSoftwareWithIntentPageContent {
  val title = "Software for Making Tax Digital for Income Tax"
  val lastUpdate = "This page was last updated: 2 Dec 2022"
  val heading = "Software for Making Tax Digital for Income Tax"
  val paragraph = "All the products showing in the filter list will allow you to submit your quarterly updates."
  val paragraphTwo = "In development means one or more features you need to complete your tax return are still being built. We expect these features will be ready for you to do your 2026 to 2027 return."

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

  val vendorsHeading = "These single products are compatible software that meets all your needs"
  val numberOfVendors = "There are 2 software providers that can send quarterly updates, submit tax return and meet your selected requirements."

  val emptyVendorListMessage = "Your search has returned no results. To increase the number of results, we suggest you:"
  val emptyVendorListMessageHeading = "Your search has returned no results."
  val emptyVendorListMessageParagraph = "To increase the number of results, we suggest you:"
  val emptyVendorListMessageBullet1 = "reduce the number of filters you apply"
  val emptyVendorListMessageBullet2 = "make sure the name you have entered into the search bar is correct"

  val intentHeadingMany = "Based on your answers we’ve found 3 results"
  val intentHeadingOne = "Based on your answers we’ve found 1 result"
  val intentHeadingNone = "There are no matching results"

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
  val incomeSources = "Income sources"
  val quarterlyUpdates = "Quarterly updates"
  val taxReturn = "Tax return"
  
  val readyNow = "Ready now"
  val inDevelopment = "In development"
  val notIncluded = "Not included"

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

  val multipleVendorsWithIntent: Seq[VendorSuitabilityViewModel] = Seq(
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
      quarterlyReady = Some(true),
      eoyReady = Some(true)
    ),
    VendorSuitabilityViewModel(
      vendor = SoftwareVendorModel(
        name = "test software vendor two",
        email = Some("test@software-vendor-two.com"),
        phone = Some("22222 222 222"),
        website = "software-vendor-name-two.com",
        filters = Seq(SoleTrader,
          SoleTrader
        ).map(vf => vf -> Available).toMap
      ),
      quarterlyReady = Some(true),
      eoyReady = Some(false)
    ),
    VendorSuitabilityViewModel(
      vendor = SoftwareVendorModel(
        name = "test software vendor three",
        email = Some("test@software-vendor-name-three.com"),
        phone = Some("33333 333 333"),
        website = "software-vendor-name-three.com",
        filters = Seq(
          FreeVersion,
          FreeTrial,
          SoleTrader,
          UkProperty,
          OverseasProperty,
          RecordKeeping,
          QuarterlyUpdates
        ).map(vf => vf -> Available).toMap
      ),
      quarterlyReady = Some(true),
      eoyReady = None
    )
  )
  def singleVendorWithIntent(quarterlyReady: Boolean, eoyReady: Boolean): Seq[VendorSuitabilityViewModel] = Seq(
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
    )
  )
}
