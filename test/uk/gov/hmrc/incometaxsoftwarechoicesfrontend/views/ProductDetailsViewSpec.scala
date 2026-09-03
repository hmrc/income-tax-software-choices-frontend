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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.{Available, Intended}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ProductDetailsView
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.TestModels.softwareVendorModelBase

class ProductDetailsViewSpec extends ViewSpec with BeforeAndAfterEach {

  import ProductDetailsPage._

  private val productDetailsView = app.injector.instanceOf[ProductDetailsView]

  private val softwareVendorModelFull = softwareVendorModelBase
    .copy(name = "abc full")
    .copy(filters = filterKeyToFilter.values.map(vf => vf -> Available).toMap)

  private val softwareVendorWithIntent = softwareVendorModelBase
    .copy(name = "abc minimal")
    .copy(filters = Map(
      SoleTrader -> Available, UkProperty -> Intended,
      UkDividends -> Intended, ForeignDividends -> Available, UkInterest -> Intended,
      StandardUpdatePeriods -> Available, CalendarUpdatePeriods -> Intended, FreeVersion -> Intended
    ))

  private val allFiltersPossibleToSelect =
    Some(allFilters.filterNot(
      Set(Agent, StandardUpdatePeriods, AveragingAdjustment, FosterCarer, TrustIncome, English).contains(_)
    ))

  private val filtersOnlyFromMinimalQuestionAnswers = Some(Seq(Individual, SoleTrader, StandardUpdatePeriods))

