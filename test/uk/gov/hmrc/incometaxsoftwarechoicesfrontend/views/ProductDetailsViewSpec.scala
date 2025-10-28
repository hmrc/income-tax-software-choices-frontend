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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.{Available, Intended}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareVendorModel
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ProductDetailsView

class ProductDetailsViewSpec extends ViewSpec {

  private val productDetailsPage = app.injector.instanceOf[ProductDetailsView]

  val softwareVendorModelBase: SoftwareVendorModel = SoftwareVendorModel(
    name = "abc",
    email = Some("test@software-vendor-name.com"),
    phone = Some("00000 000 000"),
    website = "software-vendor-name.com",
    filters = Map.empty,
    accessibilityStatementLink = None
  )

  private val softwareVendorModelFull = softwareVendorModelBase
    .copy(name = "abc full")
    .copy(filters = filterKeyToFilter.values.map(vf => vf -> Available).toMap) // All filters

  private val softwareVendorWithIntent = softwareVendorModelBase
    .copy(name = "abc minimal")
    .copy(filters = Map(
      SoleTrader -> Available, UkProperty -> Intended,
      UkDividends -> Intended, ForeignDividends -> Available, UkInterest -> Intended,
      StandardUpdatePeriods -> Available, CalendarUpdatePeriods -> Intended, FreeVersion -> Intended
    ))

  "ProductDetailsPage" when {

    def getTableHeader(table: Element, col: Int): Element = table.selectHead(s"thead > tr > th.govuk-table__header:nth-of-type($col)")

    def checkTableHeader(table: Element, col1: String, col2: String): Assertion = {
      getTableHeader(table, 1).text shouldBe col1
      getTableHeader(table, 2).text shouldBe col2
    }
    
    def checkRow(table: Element, row: Int, field: String, status: String): Assertion = {
      table.selectHead(s"tbody > tr:nth-child($row) > td:nth-child(1)").text shouldBe field
      table.selectHead(s"tbody > tr:nth-child($row) > td:nth-child(2)").text shouldBe status
    }

    "the vendor has everything ready now" which {

      val document: Document = createAndParseDocument(softwareVendorModelFull)

      def table(index: Int): Element = document.getTable(index)

      "have a title" in {
        document.title shouldBe s"""${softwareVendorModelFull.name} - ${PageContentBase.title} - GOV.UK"""
      }

      "display the vendor name heading" in {
        document.selectNth("h1", 1).text() shouldBe softwareVendorModelFull.name
      }

      "display the vendor name heading paragraph" in {
        document.mainContent.selectNth("p", 1).text() shouldBe s"${ProductDetailsPage.paragraph}"
      }

      "display the vendor website" in {
        val vendorInformationSection = document.selectNth("dl", 1)
        val row: Element = vendorInformationSection.selectNth(".govuk-summary-list__row", 1)
        val link = row.selectHead("dd").selectHead("a")

        row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsWebsite}:"
        link.text shouldBe s"${softwareVendorModelBase.website} (opens in new tab)"
        link.attr("href") shouldBe softwareVendorModelBase.website
        link.attr("target") shouldBe "_blank"
      }

      "have a software features heading" in {
        document.selectNth("h2", 1).text shouldBe ProductDetailsPage.softwareFeaturesHeading
      }

      "have the correct quarterly updates title" in {
        document.selectNth("h2", 2).text shouldBe ProductDetailsPage.quarterlyUpdatessHeading
      }

      "have the correct quarterly updates description" in {
        document.mainContent.selectNth(".govuk-body-m", 1).text() shouldBe ProductDetailsPage.quarterlyUpdatessDetails
      }

      "have the correct tax return title" in {
        document.selectNth("h2", 3).text shouldBe ProductDetailsPage.taxReturnHeading
      }

      "display all tables with correct details" which {
        "has the correct table headings" in {
          checkTableHeader(table(1), "Features provided", "Status")
          checkTableHeader(table(2), "Business income sources", "Status")
          checkTableHeader(table(3), "Other income sources and items", "Status")
        }

        "displays all the rows" in {
          checkRow(table(1), 1, ProductDetailsPage.freeVersion, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(1), 2, ProductDetailsPage.recordKeeping, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(1), 3, ProductDetailsPage.bridging, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(1), 4, ProductDetailsPage.agent, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(1), 5, ProductDetailsPage.individual, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(1), 6, ProductDetailsPage.standardUpdatePeriods, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(1), 7, ProductDetailsPage.calendarUpdatePeriods, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(2), 1, ProductDetailsPage.soleTrader, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(2), 2, ProductDetailsPage.ukProperty, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(2), 3, ProductDetailsPage.foreignProperty, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 1, ProductDetailsPage.ukInterest, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 2, ProductDetailsPage.cis, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 3, ProductDetailsPage.employment, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 4, ProductDetailsPage.ukDividends, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 5, ProductDetailsPage.statePension, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 6, ProductDetailsPage.privatePensionIncome, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 7, ProductDetailsPage.foreignDividend, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 8, ProductDetailsPage.foreignInterest, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 9, ProductDetailsPage.privatePensionContribution, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 10, ProductDetailsPage.charitableGiving, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 11, ProductDetailsPage.cgt, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 12, ProductDetailsPage.student, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 13, ProductDetailsPage.marriage, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 14, ProductDetailsPage.class2NIC, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 15, ProductDetailsPage.childBenefitCharge, status = s"${ProductDetailsPage.readyNow}")
        }
      }
    }

