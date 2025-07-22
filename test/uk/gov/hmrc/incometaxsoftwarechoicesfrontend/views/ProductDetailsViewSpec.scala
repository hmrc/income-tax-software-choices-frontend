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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareVendorModel
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ProductDetailsPage

class ProductDetailsViewSpec extends ViewSpec {

  private val productDetailsPage = app.injector.instanceOf[ProductDetailsPage]

  val softwareVendorModelBase: SoftwareVendorModel = SoftwareVendorModel(
    name = "abc",
    email = Some("test@software-vendor-name.com"),
    phone = Some("00000 000 000"),
    website = "software-vendor-name.com",
    filters = Seq.empty,
    accessibilityStatementLink = None
  )

  private val softwareVendorModelFull = softwareVendorModelBase
    .copy(name = "abc full")
    .copy(filters = filterKeyToFilter.values.toList) // All filters

  private val softwareVendorModelMinimal = softwareVendorModelBase
    .copy(name = "abc minimal")
    .copy(filters = Seq(SoleTrader))

  "ProductDetailsPage" when {

    def getTableHeader(table: Element, col: Int): Element = table.selectHead(s"thead > tr > th.govuk-table__header:nth-of-type($col)")

    def checkTableHeader(table: Element, col1: String, col2: String): Assertion = {
      getTableHeader(table, 1).text shouldBe col1
      getTableHeader(table, 2).text shouldBe col2
    }

    def checkRow(table: Element, row: Int, field: String, included: Boolean): Assertion = {
      val status = if (included) "Included" else "Not Included"
      table.selectHead(s"tbody > tr:nth-child($row) > td:nth-child(1)").text shouldBe field
      table.selectHead(s"tbody > tr:nth-child($row) > td:nth-child(2)").text shouldBe status
    }

    "the vendor has everything" which {

      val document: Document = createAndParseDocument(softwareVendorModelFull)

      def table(index: Int): Element = document.getTable(index)

      "have a title" in {
        document.title shouldBe s"""${softwareVendorModelFull.name} - ${PageContentBase.title} - GOV.UK"""
      }

      "display the vendor name heading" in {
        document.selectNth("h1", 1).text() shouldBe softwareVendorModelFull.name
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

      "display all tables with correct details" which {
        "has the correct table headings" in {
          checkTableHeader(table(1), "Features provided", "Status")
          checkTableHeader(table(2), "Business income sources", "Status")
          checkTableHeader(table(3), "Other income sources", "Status")
          checkTableHeader(table(4), "Other items", "Status")
        }

        "displays all the rows" in {
          checkRow(table(1), 1, ProductDetailsPage.freeVersion, included = true)
          checkRow(table(1), 2, ProductDetailsPage.recordKeeping, included = true)
          checkRow(table(1), 3, ProductDetailsPage.bridging, included = true)
          checkRow(table(1), 4, ProductDetailsPage.quarterlyUpdates, included = true)
          checkRow(table(1), 5, ProductDetailsPage.saTaxReturn, included = true)
          checkRow(table(1), 6, ProductDetailsPage.agent, included = true)
          checkRow(table(1), 7, ProductDetailsPage.individual, included = true)
          checkRow(table(1), 8, ProductDetailsPage.standardUpdatePeriods, included = true)
          checkRow(table(1), 9, ProductDetailsPage.calendarUpdatePeriods, included = true)
          checkRow(table(2), 1, ProductDetailsPage.soleTrader, included = true)
          checkRow(table(2), 2, ProductDetailsPage.ukProperty, included = true)
          checkRow(table(2), 3, ProductDetailsPage.foreignProperty, included = true)
          checkRow(table(3), 1, ProductDetailsPage.cis, included = true)
          checkRow(table(3), 2, ProductDetailsPage.employment, included = true)
          checkRow(table(3), 3, ProductDetailsPage.foreignInterest, included = true)
          checkRow(table(3), 4, ProductDetailsPage.foreignDividend, included = true)
          checkRow(table(3), 5, ProductDetailsPage.ukDividends, included = true)
          checkRow(table(3), 6, ProductDetailsPage.ukInterest, included = true)
          checkRow(table(3), 7, ProductDetailsPage.statePension, included = true)
          checkRow(table(3), 8, ProductDetailsPage.privatePensionIncome, included = true)
          checkRow(table(4), 1, ProductDetailsPage.cgt, included = true)
          checkRow(table(4), 2, ProductDetailsPage.charitableGiving, included = true)
          checkRow(table(4), 3, ProductDetailsPage.student, included = true)
          checkRow(table(4), 4, ProductDetailsPage.class2NIC, included = true)
          checkRow(table(4), 5, ProductDetailsPage.childBenefitCharge, included = true)
          checkRow(table(4), 6, ProductDetailsPage.privatePensionContribution, included = true)
          checkRow(table(4), 7, ProductDetailsPage.marriage, included = true)
        }
      }
    }

    "the vendor has minimal features" which {

      val document: Document = createAndParseDocument(softwareVendorModelMinimal)

      def table(index: Int): Element = document.getTable(index)

      "have a title" in {
        document.title shouldBe s"""${softwareVendorModelMinimal.name} - ${PageContentBase.title} - GOV.UK"""
      }

      "display the vendor name heading" in {
        document.selectNth("h1", 1).text() shouldBe softwareVendorModelMinimal.name
      }


      "display the vendor website" in {
        val vendorInformationSection = document.selectNth("dl", 1)
        val row: Element = vendorInformationSection.selectNth(".govuk-summary-list__row", 1)
        val link = row.selectHead("dd").selectHead("a")

        row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsWebsite}:"
        link.text shouldBe s"${softwareVendorModelMinimal.website} (opens in new tab)"
        link.attr("href") shouldBe softwareVendorModelMinimal.website
        link.attr("target") shouldBe "_blank"
      }

      "have a software features heading" in {
        document.selectNth("h2", 1).text shouldBe ProductDetailsPage.softwareFeaturesHeading
      }

      "display all tables with correct details" which {

        "has the correct table headings" in {
          checkTableHeader(table(1), "Features provided", "Status")
          checkTableHeader(table(2), "Business income sources", "Status")
          checkTableHeader(table(3), "Other income sources", "Status")
          checkTableHeader(table(4), "Other items", "Status")
        }

        "displays all the rows" in {
          checkRow(table(1), 1, ProductDetailsPage.freeVersion, included = false)
          checkRow(table(1), 2, ProductDetailsPage.recordKeeping, included = false)
          checkRow(table(1), 3, ProductDetailsPage.bridging, included = false)
          checkRow(table(1), 4, ProductDetailsPage.quarterlyUpdates, included = false)
          checkRow(table(1), 5, ProductDetailsPage.saTaxReturn, included = false)
          checkRow(table(1), 6, ProductDetailsPage.agent, included = false)
          checkRow(table(1), 7, ProductDetailsPage.individual, included = false)
          checkRow(table(1), 8, ProductDetailsPage.standardUpdatePeriods, included = false)
          checkRow(table(1), 9, ProductDetailsPage.calendarUpdatePeriods, included = false)
          checkRow(table(2), 1, ProductDetailsPage.soleTrader, included = true)
          checkRow(table(2), 2, ProductDetailsPage.ukProperty, included = false)
          checkRow(table(2), 3, ProductDetailsPage.foreignProperty, included = false)
          checkRow(table(3), 1, ProductDetailsPage.cis, included = false)
          checkRow(table(3), 2, ProductDetailsPage.employment, included = false)
          checkRow(table(3), 3, ProductDetailsPage.foreignInterest, included = false)
          checkRow(table(3), 4, ProductDetailsPage.foreignDividend, included = false)
          checkRow(table(3), 5, ProductDetailsPage.ukDividends, included = false)
          checkRow(table(3), 6, ProductDetailsPage.ukInterest, included = false)
          checkRow(table(3), 7, ProductDetailsPage.statePension, included = false)
          checkRow(table(3), 8, ProductDetailsPage.privatePensionIncome, included = false)
          checkRow(table(4), 1, ProductDetailsPage.cgt, included = false)
          checkRow(table(4), 2, ProductDetailsPage.charitableGiving, included = false)
          checkRow(table(4), 3, ProductDetailsPage.student, included = false)
          checkRow(table(4), 4, ProductDetailsPage.class2NIC, included = false)
          checkRow(table(4), 5, ProductDetailsPage.childBenefitCharge, included = false)
          checkRow(table(4), 6, ProductDetailsPage.privatePensionContribution, included = false)
          checkRow(table(4), 7, ProductDetailsPage.marriage, included = false)
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

      "display all tables with correct details" which {

        "has the correct table headings" in {
          checkTableHeader(table(1), "Features provided", "Status")
          checkTableHeader(table(2), "Business income sources", "Status")
          checkTableHeader(table(3), "Other income sources", "Status")
          checkTableHeader(table(4), "Other items", "Status")
        }

        "displays all the rows" in {
          checkRow(table(1), 1, ProductDetailsPage.freeVersion, included = false)
          checkRow(table(1), 2, ProductDetailsPage.recordKeeping, included = false)
          checkRow(table(1), 3, ProductDetailsPage.bridging, included = false)
          checkRow(table(1), 4, ProductDetailsPage.quarterlyUpdates, included = false)
          checkRow(table(1), 5, ProductDetailsPage.saTaxReturn, included = false)
          checkRow(table(1), 6, ProductDetailsPage.agent, included = false)
          checkRow(table(1), 7, ProductDetailsPage.individual, included = false)
          checkRow(table(1), 8, ProductDetailsPage.standardUpdatePeriods, included = false)
          checkRow(table(1), 9, ProductDetailsPage.calendarUpdatePeriods, included = false)
          checkRow(table(2), 1, ProductDetailsPage.soleTrader, included = false)
          checkRow(table(2), 2, ProductDetailsPage.ukProperty, included = false)
          checkRow(table(2), 3, ProductDetailsPage.foreignProperty, included = false)
          checkRow(table(3), 1, ProductDetailsPage.cis, included = false)
          checkRow(table(3), 2, ProductDetailsPage.employment, included = false)
          checkRow(table(3), 3, ProductDetailsPage.foreignInterest, included = false)
          checkRow(table(3), 4, ProductDetailsPage.foreignDividend, included = false)
          checkRow(table(3), 5, ProductDetailsPage.ukDividends, included = false)
          checkRow(table(3), 6, ProductDetailsPage.ukInterest, included = false)
          checkRow(table(3), 7, ProductDetailsPage.statePension, included = false)
          checkRow(table(3), 8, ProductDetailsPage.privatePensionIncome, included = false)
          checkRow(table(4), 1, ProductDetailsPage.cgt, included = false)
          checkRow(table(4), 2, ProductDetailsPage.charitableGiving, included = false)
          checkRow(table(4), 3, ProductDetailsPage.student, included = false)
          checkRow(table(4), 4, ProductDetailsPage.class2NIC, included = false)
          checkRow(table(4), 5, ProductDetailsPage.childBenefitCharge, included = false)
          checkRow(table(4), 6, ProductDetailsPage.privatePensionContribution, included = false)
          checkRow(table(4), 7, ProductDetailsPage.marriage, included = false)
        }
      }
    }

    "display software specifications of the vendor" which {
      val document: Document = createAndParseDocument(softwareVendorModelFull)

      "have a software spec heading" in {
        document.select("h2").get(1).text shouldBe ProductDetailsPage.softwareSpecHeading
      }

      "render the correct rows when every spec is present" in {
        val specList = document.select("dl.govuk-summary-list").get(1)
        val rows = specList.select(".govuk-summary-list__row")
        rows.size shouldBe 4

        rows.get(0).select("dt").text() shouldBe ProductDetailsPage.softwareType
        rows.get(0).select("dd").text() shouldBe ProductDetailsPage.desktopBased

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

      "not render the specs section when no specs are present" in {
        val docEmpty: Document = createAndParseDocument(softwareVendorModelBase)
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

    val freeVersion = "Free version"
    val recordKeeping = "Record keeping"
    val bridging = "Bridging"
    val quarterlyUpdates = "Quarterly updates"
    val saTaxReturn = "Self Assessment tax return"
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
    val ukDividends = "UK Dividends"
    val ukInterest = "UK Interest"
    val charitableGiving = "Charitable giving"
    val student = "Student loan"
    val class2NIC = "Voluntary Class 2 National Insurance"
    val childBenefitCharge = "High Income Child Benefit Charge"
    val statePension = "State Pension income"
    val privatePensionIncome = "Private pension income"
    val privatePensionContribution = "Private pension contributions"
    val marriage = "Marriage Allowance"

    val softwareSpecHeading = "Software specifications"
    val softwareType = "Software type"
    val compatibleWith = "Compatible with"
    val mobileApp = "Mobile App"
    val language = "Language"
    val desktopBased = "Desktop application"
    val microsoftWindows = "Microsoft Windows"
    val macOs = "Mac OS"
    val linux = "Linux"
    val android = "Android"
    val appleIOS = "Apple iOS"
    val english = "English"
  }

}
