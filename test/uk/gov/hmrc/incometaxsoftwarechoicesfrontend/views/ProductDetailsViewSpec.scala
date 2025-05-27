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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.IncomeAndDeduction._
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
    incomeAndDeductions = Seq.empty,
    accessibilityStatementLink = None
  )

  private val softwareVendorModelFull = softwareVendorModelBase
    .copy(name = "abc full")
    .copy(incomeAndDeductions = incomeAndDeductionKeyToIncomeAndDeduction.values.toList) // All income and deductions
    .copy(filters = filterKeyToFilter.values.toList) // All filters

  private val softwareVendorModelMinimal = softwareVendorModelBase
    .copy(name = "abc minimal")
    .copy(incomeAndDeductions = Seq(
      UKInterest))
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
        document.title shouldBe s"""${softwareVendorModelFull.name} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"""
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
          checkTableHeader(table(3), "Personal income sources", "Status")
          checkTableHeader(table(4), "Deductions", "Status")
          checkTableHeader(table(5), "Pensions", "Status")
          checkTableHeader(table(6), "Allowances", "Status")
        }

        "displays all the rows" in {
          checkRow(table(1), 1, ProductDetailsPage.recordKeeping, included = true)
          checkRow(table(1), 2, ProductDetailsPage.bridging, included = true)
          checkRow(table(1), 3, ProductDetailsPage.quarterlyUpdates, included = true)
          checkRow(table(1), 4, ProductDetailsPage.saTaxReturn, included = true)
          checkRow(table(1), 5, ProductDetailsPage.standardUpdatePeriods, included = true)
          checkRow(table(1), 6, ProductDetailsPage.calendarUpdatePeriods, included = true)
          checkRow(table(2), 1, ProductDetailsPage.soleTrader, included = true)
          checkRow(table(2), 2, ProductDetailsPage.ukProperty, included = true)
          checkRow(table(2), 3, ProductDetailsPage.foreignProperty, included = true)
          checkRow(table(3), 1, ProductDetailsPage.cis, included = true)
          checkRow(table(3), 2, ProductDetailsPage.cgt, included = true)
          checkRow(table(3), 3, ProductDetailsPage.paye, included = true)
          checkRow(table(3), 4, ProductDetailsPage.foreignIncome, included = true)
          checkRow(table(3), 5, ProductDetailsPage.ukDividends, included = true)
          checkRow(table(3), 6, ProductDetailsPage.ukInterest, included = true)
          checkRow(table(4), 1, ProductDetailsPage.charitableGiving, included = true)
          checkRow(table(4), 2, ProductDetailsPage.student, included = true)
          checkRow(table(4), 3, ProductDetailsPage.class2NIC, included = true)
          checkRow(table(4), 4, ProductDetailsPage.childBenefitCharge, included = true)
          checkRow(table(5), 1, ProductDetailsPage.statePension, included = true)
          checkRow(table(5), 2, ProductDetailsPage.privatePensionIncome, included = true)
          checkRow(table(5), 3, ProductDetailsPage.privatePensionContribution, included = true)
          checkRow(table(6), 1, ProductDetailsPage.marriage, included = true)
        }
      }
    }

    "the vendor has minimal features" which {

      val document: Document = createAndParseDocument(softwareVendorModelMinimal)

      def table(index: Int): Element = document.getTable(index)

      "have a title" in {
        document.title shouldBe s"""${softwareVendorModelMinimal.name} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"""
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
          checkTableHeader(table(3), "Personal income sources", "Status")
          checkTableHeader(table(4), "Deductions", "Status")
          checkTableHeader(table(5), "Pensions", "Status")
          checkTableHeader(table(6), "Allowances", "Status")
        }

        "displays all the rows" in {
          checkRow(table(1), 1, ProductDetailsPage.recordKeeping, included = false)
          checkRow(table(1), 2, ProductDetailsPage.bridging, included = false)
          checkRow(table(1), 3, ProductDetailsPage.quarterlyUpdates, included = false)
          checkRow(table(1), 4, ProductDetailsPage.saTaxReturn, included = false)
          checkRow(table(1), 5, ProductDetailsPage.standardUpdatePeriods, included = false)
          checkRow(table(1), 6, ProductDetailsPage.calendarUpdatePeriods, included = false)
          checkRow(table(2), 1, ProductDetailsPage.soleTrader, included = true)
          checkRow(table(2), 2, ProductDetailsPage.ukProperty, included = false)
          checkRow(table(2), 3, ProductDetailsPage.foreignProperty, included = false)
          checkRow(table(3), 1, ProductDetailsPage.cis, included = false)
          checkRow(table(3), 2, ProductDetailsPage.cgt, included = false)
          checkRow(table(3), 3, ProductDetailsPage.paye, included = false)
          checkRow(table(3), 4, ProductDetailsPage.foreignIncome, included = false)
          checkRow(table(3), 5, ProductDetailsPage.ukDividends, included = false)
          checkRow(table(3), 6, ProductDetailsPage.ukInterest, included = true)
          checkRow(table(4), 1, ProductDetailsPage.charitableGiving, included = false)
          checkRow(table(4), 2, ProductDetailsPage.student, included = false)
          checkRow(table(4), 3, ProductDetailsPage.class2NIC, included = false)
          checkRow(table(4), 4, ProductDetailsPage.childBenefitCharge, included = false)
          checkRow(table(5), 1, ProductDetailsPage.statePension, included = false)
          checkRow(table(5), 2, ProductDetailsPage.privatePensionIncome, included = false)
          checkRow(table(5), 3, ProductDetailsPage.privatePensionContribution, included = false)
          checkRow(table(6), 1, ProductDetailsPage.marriage, included = false)
        }
      }
    }

    "the vendor does not have any features" which {

      val document: Document = createAndParseDocument(softwareVendorModelBase)

      def table(index: Int): Element = document.getTable(index)

      "have a title" in {
        document.title shouldBe s"""${softwareVendorModelBase.name} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"""
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
          checkTableHeader(table(3), "Personal income sources", "Status")
          checkTableHeader(table(4), "Deductions", "Status")
          checkTableHeader(table(5), "Pensions", "Status")
          checkTableHeader(table(6), "Allowances", "Status")
        }

        "displays all the rows" in {
          checkRow(table(1), 1, ProductDetailsPage.recordKeeping, included = false)
          checkRow(table(1), 2, ProductDetailsPage.bridging, included = false)
          checkRow(table(1), 3, ProductDetailsPage.quarterlyUpdates, included = false)
          checkRow(table(1), 4, ProductDetailsPage.saTaxReturn, included = false)
          checkRow(table(1), 5, ProductDetailsPage.standardUpdatePeriods, included = false)
          checkRow(table(1), 6, ProductDetailsPage.calendarUpdatePeriods, included = false)
          checkRow(table(2), 1, ProductDetailsPage.soleTrader, included = false)
          checkRow(table(2), 2, ProductDetailsPage.ukProperty, included = false)
          checkRow(table(2), 3, ProductDetailsPage.foreignProperty, included = false)
          checkRow(table(3), 1, ProductDetailsPage.cis, included = false)
          checkRow(table(3), 2, ProductDetailsPage.cgt, included = false)
          checkRow(table(3), 3, ProductDetailsPage.paye, included = false)
          checkRow(table(3), 4, ProductDetailsPage.foreignIncome, included = false)
          checkRow(table(3), 5, ProductDetailsPage.ukDividends, included = false)
          checkRow(table(3), 6, ProductDetailsPage.ukInterest, included = false)
          checkRow(table(4), 1, ProductDetailsPage.charitableGiving, included = false)
          checkRow(table(4), 2, ProductDetailsPage.student, included = false)
          checkRow(table(4), 3, ProductDetailsPage.class2NIC, included = false)
          checkRow(table(4), 4, ProductDetailsPage.childBenefitCharge, included = false)
          checkRow(table(5), 1, ProductDetailsPage.statePension, included = false)
          checkRow(table(5), 2, ProductDetailsPage.privatePensionIncome, included = false)
          checkRow(table(5), 3, ProductDetailsPage.privatePensionContribution, included = false)
          checkRow(table(6), 1, ProductDetailsPage.marriage, included = false)
        }
      }
    }
  }

  private def page(vendorModel: SoftwareVendorModel) =
    productDetailsPage(vendorModel)

  private def createAndParseDocument(vendorModel: SoftwareVendorModel): Document =
    Jsoup.parse(page(vendorModel).body)

  object ProductDetailsPage {
    val paragraph = "Visit the company’s website to check if the software is right for you"
    val contactDetailsWebsite = "Website"

    val softwareFeaturesHeading = "Software features"

    val recordKeeping = "Record keeping"
    val bridging = "Bridging"
    val quarterlyUpdates = "Quarterly updates"
    val saTaxReturn = "Self Assessment tax return"
    val standardUpdatePeriods = "Standard update periods"
    val calendarUpdatePeriods = "Calendar update periods"
    val soleTrader = "Sole trader"
    val ukProperty = "UK property"
    val foreignProperty = "Foreign property"
    val cis = "Construction Industry Scheme"
    val cgt = "Capital Gains Tax"
    val paye = "Employment (PAYE)"
    val foreignIncome = "Foreign Income"
    val ukDividends = "UK dividends"
    val ukInterest = "UK interest"
    val charitableGiving = "Charitable giving"
    val student = "Student Loan"
    val class2NIC = "Voluntary Class 2 National Insurance"
    val childBenefitCharge = "High Income Child Benefit Charge"
    val statePension = "State pension"
    val privatePensionIncome = "Private pension incomes"
    val privatePensionContribution = "Private pension contributions"
    val marriage = "Marriage Allowance"

  }


}