    "the vendor has features in development" which {

      val document: Document = createAndParseDocument(softwareVendorWithIntent)

      def table(index: Int): Element = document.getTable(index)

      "display the vendor name heading" in {
        document.selectNth("h1", 1).text() shouldBe softwareVendorWithIntent.name
      }

      "display the vendor name heading paragraph" in {
        document.mainContent.selectNth("p", 1).text() shouldBe s"${ProductDetailsPage.paragraph}"
      }
      
      "display the vendor website" in {
        val vendorInformationSection = document.selectNth("dl", 1)
        val row: Element = vendorInformationSection.selectNth(".govuk-summary-list__row", 1)
        val link = row.selectHead("dd").selectHead("a")

        row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsWebsite}:"
        link.text shouldBe s"${softwareVendorWithIntent.website} (opens in new tab)"
        link.attr("href") shouldBe softwareVendorWithIntent.website
        link.attr("target") shouldBe "_blank"
      }

      "have a software features heading" in {
        document.selectNth("h2", 1).text shouldBe ProductDetailsPage.softwareFeaturesHeading
      }

      "have the correct quarterly updates title" in {
        document.selectNth("h2", 2).text shouldBe ProductDetailsPage.quarterlyUpdatessHeading
      }

      "have the correct quarterly updates description" in {
        document.mainContent.selectNth(".govuk-body-m", 1).text() shouldBe ProductDetailsPage.quarterlyUpdatessDetails
      }

      "have the correct tax return title" in {
        document.selectNth("h2", 3).text shouldBe ProductDetailsPage.taxReturnHeading
      }

