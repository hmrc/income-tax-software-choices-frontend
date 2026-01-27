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
import org.scalatest.{Assertion, BeforeAndAfterEach}
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes.ProductDetailsController
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.{Available, Intended}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.viewmodels.{SoftwareChoicesResultsViewModel, VendorSuitabilityViewModel}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwareView

import java.net.URLEncoder
import java.time.LocalDate

class SearchSoftwareWithIntentViewSpec extends ViewSpec with BeforeAndAfterEach {

  import SearchSoftwareWithIntentViewSpec._

  private def testRow(summaryList: Element, index: Int, key: String, value: String) = {
    val row = summaryList.selectNth("div", index)
    row.selectHead("dt").text shouldBe key
    row.selectHead("dd").text shouldBe value
  }

  private def agentTestCardOne(vendor: Element) = {
    val summaryList = vendor.selectHead("dl")
    testRow(summaryList, 1, SearchSoftwareWithIntentPageContent.pricing, SearchSoftwareWithIntentPageContent.freeVersion)
    testRow(summaryList, 2, SearchSoftwareWithIntentPageContent.softwareFor, s"${SearchSoftwareWithIntentPageContent.recordKeeping}, ${SearchSoftwareWithIntentPageContent.bridging}")
    testRow(summaryList, 3, SearchSoftwareWithIntentPageContent.submissionType, s"${SearchSoftwareWithIntentPageContent.quarterlyUpdates}, ${SearchSoftwareWithIntentPageContent.taxReturn}")
    testRow(summaryList, 4, SearchSoftwareWithIntentPageContent.incomeSources, s"${SearchSoftwareWithIntentPageContent.soleTrader}, ${SearchSoftwareWithIntentPageContent.ukProperty}, ${SearchSoftwareWithIntentPageContent.overseasProperty}")
  }

  private def agentTestCardTwo(vendor: Element) = {
    val summaryList = vendor.selectHead("dl")
    testRow(summaryList, 1, SearchSoftwareWithIntentPageContent.pricing, SearchSoftwareWithIntentPageContent.noFreeVersion)
  }

