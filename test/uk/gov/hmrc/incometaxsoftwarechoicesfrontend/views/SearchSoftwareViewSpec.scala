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
import scala.jdk.CollectionConverters._

class SearchSoftwareViewSpec extends ViewSpec {

  import SearchSoftwareViewSpec._

  "Search software page" when {
    Seq(true, false).foreach { betaFeatureSwitch =>
      s"beta is turned ${if (betaFeatureSwitch) "on" else "off"}" when {
        Seq(true, false).foreach { pricingSwitch =>
          s"extra pricing options is turned ${if (pricingSwitch) "on" else "off"}" when {
            Seq(true, false).foreach { displayOverseasProperty =>
              s"overseas property option is turned ${if (displayOverseasProperty) "on" else "off"}" when {
                Seq(true, false).foreach { hasResults =>
                  s"there ${if (hasResults) "are" else "are no"} results" when {
                    Seq(true, false).foreach { hasError =>
                      s"there ${if (hasError) "is" else "is not"} an error" should {
                        val document = getDocument(hasResults, hasError = hasError, beta = betaFeatureSwitch, pricing = pricingSwitch, overseas = displayOverseasProperty)

                        if (betaFeatureSwitch) {
                          "have a breadcrumb menu containing a final link to the guidance page" in {
                            val breadcrumbCount = document.select(".govuk-breadcrumbs__list-item").size()
                            val link = document.selectNth(".govuk-breadcrumbs__list-item", breadcrumbCount).selectHead("a")
                            link.text shouldBe "Guidance"
                            link.attr("href") shouldBe appConfig.guidance
                          }
                        } else {
                          "have a back link to the guidance page" in {
                            val link = document.selectHead(".govuk-back-link")
                            link.text shouldBe "Back"
                            link.attr("href") shouldBe appConfig.guidance
                          }
                        }

                        "have a title" in {
                          document.title shouldBe s"""${if (hasError) "Error: " else ""}${SearchSoftwarePageContent.title} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"""
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
                            val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 1)

                            if (betaFeatureSwitch) {
                              "contains a section header" in {
                                accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.accessibilityFeatures
                              }
                            }

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
                            val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 2)

                            if (betaFeatureSwitch) {
                              "contains a section header" in {
                                accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.pricing
                              }
                            }

                            "contains a fieldset legend" in {
                              checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwarePageContent.Filters.pricing
                            }

                            if (pricingSwitch) {
                              "contains a Free trial checkbox" in {
                                validateCheckboxInGroup(checkboxGroup, 1, FreeTrial.key, SearchSoftwarePageContent.freeTrial)
                              }
                            }

                            "contains a Free version checkbox" in {
                              val box = if (pricingSwitch) 2 else 1
                              validateCheckboxInGroup(
                                checkboxGroup,
                                box,
                                FreeVersion.key,
                                SearchSoftwarePageContent.freeVersion,
                                Some(SearchSoftwarePageContent.freeVersionHint)
                              )
                            }


                            if (pricingSwitch) {
                              "contains a paid for checkbox" in {
                                validateCheckboxInGroup(checkboxGroup, 3, PaidFor.key, SearchSoftwarePageContent.paidFor)
                              }
                            }
                          }

                          "has an suitable for section" that {
                            val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 3)

                            if (betaFeatureSwitch) {
                              "contains a section header" in {
                                accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.suitableFor
                              }
                            }

                            "contains a fieldset legend" in {
                              checkboxGroup.getElementsByTag("legend").text shouldBe SearchSoftwarePageContent.Filters.suitableFor
                            }

                            "contains a sole trader checkbox" in {
                              validateCheckboxInGroup(checkboxGroup, 1, SoleTrader.key, SearchSoftwarePageContent.soleTrader)
                            }

                            "contains a UK property checkbox" in {
                              validateCheckboxInGroup(checkboxGroup, 2, UkProperty.key, SearchSoftwarePageContent.ukProperty)
                            }
                            if (displayOverseasProperty) {
                              "contains an overseas property checkbox" in {
                                validateCheckboxInGroup(checkboxGroup, 3, OverseasProperty.key, SearchSoftwarePageContent.overseasProperty)
                              }
                            } else {
                              "does not contains an overseas property checkbox" in {
                                checkboxGroup.select(".govuk-checkboxes__item:nth-of-type(3)").asScala.headOption shouldBe (None)
                              }
                            }
                          }

                          "has a compatible with section" that {
                            val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 4)
                            if (betaFeatureSwitch) {
                              "contains a section header" in {
                                accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.operatingSystem
                              }
                            }

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
                            val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 5)

                            if (betaFeatureSwitch) {
                              "contains a section header" in {
                                accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.mobileApp
                              }
                            }

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
                            val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 6)

                            if (betaFeatureSwitch) {
                              "contains a section header" in {
                                accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.softwareType
                              }
                            }

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
                            val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 7)

                            if (betaFeatureSwitch) {
                              "contains a section header" in {
                                accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.softwareFor
                              }
                            }

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
                            val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 8)

                            if (betaFeatureSwitch) {
                              "contains a section header" in {
                                accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.businessType
                              }
                            }

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
                            val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 9)

                            if (betaFeatureSwitch) {
                              "contains a section header" in {
                                accordionSectionHeader.get.text shouldBe SearchSoftwarePageContent.Filters.softwareCompatibility
                              }
                            }

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
                            val (accordionSectionHeader, checkboxGroup) = getAccordionSectionHeaderAndCheckboxGroup(document, betaFeatureSwitch, 10)

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

                          "has a apply button section" that {
                            "contains an apply filters button" in {
                              filterSection.selectHead(".apply-filters-button").text shouldBe SearchSoftwarePageContent.Filters.applyFilters
                            }

                          }
                        }

                        "has a search form" which {
                          "contains a heading" in {
                            document.mainContent.selectHead("form h2 label").text shouldBe SearchSoftwarePageContent.searchFormHeading
                          }

                          "contains a text input" in {
                            val input: Element = document.mainContent.selectHead("#searchTerm")
                            input.attr("name") shouldBe "searchTerm"
                            input.attr("role") shouldBe "search"
                            input.attr("aria-label") shouldBe SearchSoftwarePageContent.searchFormHeading

                            if (!hasError) document.mainContent.selectHead("#searchTerm").attr("value") shouldBe "search test"
                          }

                          "contains a submit" in {
                            document.mainContent.selectHead("#searchButton").text shouldBe SearchSoftwarePageContent.searchFormHeading
                          }

                        }

                        "have the last updated date" in {
                          document.mainContent.selectHead("#lastUpdated").text shouldBe SearchSoftwarePageContent.lastUpdate
                        }

                        if (hasError) "contains an error" in {
                          document.mainContent.selectHead(".govuk-error-message").text shouldBe "test error message"
                        }


                        if (!hasResults) "displays a message when the list is empty" which {
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
                        } else if (betaFeatureSwitch) "have a beta software vendor section" which {
                          val softwareVendorsSection = getSoftwareVendorsSection(document)

                          "has a count of the number of software vendors on the page" in {
                            softwareVendorsSection.selectHead("#vendor-count h2").text shouldBe SearchSoftwarePageContent.numberOfVendors
                          }

                          "have a list of software vendors" which {

                            "have a line for software vendor one" which {
                              def firstVendor: Element = softwareVendorsSection.selectHead("#software-vendor-0")

                              "has a heading link for the software vendor" in {
                                val headingLink: Element = firstVendor.selectHead("h3").selectHead("a")
                                val firstVendorInModel = SearchSoftwarePageContent.softwareVendorsResults.vendors.head
                                headingLink.attr("href") shouldBe ProductDetailsController.show(URLEncoder.encode(firstVendorInModel.name, "UTF-8")).url
                                headingLink.text shouldBe s"${firstVendorInModel.name}"
                              }
                            }

                            "have a line for software vendor two" which {
                              def secondVendor: Element = softwareVendorsSection.selectHead("#software-vendor-1")

                              "has a heading link for the software vendor" in {
                                val headingLink: Element = secondVendor.selectHead("h3").selectHead("a")
                                val secondVendorInModel = SearchSoftwarePageContent.softwareVendorsResults.vendors(1)
                                headingLink.attr("href") shouldBe ProductDetailsController.show(URLEncoder.encode(secondVendorInModel.name, "UTF-8")).url
                                headingLink.text shouldBe s"${secondVendorInModel.name}"
                              }
                            }
                          }
                        } else "have a alpha software vendor section" which {
                          val softwareVendorsSection = getSoftwareVendorsSection(document)

                          "has a count of the number of software vendors on the page" in {
                            softwareVendorsSection.selectHead("#vendor-count")
                              .select("h2").text shouldBe SearchSoftwarePageContent.numberOfVendorsAlpha
                          }

                          "has a list of software vendors" which {

                            "has a software vendor with lots of detail" which {
                              def firstVendor: Element = softwareVendorsSection.selectHead("#software-vendor-0")

                              "has a heading for the software vendor" in {
                                val heading: Element = firstVendor.selectHead("h3")
                                val firstVendorInModel = SearchSoftwarePageContent.softwareVendorsResults.vendors.head
                                heading.text shouldBe s"${firstVendorInModel.name}"
                              }

                              "has a link for the software vendor" in {
                                val link: Element = firstVendor.selectHead("a")
                                link.attr("href") shouldBe SearchSoftwarePageContent.softwareVendorsResults.vendors.head.website
                                link.attr("target") shouldBe "_blank"
                                link.text contains s"${SearchSoftwarePageContent.softwareVendorsResults.vendors.head.name}"
                              }

                              "has a list of detail for the software vendor with full detail" in {
                                val summaryList: Element = firstVendor.selectHead("dl")

                                val firstRow: Element = summaryList.selectNth("div", 1)
                                firstRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.accessibility
                                firstRow.selectHead("dd").text shouldBe
                                  s"${SearchSoftwarePageContent.visual}/${SearchSoftwarePageContent.hearing}/${SearchSoftwarePageContent.motor}/${SearchSoftwarePageContent.cognitive}"

                                val secondRow: Element = summaryList.selectNth("div", 2)
                                secondRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.pricing
                                val pricingText = if (pricingSwitch)
                                  s"${SearchSoftwarePageContent.freeTrial}/${SearchSoftwarePageContent.freeVersion}"
                                else
                                  SearchSoftwarePageContent.freeVersion
                                secondRow.selectHead("dd").text shouldBe pricingText

                                val thirdRow: Element = summaryList.selectNth("div", 3)
                                val suitableForText = if (displayOverseasProperty)
                                  s"${SearchSoftwarePageContent.soleTrader}/${SearchSoftwarePageContent.ukProperty}/${SearchSoftwarePageContent.overseasProperty}"
                                else
                                  s"${SearchSoftwarePageContent.soleTrader}/${SearchSoftwarePageContent.ukProperty}"
                                thirdRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.suitableFor
                                thirdRow.selectHead("dd").text shouldBe suitableForText

                                val fourthRow: Element = summaryList.selectNth("div", 4)
                                fourthRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.operatingSystem
                                fourthRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.microsoftWindows}/${SearchSoftwarePageContent.macOS}"

                                val fifthRow: Element = summaryList.selectNth("div", 5)
                                fifthRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.mobileApp
                                fifthRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.android}/${SearchSoftwarePageContent.appleIOS}"

                                val sixthRow: Element = summaryList.selectNth("div", 6)
                                sixthRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.softwareType
                                sixthRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.browserBased}/${SearchSoftwarePageContent.applicationBased}"

                                val seventhRow: Element = summaryList.selectNth("div", 7)
                                seventhRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.softwareFor
                                seventhRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.recordKeeping}/${SearchSoftwarePageContent.bridging}"

                                val eighthRow: Element = summaryList.selectNth("div", 8)
                                eighthRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.businessType
                                eighthRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.individual}/${SearchSoftwarePageContent.agent}"

                                val ninthRow: Element = summaryList.selectNth("div", 9)
                                ninthRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.softwareCompatibility
                                ninthRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.vat}/${SearchSoftwarePageContent.incomeTax}"