      "display all tables with correct details" which {

        "has the correct table headings" in {
          checkTableHeader(table(1), "Features provided", "Status")
          checkTableHeader(table(2), "Business income sources", "Status")
          checkTableHeader(table(3), "Other income sources and items", "Status")
        }

        "displays the correct statuses" in {
          checkRow(table(1), 1, ProductDetailsPage.freeVersion, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(1), 2, ProductDetailsPage.recordKeeping, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(1), 3, ProductDetailsPage.bridging, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(1), 4, ProductDetailsPage.agent, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(1), 5, ProductDetailsPage.individual, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(1), 6, ProductDetailsPage.standardUpdatePeriods, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(1), 7, ProductDetailsPage.calendarUpdatePeriods, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(2), 1, ProductDetailsPage.soleTrader, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(2), 2, ProductDetailsPage.ukProperty, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(2), 3, ProductDetailsPage.foreignProperty, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 1, ProductDetailsPage.ukInterest, status = s"${ProductDetailsPage.inDevelopment}")
          checkRow(table(3), 2, ProductDetailsPage.cis, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 3, ProductDetailsPage.employment, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 4, ProductDetailsPage.ukDividends, status = s"${ProductDetailsPage.inDevelopment}")
          checkRow(table(3), 5, ProductDetailsPage.statePension, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 6, ProductDetailsPage.privatePensionIncome, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 7, ProductDetailsPage.foreignDividend, status = s"${ProductDetailsPage.readyNow}")
          checkRow(table(3), 8, ProductDetailsPage.foreignInterest, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 9, ProductDetailsPage.privatePensionContribution, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 10, ProductDetailsPage.charitableGiving, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 11, ProductDetailsPage.cgt, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 12, ProductDetailsPage.student, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 13, ProductDetailsPage.marriage, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 14, ProductDetailsPage.class2NIC, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 15, ProductDetailsPage.childBenefitCharge, status = s"${ProductDetailsPage.notIncluded}")
        }
      }
    }

    "the vendor does not have any features" which {

      val document: Document = createAndParseDocument(softwareVendorModelBase)

      def table(index: Int): Element = document.getTable(index)

      "have a title" in {
        document.title shouldBe s"""${softwareVendorModelBase.name} - ${PageContentBase.title} - GOV.UK"""
      }

      "display the vendor name heading" in {
        document.selectNth("h1", 1).text() shouldBe softwareVendorModelBase.name
      }

      "display the vendor name heading paragraph" in {
        document.mainContent.selectNth("p", 1).text() shouldBe s"${ProductDetailsPage.paragraph}"
      }
      
      "display the vendor website" in {
        val vendorInformationSection = document.selectNth("dl", 1)
        val row: Element = vendorInformationSection.selectNth(".govuk-summary-list__row", 1)
        val link = row.selectHead("dd").selectHead("a")

        row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsWebsite}:"
        link.text shouldBe s"${softwareVendorModelBase.website} (opens in new tab)"
        link.attr("href") shouldBe softwareVendorModelBase.website
        link.attr("target") shouldBe "_blank"
      }

      "have a software features heading" in {
        document.selectNth("h2", 1).text shouldBe ProductDetailsPage.softwareFeaturesHeading
      }

      "have the correct quarterly updates title" in {
        document.selectNth("h2", 2).text shouldBe ProductDetailsPage.quarterlyUpdatessHeading
      }

      "have the correct quarterly updates description" in {
        document.mainContent.selectNth(".govuk-body-m", 1).text() shouldBe ProductDetailsPage.quarterlyUpdatessDetails
      }
      
      "have the correct tax return title" in {
        document.selectNth("h2", 3).text shouldBe ProductDetailsPage.taxReturnHeading
      }

      "display all tables with correct details" which {

        "has the correct table headings" in {
          checkTableHeader(table(1), "Features provided", "Status")
          checkTableHeader(table(2), "Business income sources", "Status")
          checkTableHeader(table(3), "Other income sources and items", "Status")
        }

        "displays all the rows" in {
          checkRow(table(1), 1, ProductDetailsPage.freeVersion, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(1), 2, ProductDetailsPage.recordKeeping, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(1), 3, ProductDetailsPage.bridging, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(1), 4, ProductDetailsPage.agent, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(1), 5, ProductDetailsPage.individual, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(1), 6, ProductDetailsPage.standardUpdatePeriods, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(1), 7, ProductDetailsPage.calendarUpdatePeriods, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(2), 1, ProductDetailsPage.soleTrader, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(2), 2, ProductDetailsPage.ukProperty, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(2), 3, ProductDetailsPage.foreignProperty, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 1, ProductDetailsPage.ukInterest, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 2, ProductDetailsPage.cis, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 3, ProductDetailsPage.employment, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 4, ProductDetailsPage.ukDividends, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 5, ProductDetailsPage.statePension, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 6, ProductDetailsPage.privatePensionIncome, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 7, ProductDetailsPage.foreignDividend, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 8, ProductDetailsPage.foreignInterest, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 9, ProductDetailsPage.privatePensionContribution, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 10, ProductDetailsPage.charitableGiving, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 11, ProductDetailsPage.cgt, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 12, ProductDetailsPage.student, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 13, ProductDetailsPage.marriage, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 14, ProductDetailsPage.class2NIC, status = s"${ProductDetailsPage.notIncluded}")
          checkRow(table(3), 15, ProductDetailsPage.childBenefitCharge, status = s"${ProductDetailsPage.notIncluded}")
        }
      }
    }

    "display software specifications of the vendor" which {
      val document: Document = createAndParseDocument(softwareVendorModelFull)

      "have a software spec heading" in {
        document.select("h2").get(3).text shouldBe ProductDetailsPage.softwareSpecHeading
      }

      "render the correct rows when every spec is present" in {
        val specList = document.select("dl.govuk-summary-list").get(1)
        val rows = specList.select(".govuk-summary-list__row")
        rows.size shouldBe 4

        rows.get(0).select("dt").text() shouldBe ProductDetailsPage.softwareType
        rows.get(0).select("dd").text() shouldBe
          Seq(
            ProductDetailsPage.desktopBased,
            ProductDetailsPage.webBrowser
          ).mkString(" ")

        rows.get(1).select("dt").text() shouldBe ProductDetailsPage.compatibleWith
        rows.get(1).select("dd").text() shouldBe
          Seq(
            ProductDetailsPage.microsoftWindows,
            ProductDetailsPage.macOs,
            ProductDetailsPage.linux
          ).mkString(" ")

        rows.get(2).select("dt").text() shouldBe ProductDetailsPage.mobileApp
        rows.get(2).select("dd").text() shouldBe
          Seq(
            ProductDetailsPage.android,
            ProductDetailsPage.appleIOS
          ).mkString(" ")

        rows.get(3).select("dt").text() shouldBe ProductDetailsPage.language
        rows.get(3).select("dd").text() shouldBe ProductDetailsPage.english
      }

      "not render the specs section when no specs are present or are intended" in {
        val docEmpty: Document = createAndParseDocument(softwareVendorModelBase.copy(filters = Map(
          DesktopApplication -> Intended, WebBrowser -> Intended, MacOS -> Intended, Apple -> Intended, English -> Intended
        )))
        val allSummaryLists = docEmpty.select("dl.govuk-summary-list")
        allSummaryLists.size shouldBe 1

        val specHeading = ProductDetailsPage.softwareSpecHeading
        docEmpty.select(s"h2:contains($specHeading)").size shouldBe 0
      }
    }
  }

  private def page(vendorModel: SoftwareVendorModel) =
    productDetailsPage(vendorModel, zeroResults = false)

  private def createAndParseDocument(vendorModel: SoftwareVendorModel): Document =
    Jsoup.parse(page(vendorModel).body)

  object ProductDetailsPage {
    val paragraph = "Visit the company’s website to check if the software is right for you"
    val contactDetailsWebsite = "Website"

    val softwareFeaturesHeading = "Software features"
    val quarterlyUpdatessHeading = "What you need for your quarterly updates"
    val taxReturnHeading = "What you need for your tax return"

    val quarterlyUpdatessDetails = "You’ll still need to send these income sources in your tax return."

    val freeVersion = "Free version"
    val recordKeeping = "Software that creates digital records"
    val bridging = "Software that connects to your records (bridging software)"
    val quarterlyUpdates = "Quarterly updates"
    val saTaxReturn = "Tax return"
    val agent = "Agent software"
    val individual = "Individual software"
    val standardUpdatePeriods = "Standard update period (6 April to 5 April)"
    val calendarUpdatePeriods = "Calendar update period (1 April to 31 March)"
    val soleTrader = "Sole trader"
    val ukProperty = "UK property"
    val foreignProperty = "Foreign property"
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

    val softwareSpecHeading = "Software specifications"
    val softwareType = "Software type"
    val compatibleWith = "Compatible with"
    val mobileApp = "Mobile App"
    val language = "Language"
    val desktopBased = "Desktop application"
    val webBrowser = "Web browser"
    val microsoftWindows = "Microsoft Windows"
    val macOs = "Mac OS"
    val linux = "Linux"
    val android = "Android"
    val appleIOS = "Apple iOS"
    val english = "English"

    val readyNow = "Ready now"
    val inDevelopment = "In development"
    val notIncluded = "Not included"
  }

}