  private def agentTestCardFour(vendor: Element) = {
    val summaryList = vendor.selectHead("dl")
    testRow(summaryList, 1, SearchSoftwareWithIntentPageContent.pricing, SearchSoftwareWithIntentPageContent.noFreeVersion)
    testRow(summaryList, 2, SearchSoftwareWithIntentPageContent.submissionType, SearchSoftwareWithIntentPageContent.quarterlyUpdates)
    testRow(summaryList, 3, SearchSoftwareWithIntentPageContent.incomeSources, SearchSoftwareWithIntentPageContent.soleTrader)
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

  private def testCardFour(vendor: Element) = {
    val summaryList = vendor.selectHead("dl")
    testRow(summaryList, 1, SearchSoftwareWithIntentPageContent.pricing, SearchSoftwareWithIntentPageContent.noFreeVersion)
    testRow(summaryList, 2, SearchSoftwareWithIntentPageContent.incomeSources, SearchSoftwareWithIntentPageContent.soleTrader)
    testRow(summaryList, 3, SearchSoftwareWithIntentPageContent.quarterlyUpdates, s"${SearchSoftwareWithIntentPageContent.readyNow}")
    testRow(summaryList, 4, SearchSoftwareWithIntentPageContent.taxReturn, s"${SearchSoftwareWithIntentPageContent.notIncluded}")
  }

  "Search software page must have a filter section" which {
    lazy val document = {
      val model = SoftwareChoicesResultsViewModel(
        vendorsWithIntent = SearchSoftwareWithIntentPageContent.multipleVendorsWithIntent
      )
      Jsoup.parse(page(model).body)
    }
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

  "Search software page" when {
    "user is an individual" must {
      lazy val documentManyResults = {
        val model = SoftwareChoicesResultsViewModel(
          vendorsWithIntent = SearchSoftwareWithIntentPageContent.multipleVendorsWithIntent
        )
        Jsoup.parse(page(model).body)
      }
      lazy val documentOneResult = {
        val model = SoftwareChoicesResultsViewModel(
          vendorsWithIntent = SearchSoftwareWithIntentPageContent.singleVendorWithIntent(quarterlyReady = true, eoyReady = true)
        )
        Jsoup.parse(page(model).body)
      }
      lazy val documentNoResults = {
        val model = SoftwareChoicesResultsViewModel(
          vendorsWithIntent = Seq.empty
        )
        Jsoup.parse(page(model).body)
      }
      "have the correct title and h1" when {
        "there are many results" in {
          documentManyResults.title shouldBe SearchSoftwareWithIntentPageContent.title(4)
          documentManyResults.mainContent.selectHead("h1").text shouldBe SearchSoftwareWithIntentPageContent.heading(4)
        }
        "there is one results" in {
          documentOneResult.title shouldBe SearchSoftwareWithIntentPageContent.titleOne
          documentOneResult.mainContent.selectHead("h1").text shouldBe SearchSoftwareWithIntentPageContent.headingOne
        }
        "there are 0 results" in {
          documentNoResults.title shouldBe SearchSoftwareWithIntentPageContent.title(0)
          documentNoResults.mainContent.selectHead("h1").text shouldBe SearchSoftwareWithIntentPageContent.heading(0)
        }
      }
      "have paragraph" in {
        documentManyResults.mainContent.selectNth("p", 1).text shouldBe SearchSoftwareWithIntentPageContent.paragraph
      }
      "have a second paragraph" in {
        documentManyResults.mainContent.selectNth("p", 2).text shouldBe SearchSoftwareWithIntentPageContent.paragraphTwo
      }
      "have a button to Change answers" in {
        documentManyResults.mainContent.selectHead(".govuk-button.govuk-button--secondary").text shouldBe SearchSoftwareWithIntentPageContent.changeAnswers
      }
      "have a single software vendor section for result" which {
        "has the correct heading" when {
          "there are multiple results" in {
            documentManyResults.selectHead("#vendor-count h2").text shouldBe SearchSoftwareWithIntentPageContent.intentHeadingMany
          }
          "there is 1 result" in {
            documentOneResult.selectHead("#vendor-count h2").text shouldBe SearchSoftwareWithIntentPageContent.intentHeadingOne
          }
          "there are 0 results" in {
            documentNoResults.selectHead("#vendor-count h2").text shouldBe SearchSoftwareWithIntentPageContent.intentHeadingNone
          }
        }

        "has a list of software vendors" which {
          "has a software vendor with lots of detail in Ready now status" which {
            def firstVendor: Element = documentManyResults.selectHead("#software-vendor-0")

            val firstModel = SearchSoftwareWithIntentPageContent.softwareVendorsResults.vendors.head

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
              testCardOne(firstVendor)
            }
          }

          "has a software vendor with minimal detail and In Development status" which {
            def secondVendor: Element = documentManyResults.selectHead("#software-vendor-1")

            val secondModel = SearchSoftwareWithIntentPageContent.softwareVendorsResults.vendors(1)

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
              testCardTwo(secondVendor)
            }
          }

          "has a software vendor with Not included status" which {
            def thirdVendor: Element = documentManyResults.selectHead("#software-vendor-2")

            val thirdModel = SearchSoftwareWithIntentPageContent.multipleVendorsWithIntent(2).vendor

            "has a heading for the software vendor" in {
              val heading: Element = thirdVendor.selectHead("h3")
              heading.text shouldBe thirdModel.name
            }

            "has a link for the software vendor" in {
              val link: Element = thirdVendor.selectHead("a")
              val expectedUrl = ProductDetailsController.show(URLEncoder.encode(thirdModel.name, "UTF-8")).url

              link.attr("href") shouldBe expectedUrl
              link.text should include(thirdModel.name)
            }

            "has a list of detail for the software vendor with minimal detail" in {
              testCardThree(thirdVendor)
            }
          }

          "pricing, type of software and income sources in card detail only display features available now" in {
            def fourthVendor: Element = documentManyResults.selectHead("#software-vendor-3")

            testCardFour(fourthVendor)
          }
        }
      }
      "display the exit survey link" in {
        val link = documentManyResults.mainContent.select(".govuk-link").get(0)
        link.text shouldBe SearchSoftwareWithIntentPageContent.exitSurveyLinkTitle
        link.attr("href") shouldBe SearchSoftwareWithIntentPageContent.exitSurveyLink
      }
    }
    "user is an agent" must {
      lazy val documentAgentMany = {
        val model = SoftwareChoicesResultsViewModel(
          vendorsWithIntent = SearchSoftwareWithIntentPageContent.multipleVendorsWithIntent,
          isAgent = true
        )
        Jsoup.parse(page(model).body)
      }
      lazy val documentAgentOneResult = {
        val model = SoftwareChoicesResultsViewModel(
          vendorsWithIntent = SearchSoftwareWithIntentPageContent.singleVendorWithIntent(quarterlyReady = true, eoyReady = true),
          isAgent = true
        )
        Jsoup.parse(page(model).body)
      }
      lazy val documentAgentNoResults = {
        val model = SoftwareChoicesResultsViewModel(
          vendorsWithIntent = Seq.empty,
          isAgent = true
        )
        Jsoup.parse(page(model).body)
      }

      "have the correct title and h1" when {
        "there are many results" in {
          documentAgentMany.title shouldBe SearchSoftwareWithIntentPageContent.title(4)
          documentAgentMany.mainContent.selectHead("h1").text shouldBe SearchSoftwareWithIntentPageContent.heading(4)
        }
        "there is one results" in {
          documentAgentOneResult.title shouldBe SearchSoftwareWithIntentPageContent.titleOne
          documentAgentOneResult.mainContent.selectHead("h1").text shouldBe SearchSoftwareWithIntentPageContent.headingOne
        }
        "there are 0 results" in {
          documentAgentNoResults.title shouldBe SearchSoftwareWithIntentPageContent.title(0)
          documentAgentNoResults.mainContent.selectHead("h1").text shouldBe SearchSoftwareWithIntentPageContent.heading(0)
        }
      }
      "have introductory text for agents" which {
        "has first paragraph" in {
          documentAgentMany.mainContent.selectNth("p", 1).text shouldBe SearchSoftwareWithIntentPageContent.agentPara1
        }
        "has second paragraph" in {
          documentAgentMany.mainContent.selectNth("p", 2).text shouldBe SearchSoftwareWithIntentPageContent.agentPara2
        }
        "has third paragraph" in {
          documentAgentMany.mainContent.selectNth("p", 3).text shouldBe SearchSoftwareWithIntentPageContent.agentPara3
        }
        "has inset text paragraph" in {
          documentAgentMany.mainContent.selectHead(".govuk-inset-text").text shouldBe SearchSoftwareWithIntentPageContent.agentInset
        }
      }
      "does not have a 'Change answers' button" in {
        documentAgentMany.mainContent.selectOptionally(".govuk-button--secondary") shouldBe None
      }

      "have a single software vendor section for agents" which {

        "has the correct title and h1" when {
          "there are multiple results" in {
            documentAgentMany.selectHead("#vendor-count h2").text shouldBe SearchSoftwareWithIntentPageContent.agentHeadingMany
          }
          "there is 1 result" in {
            documentAgentOneResult.selectHead("#vendor-count h2").text shouldBe SearchSoftwareWithIntentPageContent.agentHeadingOne
          }
          "there are 0 results" in {
            documentAgentNoResults.selectHead("#vendor-count h2").text shouldBe SearchSoftwareWithIntentPageContent.agentHeadingNone
          }
        }

        "has a list of software vendors" which {
          "has a software vendor with lots of detail" which {
            def firstVendor: Element = documentAgentMany.selectHead("#software-vendor-0")

            val firstModel = SearchSoftwareWithIntentPageContent.softwareVendorsResults.vendors.head

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
              agentTestCardOne(firstVendor)
            }
          }

          "has a software vendor with minimal detail" which {
            def secondVendor: Element = documentAgentMany.selectHead("#software-vendor-1")

            val secondModel = SearchSoftwareWithIntentPageContent.softwareVendorsResults.vendors(1)

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
              agentTestCardTwo(secondVendor)
            }
          }

          "only displays features that are available now in card detail" in {
            def fourthVendor: Element = documentAgentMany.selectHead("#software-vendor-3")

            agentTestCardFour(fourthVendor)
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
  def heading(count: Int) = s"You have $count software results"
  val headingOne = "You have 1 software result"
  def title(count: Int) = s"${heading(count)} - ${PageContentBase.title} - GOV.UK"
  val titleOne = s"$headingOne - ${PageContentBase.title} - GOV.UK"
  val paragraph = "All the products showing in the filter list will allow you to submit your quarterly updates."
  val paragraphTwo = "In development means one or more features you need to complete your tax return are still being built. " +
    "We expect these features will be ready for you to do your 2026 to 2027 return."
  val changeAnswers = "Change answers"
  val exitSurveyLinkTitle = "Give feedback on this service (opens in new tab)"
  val exitSurveyLink = "http://localhost:9514/feedback/SOFTWAREMTDIT"

  object Filters {
    val filterHeading = "Filter options"
    val filterParagraph = "You can use filters to find specific software. All fields are optional."
    val clearFilters = "Clear filters"
    val pricing = "Price"
    val suitableFor = "Income sources"
    val softwareFor = "Type of software"
    val softwareCompatibility = "Making Tax Digital Compatibility"
    val accessibilityFeatures = "Accessibility features"
    val applyFilters = "Apply filters"
  }

  val intentHeadingMany = "Based on your filters and answers, we’ve found 4 results"
  val intentHeadingOne = "Based on your filters and answers, we’ve found 1 result"
  val intentHeadingNone = "Based on your filters and answers, there are no results"

  val agentHeadingMany = "Based on your answers, we’ve found 4 results"
  val agentHeadingOne = "Based on your answers, we’ve found 1 result"
  val agentHeadingNone = "Based on your answers, there are no results"

  val agentPara1 = "All of this software has been through a recognition process where HMRC checks it’s capable of filing your taxes. HMRC does not endorse or recommend any one product or software provider."
  val agentPara2 = "Some of the listed products may have free trials or free versions, but you’ll have to pay for others."
  val agentPara3 = "Some of the features you’ll need to submit your client’s tax returns are still being developed."
  val agentInset = "HMRC is not responsible for the availability of products or making sure that the product you chose meets the current and future needs of your clients. We recommend that you visit software providers’ websites to do more research before choosing a product."

  val pricing = "Price"
  val freeVersion = "Free version"
  val noFreeVersion = "Paid version"
  val freeVersionHint = "Check the company’s website for information on their pricing structure."

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

  val vat = "VAT"

  val visual = "Blindness or impaired vision"
  val hearing = "Deafness or impaired hearing"
  val motor = "Motor or physical difficulties"
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
          QuarterlyUpdates,
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
        filters = Seq(
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
    ),
    VendorSuitabilityViewModel(
      vendor = SoftwareVendorModel(
        name = "test software vendor four",
        email = Some("test@software-vendor-name-four.com"),
        phone = Some("44444 444 444"),
        website = "software-vendor-name-four.com",
        filters = Map(
          FreeVersion -> Intended,
          SoleTrader -> Available,
          UkProperty -> Intended,
          OverseasProperty -> Intended,
          RecordKeeping -> Intended,
          QuarterlyUpdates -> Available,
          TaxReturn -> Intended
        )
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