  "ProductDetailsPage" when {

    def getTableHeader(table: Element, col: Int): Element = table.selectHead(s"thead > tr > th.govuk-table__header:nth-of-type($col)")

    def checkTableHeader(table: Element, col1: String, col2: String): Assertion = {
      getTableHeader(table, 1).text shouldBe col1
      getTableHeader(table, 2).text shouldBe col2
    }

    def checkRow(table: Element, row: Int, field: String, status: String): Assertion = {
      table.selectHead(s"tbody > tr:nth-child($row) > th:nth-child(1)").text shouldBe field
      table.selectHead(s"tbody > tr:nth-child($row) > td:nth-child(2)").text shouldBe status
    }

    "in static view and the vendor has everything ready now" which {

      val document: Document = createAndParseDocument(softwareVendorModelFull)

      def table(index: Int): Element = document.getTable(index)

      "have a title" in {
        document.title shouldBe s"""${softwareVendorModelFull.name} - ${PageContentBase.title} - GOV.UK"""
      }

      "display the vendor name heading" in {
        document.selectNth("h1", 1).text() shouldBe softwareVendorModelFull.name
      }

      "has link to the vendor website" in {
        val link = document.mainContent.select(".govuk-link").get(0)
        link.text shouldBe s"Explore this software on ${softwareVendorModelFull.name}'s website (opens in new tab)"
        link.attr("href") shouldBe softwareVendorModelFull.website
        link.attr("target") shouldBe "_blank"
      }

      "have a software features heading" in {
        document.selectNth("h2", 1).text shouldBe softwareFeaturesHeading
      }

      "have the correct quarterly updates title" in {
        document.selectNth("h2", 2).text shouldBe quarterlyUpdatesHeading
      }

      "have the correct tax return title" in {
        document.selectNth("h2", 3).text shouldBe taxReturnHeading
      }

      "display all tables with correct details" which {
        "has the correct table headings" in {
          checkTableHeader(table(1), featureStatusTitle, meaningTitle)
          checkTableHeader(table(2), featuresProvidedTitle, statusTitle)
          checkTableHeader(table(3), businessIncomeTitle, statusTitle)
          checkTableHeader(table(4), otherIncomeTitle, statusTitle)

          checkTableHeader(table(5), applicationTypeTitle, statusTitle)
          checkTableHeader(table(6), accessibilityTitle, statusTitle)
          checkTableHeader(table(7), languageTitle, statusTitle)
        }

        "displays all the rows" in {
          checkRow(table(1), 1, readyNow, readyNowDescription)
          checkRow(table(1), 2, inDevelopment, inDevelopmentDescription)
          checkRow(table(1), 3, notIncluded, notIncludedDescription)
          
          checkRow(table(2), 1, freeVersion, readyNow)
          checkRow(table(2), 2, agent, readyNow)
          checkRow(table(2), 3, individual, readyNow)
          checkRow(table(2), 4, standardUpdatePeriods, readyNow)
          checkRow(table(2), 5, calendarUpdatePeriods, readyNow)
          checkRow(table(2), 6, recordKeeping, readyNow)
          checkRow(table(2), 7, bridging, readyNow)
          checkRow(table(2), 8, vat, readyNow)
          checkRow(table(2), 9, hmrcAssist, readyNow)
          
          checkRow(table(3), 1, soleTrader, readyNow)
          checkRow(table(3), 2, ukProperty, readyNow)
          checkRow(table(3), 3, foreignProperty, readyNow)
          
          checkRow(table(4), 1, ukInterest, readyNow)
          checkRow(table(4), 2, employment, readyNow)
          checkRow(table(4), 3, ukDividends, readyNow)
          checkRow(table(4), 4, statePension, readyNow)
          checkRow(table(4), 5, privatePensionIncome, readyNow)
          checkRow(table(4), 6, partnerIncome, readyNow)
          checkRow(table(4), 7, foreignDividend, readyNow)
          checkRow(table(4), 8, foreignInterest, readyNow)
          checkRow(table(4), 9, privatePensionContribution, readyNow)
          checkRow(table(4), 10, cis, readyNow)
          checkRow(table(4), 11, charitableGiving, readyNow)
          checkRow(table(4), 12, cgt, readyNow)
          checkRow(table(4), 13, student, readyNow)
          checkRow(table(4), 14, marriage, readyNow)
          checkRow(table(4), 15, class2NIC, readyNow)
          checkRow(table(4), 16, childBenefitCharge, readyNow)

          checkRow(table(5), 1, webBrowser, readyNow)
          checkRow(table(5), 2, microsoftWindows, readyNow)
          checkRow(table(5), 3, macOs, readyNow)
          checkRow(table(5), 4, linux, readyNow)
          checkRow(table(5), 5, android, readyNow)
          checkRow(table(5), 6, appleIOS, readyNow)

          checkRow(table(6), 1, visual, readyNow)
          checkRow(table(6), 2, hearing, readyNow)
          checkRow(table(6), 3, motor, readyNow)
          checkRow(table(6), 4, cognitive, readyNow)

          checkRow(table(7), 1, english, readyNow)
          checkRow(table(7), 2, welsh, readyNow)
        }
      }
    }

    "in static view and the vendor has features in development" which {

      val document: Document = createAndParseDocument(softwareVendorWithIntent)

      def table(index: Int): Element = document.getTable(index)

      "display the vendor name heading" in {
        document.selectNth("h1", 1).text() shouldBe softwareVendorWithIntent.name
      }

      "has a link to the vendor website" in {
        val link = document.mainContent.select(".govuk-link").get(0)
        link.text shouldBe s"Explore this software on ${softwareVendorWithIntent.name}'s website (opens in new tab)"
        link.attr("href") shouldBe softwareVendorWithIntent.website
        link.attr("target") shouldBe "_blank"
      }

      "have a software features heading" in {
        document.selectNth("h2", 1).text shouldBe softwareFeaturesHeading
      }

      "have the correct quarterly updates title" in {
        document.selectNth("h2", 2).text shouldBe quarterlyUpdatesHeading
      }

      "have the correct tax return title" in {
        document.selectNth("h2", 3).text shouldBe taxReturnHeading
      }

      "display all tables with correct details" which {

        "has the correct table headings" in {
          checkTableHeader(table(1), featureStatusTitle, meaningTitle)
          checkTableHeader(table(2), featuresProvidedTitle, statusTitle)
          checkTableHeader(table(3), businessIncomeTitle, statusTitle)
          checkTableHeader(table(4), otherIncomeTitle, statusTitle)
        }

        "displays the correct statuses" in {
          checkRow(table(1), 1, readyNow, readyNowDescription)
          checkRow(table(1), 2, inDevelopment, inDevelopmentDescription)
          checkRow(table(1), 3, notIncluded, notIncludedDescription)
          
          checkRow(table(2), 1, freeVersion, notIncluded)
          checkRow(table(2), 2, agent, notIncluded)
          checkRow(table(2), 3, individual, notIncluded)
          checkRow(table(2), 4, standardUpdatePeriods, readyNow)
          checkRow(table(2), 5, calendarUpdatePeriods, notIncluded)
          checkRow(table(2), 6, recordKeeping, notIncluded)
          checkRow(table(2), 7, bridging, notIncluded)
          checkRow(table(2), 8, vat, notIncluded)
          checkRow(table(2), 9, hmrcAssist, notIncluded)
          
          checkRow(table(3), 1, soleTrader, readyNow)
          checkRow(table(3), 2, ukProperty, notIncluded)
          checkRow(table(3), 3, foreignProperty, notIncluded)
          
          checkRow(table(4), 1, ukInterest, inDevelopment)
          checkRow(table(4), 2, employment, notIncluded)
          checkRow(table(4), 3, ukDividends, inDevelopment)
          checkRow(table(4), 4, statePension, notIncluded)
          checkRow(table(4), 5, privatePensionIncome, notIncluded)
          checkRow(table(4), 6, partnerIncome, notIncluded)
          checkRow(table(4), 7, foreignDividend, readyNow)
          checkRow(table(4), 8, foreignInterest, notIncluded)
          checkRow(table(4), 9, privatePensionContribution, notIncluded)
          checkRow(table(4), 10, cis, notIncluded)
          checkRow(table(4), 11, charitableGiving, notIncluded)
          checkRow(table(4), 12, cgt, notIncluded)
          checkRow(table(4), 13, student, notIncluded)
          checkRow(table(4), 14, marriage, notIncluded)
          checkRow(table(4), 15, class2NIC, notIncluded)
          checkRow(table(4), 16, childBenefitCharge, notIncluded)

          checkRow(table(5), 1, webBrowser, notIncluded)
          checkRow(table(5), 2, microsoftWindows, notIncluded)
          checkRow(table(5), 3, macOs, notIncluded)
          checkRow(table(5), 4, linux, notIncluded)
          checkRow(table(5), 5, android, notIncluded)
          checkRow(table(5), 6, appleIOS, notIncluded)

          checkRow(table(6), 1, visual, notIncluded)
          checkRow(table(6), 2, hearing, notIncluded)
          checkRow(table(6), 3, motor, notIncluded)
          checkRow(table(6), 4, cognitive, notIncluded)

          checkRow(table(7), 1, english, notIncluded)
          checkRow(table(7), 2, welsh, notIncluded)
        }
      }
    }

    "in static view and the vendor does not have any features" which {

      val document: Document = createAndParseDocument(softwareVendorModelBase)

      def table(index: Int): Element = document.getTable(index)

      "have a title" in {
        document.title shouldBe s"""${softwareVendorModelBase.name} - ${PageContentBase.title} - GOV.UK"""
      }

      "display the vendor name heading" in {
        document.selectNth("h1", 1).text() shouldBe softwareVendorModelBase.name
      }

      "has a link to the vendor website" in {
        val link = document.mainContent.select(".govuk-link").get(0)
        link.text shouldBe s"Explore this software on ${softwareVendorModelBase.name}'s website (opens in new tab)"
        link.attr("href") shouldBe softwareVendorModelBase.website
        link.attr("target") shouldBe "_blank"
      }

      "have a software features heading" in {
        document.selectNth("h2", 1).text shouldBe softwareFeaturesHeading
      }

      "have the correct quarterly updates title" in {
        document.selectNth("h2", 2).text shouldBe quarterlyUpdatesHeading
      }

      "have the correct tax return title" in {
        document.selectNth("h2", 3).text shouldBe taxReturnHeading
      }

      "display all tables with correct details" which {

        "has the correct table headings" in {
          checkTableHeader(table(1), featureStatusTitle, meaningTitle)
          checkTableHeader(table(2), featuresProvidedTitle, statusTitle)
          checkTableHeader(table(3), businessIncomeTitle, statusTitle)
          checkTableHeader(table(4), otherIncomeTitle, statusTitle)
          checkTableHeader(table(5), applicationTypeTitle, statusTitle)
          checkTableHeader(table(6), accessibilityTitle, statusTitle)
          checkTableHeader(table(7), languageTitle, statusTitle)
        }
        }

        "displays all the rows" in {
          checkRow(table(1), 1, readyNow, readyNowDescription)
          checkRow(table(1), 2, inDevelopment, inDevelopmentDescription)
          checkRow(table(1), 3, notIncluded, notIncludedDescription)
          
          checkRow(table(2), 1, freeVersion, notIncluded)
          checkRow(table(2), 2, agent, notIncluded)
          checkRow(table(2), 3, individual, notIncluded)
          checkRow(table(2), 4, standardUpdatePeriods, notIncluded)
          checkRow(table(2), 5, calendarUpdatePeriods, notIncluded)
          checkRow(table(2), 6, recordKeeping, notIncluded)
          checkRow(table(2), 7, bridging, notIncluded)
          checkRow(table(2), 8, vat, notIncluded)
          checkRow(table(2), 9, hmrcAssist, notIncluded)
          
          checkRow(table(3), 1, soleTrader, notIncluded)
          checkRow(table(3), 2, ukProperty, notIncluded)
          checkRow(table(3), 3, foreignProperty, notIncluded)
          
          checkRow(table(4), 1, ukInterest, notIncluded)
          checkRow(table(4), 2, employment, notIncluded)
          checkRow(table(4), 3, ukDividends, notIncluded)
          checkRow(table(4), 4, statePension, notIncluded)
          checkRow(table(4), 5, privatePensionIncome, notIncluded)
          checkRow(table(4), 6, partnerIncome, notIncluded)
          checkRow(table(4), 7, foreignDividend, notIncluded)
          checkRow(table(4), 8, foreignInterest, notIncluded)
          checkRow(table(4), 9, privatePensionContribution, notIncluded)
          checkRow(table(4), 10, cis, notIncluded)
          checkRow(table(4), 11, charitableGiving, notIncluded)
          checkRow(table(4), 12, cgt, notIncluded)
          checkRow(table(4), 13, student, notIncluded)
          checkRow(table(4), 14, marriage, notIncluded)
          checkRow(table(4), 15, class2NIC, notIncluded)
          checkRow(table(4), 16, childBenefitCharge, notIncluded)
          
          checkRow(table(5), 1, webBrowser, notIncluded)
          checkRow(table(5), 2, microsoftWindows, notIncluded)
          checkRow(table(5), 3, macOs, notIncluded)
          checkRow(table(5), 4, linux, notIncluded)
          checkRow(table(5), 5, android, notIncluded)
          checkRow(table(5), 6, appleIOS, notIncluded)

          checkRow(table(6), 1, visual, notIncluded)
          checkRow(table(6), 2, hearing, notIncluded)
          checkRow(table(6), 3, motor, notIncluded)
          checkRow(table(6), 4, cognitive, notIncluded)

          checkRow(table(7), 1, english, notIncluded)
          checkRow(table(7), 2, welsh, notIncluded)
        }
      }


    "in personalised view when the user has selected all possible filters" which {

      val document: Document = createAndParseDocument(softwareVendorModelFull, allFiltersPossibleToSelect)

      def table(index: Int): Element = document.getTable(index)

      "have a title" in {
        document.title shouldBe s"""${softwareVendorModelFull.name} - ${PageContentBase.title} - GOV.UK"""
      }

      "display the vendor name heading" in {
        document.selectNth("h1", 1).text() shouldBe softwareVendorModelFull.name
      }

      "has link to the vendor website" in {
        val link = document.mainContent.select(".govuk-link").get(0)
        link.text shouldBe s"Explore this software on ${softwareVendorModelFull.name}'s website (opens in new tab)"
        link.attr("href") shouldBe softwareVendorModelFull.website
        link.attr("target") shouldBe "_blank"
      }

      "have a main personalised heading" in {
        document.selectNth("h2", 1).text shouldBe mainPersonalisedHeading
      }

      "have a software features heading" in {
        document.selectNth("h2", 2).text shouldBe softwareFeaturesHeadingPersonalised
      }

      "have the correct quarterly updates title" in {
        document.selectNth("h2", 3).text shouldBe quarterlyUpdatesHeading
      }

      "have the correct tax return title" in {
        document.selectNth("h2", 4).text shouldBe taxReturnHeading
      }

      "have a software specifications heading" in {
        document.selectNth("h2", 5).text shouldBe softwareSpecificationsHeading
      }

      "display all tables with correct details" which {
        "has the correct table headings" in {
          checkTableHeader(table(1), featureStatusTitle, meaningTitle)
          checkTableHeader(table(2), featuresProvidedTitle, statusTitle)
          checkTableHeader(table(3), businessIncomeTitle, statusTitle)
          checkTableHeader(table(4), otherIncomeTitle, statusTitle)
          checkTableHeader(table(5), applicationTypeTitle, statusTitle)
          checkTableHeader(table(6), accessibilityTitle, statusTitle)
          checkTableHeader(table(7), languageTitle, statusTitle)
        }

        "displays all the rows" in {
          checkRow(table(1), 1, readyNow, readyNowDescription)
          checkRow(table(1), 2, inDevelopment, inDevelopmentDescription)
          checkRow(table(1), 3, notIncluded, notIncludedDescription)
          
          checkRow(table(2), 1, freeVersion, readyNow)
          checkRow(table(2), 2, individual, readyNow)
          checkRow(table(2), 3, calendarUpdatePeriods, readyNow)
          checkRow(table(2), 4, recordKeeping, readyNow)
          checkRow(table(2), 5, bridging, readyNow)
          checkRow(table(2), 6, vat, readyNow)
          checkRow(table(2), 7, hmrcAssist, readyNow)
          
          checkRow(table(3), 1, soleTrader, readyNow)
          checkRow(table(3), 2, ukProperty, readyNow)
          checkRow(table(3), 3, foreignProperty, readyNow)
          
          checkRow(table(4), 1, ukInterest, readyNow)
          checkRow(table(4), 2, employment, readyNow)
          checkRow(table(4), 3, ukDividends, readyNow)
          checkRow(table(4), 4, statePension, readyNow)
          checkRow(table(4), 5, privatePensionIncome, readyNow)
          checkRow(table(4), 6, partnerIncome, readyNow)
          checkRow(table(4), 7, foreignDividend, readyNow)
          checkRow(table(4), 8, foreignInterest, readyNow)
          checkRow(table(4), 9, privatePensionContribution, readyNow)
          checkRow(table(4), 10, cis, readyNow)
          checkRow(table(4), 11, charitableGiving, readyNow)
          checkRow(table(4), 12, cgt, readyNow)
          checkRow(table(4), 13, student, readyNow)
          checkRow(table(4), 14, marriage, readyNow)
          checkRow(table(4), 15, class2NIC, readyNow)
          checkRow(table(4), 16, childBenefitCharge, readyNow)
          
          checkRow(table(5), 1, webBrowser, readyNow)
          checkRow(table(5), 2, microsoftWindows, readyNow)
          checkRow(table(5), 3, macOs, readyNow)
          checkRow(table(5), 4, linux, readyNow)
          checkRow(table(5), 5, android, readyNow)
          checkRow(table(5), 6, appleIOS, readyNow)

          checkRow(table(6), 1, visual, readyNow)
          checkRow(table(6), 2, hearing, readyNow)
          checkRow(table(6), 3, motor, readyNow)
          checkRow(table(6), 4, cognitive, readyNow)

          checkRow(table(7), 1, welsh, readyNow)
        }
      }

      "have an 'other potential software features' section" which {

        val detailsElement: Element = document.selectNth("details", 1)

        def detailsElementTable(index: Int): Element = detailsElement.getTable(index)

        "have an other software features heading" in {
          detailsElement.selectNth("h2", 1).text shouldBe softwareFeaturesHeadingOther
        }

        "have an other software specifications heading" in {
          detailsElement.selectNth("h2", 2).text shouldBe softwareSpecificationsHeadingOther
        }

        "display all tables with correct details" which {
          "has the correct table headings" in {
            checkTableHeader(detailsElementTable(1), featuresProvidedTitle, statusTitle)
            checkTableHeader(detailsElementTable(2), languageTitle, statusTitle)
          }

          "displays all the rows" in {
            checkRow(detailsElementTable(1), 1, agent, readyNow)
            checkRow(detailsElementTable(1), 2, standardUpdatePeriods, readyNow)
            checkRow(detailsElementTable(2), 1, english, readyNow)
          }
        }
      }
    }

    "in personalised view when the user has only filters from minimal question answers" which {

      val document: Document = createAndParseDocument(softwareVendorModelFull, filtersOnlyFromMinimalQuestionAnswers)

      def table(index: Int): Element = document.getTable(index)

      "have a title" in {
        document.title shouldBe s"""${softwareVendorModelFull.name} - ${PageContentBase.title} - GOV.UK"""
      }

      "display the vendor name heading" in {
        document.selectNth("h1", 1).text() shouldBe softwareVendorModelFull.name
      }

      "has link to the vendor website" in {
        val link = document.mainContent.select(".govuk-link").get(0)
        link.text shouldBe s"Explore this software on ${softwareVendorModelFull.name}'s website (opens in new tab)"
        link.attr("href") shouldBe softwareVendorModelFull.website
        link.attr("target") shouldBe "_blank"
      }

      "have a main personalised heading" in {
        document.selectNth("h2", 1).text shouldBe mainPersonalisedHeading
      }

      "have a software features heading" in {
        document.selectNth("h2", 2).text shouldBe softwareFeaturesHeadingPersonalised
      }

      "have the correct quarterly updates title" in {
        document.selectNth("h2", 3).text shouldBe quarterlyUpdatesHeading
      }

      "display all tables with correct details" which {
        "has the correct table headings" in {
          checkTableHeader(table(1), featureStatusTitle, meaningTitle)
          checkTableHeader(table(2), featuresProvidedTitle, statusTitle)
          checkTableHeader(table(3), businessIncomeTitle, statusTitle)
        }

        "displays all the rows" in {
          checkRow(table(1), 1, readyNow, readyNowDescription)
          checkRow(table(1), 2, inDevelopment, inDevelopmentDescription)
          checkRow(table(1), 3, notIncluded, notIncludedDescription)
          checkRow(table(2), 1, individual, readyNow)
          checkRow(table(2), 2, standardUpdatePeriods, readyNow)
          checkRow(table(3), 1, soleTrader, readyNow)
        }
      }

      "have an 'other potential software features' section" which {

        val detailsElement: Element = document.selectNth("details", 1)

        def detailsElementTable(index: Int): Element = detailsElement.getTable(index)

        "have an other software features heading" in {
          detailsElement.selectNth("h2", 1).text shouldBe softwareFeaturesHeadingOther
        }

        "have an other quarterly update items heading" in {
          detailsElement.selectNth("h2", 2).text shouldBe quarterlyUpdatesHeadingOther
        }

        "have an tax return items heading" in {
          detailsElement.selectNth("h2", 3).text shouldBe taxReturnHeadingOther
        }

        "have an other software specifications heading" in {
          detailsElement.selectNth("h2", 4).text shouldBe softwareSpecificationsHeadingOther
        }

        "display all tables with correct details" which {
          "has the correct table headings" in {
            checkTableHeader(detailsElementTable(1), featuresProvidedTitle, statusTitle)
            checkTableHeader(detailsElementTable(2), businessIncomeTitle, statusTitle)
            checkTableHeader(detailsElementTable(3), otherIncomeTitle, statusTitle)
            checkTableHeader(detailsElementTable(4), applicationTypeTitle, statusTitle)
            checkTableHeader(detailsElementTable(5), accessibilityTitle, statusTitle)
            checkTableHeader(detailsElementTable(6), languageTitle, statusTitle)
          }

          "displays all the rows" in {
            checkRow(detailsElementTable(1), 1, freeVersion, readyNow)
            checkRow(detailsElementTable(1), 2, agent, readyNow)
            checkRow(detailsElementTable(1), 3, calendarUpdatePeriods, readyNow)
            checkRow(detailsElementTable(1), 4, recordKeeping, readyNow)
            checkRow(detailsElementTable(1), 5, bridging, readyNow)
            checkRow(detailsElementTable(1), 6, vat, readyNow)
            checkRow(detailsElementTable(1), 7, hmrcAssist, readyNow)

            checkRow(detailsElementTable(2), 1, ukProperty, readyNow)
            checkRow(detailsElementTable(2), 2, foreignProperty, readyNow)

            checkRow(detailsElementTable(3), 1, ukInterest, readyNow)
            checkRow(detailsElementTable(3), 2, employment, readyNow)
            checkRow(detailsElementTable(3), 3, ukDividends, readyNow)
            checkRow(detailsElementTable(3), 4, statePension, readyNow)
            checkRow(detailsElementTable(3), 5, privatePensionIncome, readyNow)
            checkRow(detailsElementTable(3), 6, partnerIncome, readyNow)
            checkRow(detailsElementTable(3), 7, foreignDividend, readyNow)
            checkRow(detailsElementTable(3), 8, foreignInterest, readyNow)
            checkRow(detailsElementTable(3), 9, privatePensionContribution, readyNow)
            checkRow(detailsElementTable(3), 10, cis, readyNow)
            checkRow(detailsElementTable(3), 11, charitableGiving, readyNow)
            checkRow(detailsElementTable(3), 12, cgt, readyNow)
            checkRow(detailsElementTable(3), 13, student, readyNow)
            checkRow(detailsElementTable(3), 14, marriage, readyNow)
            checkRow(detailsElementTable(3), 15, class2NIC, readyNow)
            checkRow(detailsElementTable(3), 16, childBenefitCharge, readyNow)

            checkRow(detailsElementTable(4), 1, webBrowser, readyNow)
            checkRow(detailsElementTable(4), 2, microsoftWindows, readyNow)
            checkRow(detailsElementTable(4), 3, macOs, readyNow)
            checkRow(detailsElementTable(4), 4, linux, readyNow)
            checkRow(detailsElementTable(4), 5, android, readyNow)
            checkRow(detailsElementTable(4), 6, appleIOS, readyNow)

            checkRow(detailsElementTable(5), 1, visual, readyNow)
            checkRow(detailsElementTable(5), 2, hearing, readyNow)
            checkRow(detailsElementTable(5), 3, motor, readyNow)
            checkRow(detailsElementTable(5), 4, cognitive, readyNow)

            checkRow(detailsElementTable(6), 1, english, readyNow)
            checkRow(detailsElementTable(6), 2, welsh, readyNow)
          }
        }
      }
    }


    "display the exit survey link" in {
      val document: Document = createAndParseDocument(softwareVendorModelFull)
      val link = document.mainContent.select(".govuk-link").get(1)
      link.text shouldBe exitSurveyLinkTitle
      link.attr("href") shouldBe exitSurveyLink
    }
  }