                                val tenthRow: Element = summaryList.selectNth("div", 10)
                                tenthRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.language
                                tenthRow.selectHead("dd").text shouldBe s"${SearchSoftwarePageContent.welsh}/${SearchSoftwarePageContent.english}"
                              }
                            }

                            "has a software vendor with minimal detail" which {
                              def secondVendor: Element = softwareVendorsSection.selectHead("#software-vendor-1")

                              "has a heading for the software vendor" in {
                                val heading: Element = secondVendor.selectHead("h3")
                                val secondVendorInModel = SearchSoftwarePageContent.softwareVendorsResults.vendors(1)
                                heading.text shouldBe s"${secondVendorInModel.name}"
                              }

                              "has a link for the software vendor" in {
                                val link: Element = secondVendor.selectHead("a")
                                link.attr("href") shouldBe SearchSoftwarePageContent.softwareVendorsResults.vendors(1).website
                                link.attr("target") shouldBe "_blank"
                                link.text contains s"${SearchSoftwarePageContent.softwareVendorsResults.vendors(1).name}"
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

                                val secondRow: Element = summaryList.selectNth("div", 2)
                                secondRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.softwareCompatibility
                                secondRow.selectHead("dd").text shouldBe SearchSoftwarePageContent.incomeTax

                                val thirdRow: Element = summaryList.selectNth("div", 3)
                                thirdRow.selectHead("dt").text shouldBe SearchSoftwarePageContent.language
                                thirdRow.selectHead("dd").text shouldBe SearchSoftwarePageContent.english

                                val fourthRow: Option[Element] = summaryList.selectOptionally("div:nth-of-type(4)")
                                fourthRow shouldBe None
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

  def getDocument(hasResults: Boolean, hasError: Boolean, beta: Boolean, pricing: Boolean, overseas: Boolean): Document = {
    val results = if (hasResults) SearchSoftwarePageContent.softwareVendorsResults else SearchSoftwarePageContent.softwareVendorsNoResults
    Jsoup.parse(page(results, hasError, beta, pricing, overseas).body)
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
  val lastUpdate = "This page was last updated: 2 Dec 2022"
  val heading = "Choose your software"
  val paragraph1: String = "All software on this page has been through HMRC’s recognition process. " +
    "But we do not endorse or recommend any one product or software provider."
  val insetText = "If you need help to choose software, contact the software provider or your tax agent before making a decision."
  val whatSoftwareHeading = "What kind of software is available?"
  val whatSoftwareRecordKeepingHeading = "Record Keeping Software"
  val whatSoftwareRecordKeepingBullet1 = "updates and stores your records digitally"
  val whatSoftwareRecordKeepingBullet2 = "works directly with HMRC systems allowing you to file a Income tax"
  val whatSoftwareBridgingHeading = "Bridging software"
  val whatSoftwareBridgingBullet1 = "works with non-compatible software like spreadsheets, accounting systems and other digital bookkeeping products"
  val whatSoftwareBridgingBullet2 = "lets you send the required information digitally to HMRC in the correct format"
  val skiptoresults = "Skip to results."
  val searchFormHeading = "Search by software name"

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

  val numberOfVendorsAlpha = "Currently there are 2 software providers"

  val numberOfVendors = "2 software providers"

  val emptyVendorListMessage = "Your search has returned no results. To increase the number of results, we suggest you:"
  val emptyVendorListMessageHeading = "Your search has returned no results."
  val emptyVendorListMessageParagraph = "To increase the number of results, we suggest you:"
  val emptyVendorListMessageBullet1 = "reduce the number of filters you apply"
  val emptyVendorListMessageBullet2 = "make sure the name you have entered into the search bar is correct"

  val pricing = "Pricing:"
  val freeTrial = "Free trial"
  val freeVersion = "Free version"
  val freeVersionHint = "These are usually only free for a limited time, or have restricted features"
  val paidFor = "Paid for"
  val noFreeTrial = "No free trial"
  val noFreeVersion = "No free version"

  val suitableFor = "Suitable for:"
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

  val softwareFor = "Software for:"
  val recordKeeping = "Record keeping"
  val recordKeepingHint = "Software to store and submit your tax records"
  val bridging = "Bridging"
  val bridgingHint = "Submit records with selected non-compatible software, like spreadsheets"

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
