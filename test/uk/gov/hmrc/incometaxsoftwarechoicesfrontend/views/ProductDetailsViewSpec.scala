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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.IncomeAndDeduction._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{IncomeAndDeduction, SoftwareVendorModel}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ProductDetailsPage

class ProductDetailsViewSpec extends ViewSpec {

  private val productDetailsPage = app.injector.instanceOf[ProductDetailsPage]

  object ProductDetailsPage {
    val title: String = "Choose the right software for your needs" // TODO what should the title be?

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

  val softwareVendorModelBase: SoftwareVendorModel = SoftwareVendorModel(
    name = "abc",
    email = Some("test@software-vendor-name-three.com"),
    phone = Some("00000 000 000"),
    website = "software-vendor-name-three.com",
    filters = Seq.empty,
    incomeAndDeductions = Seq.empty,
    accessibilityStatementLink = None
  )

  private val softwareVendorModelFull = softwareVendorModelBase
    .copy(incomeAndDeductions = incomeAndDeductionKeyToIncomeAndDeduction.values.toList) // All income and deductions
    .copy(filters = filterKeyToFilter.values.toList) // All filters


  "ProductDetailsPage" when {
    "the vendor has everything" which {

      val document: Document = createAndParseDocument(softwareVendorModelFull)

      "have a title" in {
        document.title shouldBe s"""${softwareVendorModelFull.name} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"""
      }

      "display the vendor name heading" in {
        document.selectNth("h1", 1).text() shouldBe softwareVendorModelFull.name
      }

      val vendorInformationSection = document.selectNth("dl", 1)
      "display the vendor website" in {
        val row: Element = vendorInformationSection.selectNth(".govuk-summary-list__row", 1)
        row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsWebsite}:"

        val link = row.selectHead("dd").selectHead("a")
        link.text shouldBe s"${softwareVendorModelBase.website} (opens in new tab)"
        link.attr("href") shouldBe softwareVendorModelBase.website
        link.attr("target") shouldBe "_blank"
      }

      "have a software features heading" in {
        document.selectNth("h2", 1).text shouldBe ProductDetailsPage.softwareFeaturesHeading
      }

      "display the features provided table" which {
        def getTable(table: Int): Element = document.selectHead(s".govuk-table:nth-of-type($table)")

        def getTableHeader(table: Int, col: Int): Element = getTable(table).selectHead(s"thead > tr > th.govuk-table__header:nth-of-type($col)")

        def checkTableHeader(table: Int, col1: String, col2: String): Assertion = {
          getTableHeader(table, 1).text shouldBe col1
          getTableHeader(table, 2).text shouldBe col2
        }

        def checkRow(table: Int, row: Int, field: String, included: Boolean): Assertion = {
          val status = if (included) "Included" else "Not Included"
          getTable(table).selectHead(s"tbody > tr:nth-child($row) > td:nth-child(1)").text shouldBe field
          getTable(table).selectHead(s"tbody > tr:nth-child($row) > td:nth-child(2)").text shouldBe status
        }

        "has the correct table headings" in {
          checkTableHeader(1, "Features provided", "Status")
          checkTableHeader(2, "Business income sources", "Status")
          checkTableHeader(3, "Personal income sources", "Status")
          checkTableHeader(4, "Deductions", "Status")
          checkTableHeader(5, "Pensions", "Status")
          checkTableHeader(6, "Allowances", "Status")
        }

        "displays all the rows" in {
          checkRow(1, 1, ProductDetailsPage.recordKeeping, included = true)
          checkRow(1, 2, ProductDetailsPage.bridging, included = true)
          checkRow(1, 3, ProductDetailsPage.quarterlyUpdates, included = true)
          checkRow(1, 4, ProductDetailsPage.saTaxReturn, included = true)
          checkRow(1, 5, ProductDetailsPage.standardUpdatePeriods, included = true)
          checkRow(1, 6, ProductDetailsPage.calendarUpdatePeriods, included = true)
          checkRow(2, 1, ProductDetailsPage.soleTrader, included = true)
          checkRow(2, 2, ProductDetailsPage.ukProperty, included = true)
          checkRow(2, 3, ProductDetailsPage.foreignProperty, included = true)
          checkRow(3, 1, ProductDetailsPage.cis, included = true)
          checkRow(3, 2, ProductDetailsPage.cgt, included = true)
          checkRow(3, 3, ProductDetailsPage.paye, included = true)
          checkRow(3, 4, ProductDetailsPage.foreignIncome, included = true)
          checkRow(3, 5, ProductDetailsPage.ukDividends, included = true)
          checkRow(3, 6, ProductDetailsPage.ukInterest, included = true)
          checkRow(4, 1, ProductDetailsPage.charitableGiving, included = true)
          checkRow(4, 2, ProductDetailsPage.student, included = true)
          checkRow(4, 3, ProductDetailsPage.class2NIC, included = true)
          checkRow(4, 4, ProductDetailsPage.childBenefitCharge, included = true)
          checkRow(5, 1, ProductDetailsPage.statePension, included = true)
          checkRow(5, 2, ProductDetailsPage.privatePensionIncome, included = true)
          checkRow(5, 3, ProductDetailsPage.privatePensionContribution, included = true)
          checkRow(6, 1, ProductDetailsPage.marriage, included = true)
        }
      }

    }
  }

  private def page(vendorModel: SoftwareVendorModel) =
    productDetailsPage(
      vendorModel, displayIncomeAndDeductionTypes = false, displayExtraPricingOptions = false, displayOverseasPropertyOption = true
    )

  private def createAndParseDocument(vendorModel: SoftwareVendorModel): Document =
    Jsoup.parse(page(vendorModel).body)

}