  private def page(vendorModel: SoftwareVendorModel, filters: Option[Seq[VendorFilter]] = None) = {
    //    println("page is here")
    //    println(productDetailsView(vendorModel, testBackUrl, filters))
    productDetailsView(vendorModel, testBackUrl, filters)
  }

  private def createAndParseDocument(vendorModel: SoftwareVendorModel, filters: Option[Seq[VendorFilter]] = None): Document = {
    //    println("page(vendorModel, filters).body")
    //    println(page(vendorModel, filters).body)
    Jsoup.parse(page(vendorModel, filters).body)
  }

  object ProductDetailsPage {

    val exitSurveyLinkTitle = "Give feedback on this service (opens in new tab)"
    val exitSurveyLink = "http://localhost:9514/feedback/SOFTWAREMTDIT?useServiceNavigation"

    val featureStatusHeading = "What each feature status means"
    val mainPersonalisedHeading = "Based on your selections"
    val softwareFeaturesHeading = "Software features"
    val softwareFeaturesHeadingPersonalised = "Software features needed"
    val softwareFeaturesHeadingOther = "Other features"
    val quarterlyUpdatesHeading = "What is needed for quarterly updates"
    val quarterlyUpdatesHeadingOther = "Other quarterly update income sources"
    val taxReturnHeading = "What is needed for tax returns"
    val taxReturnHeadingOther = "Other tax return income sources and items"
    val softwareSpecificationsHeading = "Software specifications"
    val softwareSpecificationsHeadingOther = "Other software specifications"

    val featureStatusTitle = "Feature status"
    val meaningTitle = "Meaning"
    val featuresProvidedTitle = "Features provided"
    val businessIncomeTitle = "Business income sources"
    val otherIncomeTitle = "Other income sources and items"
    val applicationTypeTitle = "Software application type"
    val accessibilityTitle = "Accessibility features"
    val languageTitle = "Language"
    val statusTitle = "Status"


    val freeVersion = "Free version"
    val agent = "Agent software"
    val individual = "Individual software"
    val standardUpdatePeriods = "Standard update period (6 April to 5 April)"
    val calendarUpdatePeriods = "Calendar update period (1 April to 31 March)"
    val recordKeeping = "All-in-one software that creates digital records"
    val bridging = "Bridging software that connects to records"
    val vat = "Making Tax Digital for VAT"
    val hmrcAssist = "HMRC Assist (Submission Feedback)"

    val quarterlyUpdates = "Quarterly updates"
    val soleTrader = "Sole trader"
    val ukProperty = "UK property"
    val foreignProperty = "Foreign property"

    val saTaxReturn = "Tax return"
    val cis = "Construction Industry Scheme"
    val cgt = "Capital Gains"
    val employment = "Employment (PAYE)"
    val foreignInterest = "Foreign interest"
    val foreignDividend = "Foreign dividends"
    val ukDividends = "UK dividends"
    val ukInterest = "UK interest"
    val charitableGiving = "Charitable giving"
    val student = "Student Loan"
    val class2NIC = "Voluntary Class 2 National Insurance"
    val childBenefitCharge = "High Income Child Benefit Charge"
    val statePension = "State Pension income"
    val privatePensionIncome = "Private pension incomes"
    val privatePensionContribution = "Private pension contributions"
    val marriage = "Marriage Allowance"
    val partnerIncome = "Partner income from a partnership"

    val webBrowser = "Online in web browser (all systems)"
    val microsoftWindows = "Desktop app (Microsoft Windows)"
    val macOs = "Desktop app (Mac OS)"
    val linux = "Desktop app (Linux)"
    val android = "Mobile app (Android)"
    val appleIOS = "Mobile app (Apple iOS)"

    val visual = "Blindness or impaired vision"
    val hearing = "Deafness or impaired hearing"
    val motor = "Motor or physical difficulties"
    val cognitive = "Cognitive impairments"

    val english = "English"
    val welsh = "Welsh"

    val readyNow = "Ready now"
    val inDevelopment = "In development"
    val notIncluded = "Not included"

    val readyNowDescription = "This feature is ready to use now."
    val inDevelopmentDescription = "The software provider has committed to building this in time for the 2026 to 2027 tax return."
    val notIncludedDescription = "This is not available in this software product."
  }

}