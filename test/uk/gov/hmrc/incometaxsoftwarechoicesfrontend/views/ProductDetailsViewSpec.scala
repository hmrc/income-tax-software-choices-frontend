/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.IncomeAndDeduction._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{IncomeAndDeduction, SoftwareVendorModel, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ProductDetailsPage

class ProductDetailsViewSpec extends ViewSpec {

  private val productDetailsPage = app.injector.instanceOf[ProductDetailsPage]

  object ProductDetailsPage {
    val title: String = "Choose the right software for your needs"

    val contactDetailsHeading = "Find out more or get help and support"
    val contactDetailsEmail = "Email"
    val contactDetailsPhone = "Call"
    val contactDetailsWebsite = "Visit website"

    val productDetailsHeading: String = "Product details"

    val pricing: String = "Pricing"
    val freeTrial: String = "Free trial"
    val freeVersion: String = "Free version"
    val paidFor: String = "Paid for"

    val incomeType: String = "Income type"
    val soleTrader: String = "Sole trader"
    val ukProperty: String = "UK property"
    val overseasProperty: String = "Overseas property"

    val compatibleWith: String = "Compatible with"
    val microsoftWindows: String = "Microsoft Windows"
    val macOS: String = "Mac OS"

    val mobileApp: String = "Mobile app"
    val android: String = "Android"
    val iOS: String = "iOS"

    val softwareType: String = "Software type"
    val browserBased: String = "Browser based"
    val applicationBased: String = "Application based"

    val softwareFor: String = "Software for"
    val recordKeeping: String = "Record keeping"
    val bridging: String = "Bridging"

    val businessType: String = "Business type"
    val individual: String = "Individual"
    val agent: String = "Agent"

    val softwareCompatibility: String = "Software compatibility"
    val incomeTax: String = "Income tax"
    val vat: String = "VAT"

    val language: String = "Language"
    val welsh: String = "Welsh"

    val accessibility: String = "Accessibility"
    val visual: String = "Visual"
    val hearing: String = "Hearing"
    val motor: String = "Motor"
    val cognitive: String = "Cognitive"

    val incomesAndDeductionsHeading: String = "Self Assessment income and deduction types this software covers"
    val blindPersonsAllowance: String = "Blind Person’s Allowance"
    val capitalGainsTax: String = "Capital Gains Tax"
    val complexPartnerships: String = "Complex partnerships"
    val constructionIndustryScheme: String = "Construction Industry Scheme (CIS)"
    val employment: String = "Employment"
    val foreignIncome: String = "Foreign income"
    val giftAid: String = "Gift Aid"
    val highIncomeChildBenefit: String = "High Income Child Benefit"
    val investments: String = "Investments"
    val lloydsUnderwriters: String = "Lloyds underwriters"
    val marriageAllowance: String = "Marriage Allowance"
    val marriedAllowance: String = "Married Allowance"
    val membersOfParliament: String = "Members of Parliament"
    val ministersOfReligion: String = "Ministers of religion"
    val partnerIncome: String = "Partner income"
    val paye: String = "PAYE"
    val pensionContributions: String = "Pension contributions"
    val pensions: String = "Pensions"
    val propertyBusiness: String = "Property business"
    val residenceAndRemittance: String = "Residence and Remittance"
    val saAdditionalIncome: String = "SA additional income"
    val selfEmployment: String = "Self employment"
    val simplePartnerships: String = "Simple partnerships"
    val statePension: String = "State Pension"
    val studentLoans: String = "Student loans"
    val ukDividends: String = "UK dividends"
    val ukInterest: String = "UK interest"

    val allIncomeAndDeductions: Seq[String] = Seq(
      blindPersonsAllowance,
      capitalGainsTax,
      complexPartnerships,
      constructionIndustryScheme,
      employment,
      foreignIncome,
      giftAid,
      highIncomeChildBenefit,
      investments,
      lloydsUnderwriters,
      marriageAllowance,
      marriedAllowance,
      membersOfParliament,
      ministersOfReligion,
      partnerIncome,
      paye,
      pensionContributions,
      pensions,
      propertyBusiness,
      residenceAndRemittance,
      saAdditionalIncome,
      selfEmployment,
      simplePartnerships,
      statePension,
      studentLoans,
      ukDividends,
      ukInterest
    )

    def numberCovered(name: String, covered: Int): String = {
      val total = IncomeAndDeduction.incomeAndDeductionKeyToIncomeAndDeduction.toSeq.length
      s"$name covers $covered out of $total:"
    }

    val accessibilityHeading: String = "Accessibility"
    val accessibilityStatement: String = "Accessibility Statement"
  }

  val softwareVendorModelFull: SoftwareVendorModel = SoftwareVendorModel(
    name = "abc",
    url = "/url",
    email = "test@software-vendor-name-three.com",
    phone = "00000 000 000",
    website = "software-vendor-name-three.com",
    filters = Seq(
      FreeTrial,
      FreeVersion,
      PaidFor,
      SoleTrader,
      UkProperty,
      OverseasProperty,
      Individual,
      Agent,
      MicrosoftWindows,
      MacOS,
      Android,
      AppleIOS,
      BrowserBased,
      ApplicationBased,
      Welsh,
      Visual,
      Hearing,
      Motor,
      Cognitive,
      RecordKeeping,
      Bridging,
      Vat
    ),
    incomeAndDeductions = Seq(
      BlindPersonsAllowance,
      CapitalGainsTax,
      ComplexPartnerships,
      ConstructionIndustryScheme,
      Employment,
      ForeignIncome,
      GiftAid,
      HighIncomeChildBenefit,
      Investments,
      LloydsUnderwriters,
      MarriageAllowance,
      MarriedAllowance,
      MemberOfParliament,
      MinisterOfReligion,
      PartnerIncome,
      PAYE,
      PensionContributions,
      Pensions,
      PropertyBusiness,
      ResidenceAndRemittance,
      SAAdditionalIncome,
      SelfEmployment,
      SimplePartnerships,
      StatePension,
      StudentLoans,
      UKDividends,
      UKInterest
    ),
    accessibilityStatementLink = Some("software-vendor-accessibility.com")
  )

  "ProductDetailsPage" must {
    "have a breadcrumb menu" which {
      "contains the guidance page" in {
        val link = document().selectNth(".govuk-breadcrumbs__list-item", 1).selectHead("a")
        link.text shouldBe "Guidance"
        link.attr("href") shouldBe appConfig.guidance
      }

      "contains the filter page" in {
        val link = document().selectNth(".govuk-breadcrumbs__list-item", 2).selectHead("a")
        link.text shouldBe "Filter"
        link.attr("href") shouldBe routes.SearchSoftwareController.show.url
      }

      "contains the current page" in {
        document().selectNth(".govuk-breadcrumbs__list-item", 3).text shouldBe "Details"
      }
    }

    "have a title" in {
      document().title shouldBe s"""${ProductDetailsPage.title} - Find software for Making Tax Digital for Income Tax - GOV.UK"""
    }

    "display the vendor contact details heading" in {
      document().selectNth("h2", 1).text() shouldBe s"${ProductDetailsPage.contactDetailsHeading}"
    }

    "display the vendor email address" in {
      val row: Element = document().selectNth("dl", 1).selectNth(".govuk-summary-list__row", 1)
      row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsEmail}:"

      val link = row.selectHead("dd").selectHead("a")
      link.text shouldBe softwareVendorModelFull.email
      link.attr("href") shouldBe s"mailto:${softwareVendorModelFull.email}"
    }

    "display the vendor phone number" in {
      val row: Element = document().selectNth("dl", 1).selectNth(".govuk-summary-list__row", 2)
      row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsPhone}:"
      row.selectHead("dd").text shouldBe softwareVendorModelFull.phone
    }

    "display the vendor website" in {
      val row: Element = document().selectNth("dl", 1).selectNth(".govuk-summary-list__row", 3)
      row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsWebsite}:"

      val link = row.selectHead("dd").selectHead("a")
      link.text shouldBe softwareVendorModelFull.website
      link.attr("href") shouldBe softwareVendorModelFull.website
    }

    "have a product details heading" in {
      document().selectNth("h2", 2).text shouldBe ProductDetailsPage.productDetailsHeading
    }

    "have a income and deductions heading" in {
      document().selectNth("h2", 3).text shouldBe ProductDetailsPage.incomesAndDeductionsHeading
    }

    "have an income and deductions summary" when {
      "the vendor has a full set of incomes and deductions supported" in {
        document().selectHead("h2:nth-of-type(3) + p").text shouldBe ProductDetailsPage.numberCovered(
          softwareVendorModelFull.name,
          softwareVendorModelFull.incomeAndDeductions.length
        )
      }
      "the vendor has minimal incomes and deductions supported" in {
        document(softwareVendorModelFull.copy(incomeAndDeductions = Seq.empty))
          .selectHead("h2:nth-of-type(3) + p")
          .text shouldBe ProductDetailsPage.numberCovered(softwareVendorModelFull.name, 0)
      }
    }

  }

  "Product details page" when {
    "the software vendor has full product details" should {
      "display the pricing row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 1)
        row.selectHead("dt").text shouldBe ProductDetailsPage.pricing

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.freeTrial
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.freeVersion
        detail.selectNth("p", 3).text shouldBe ProductDetailsPage.paidFor
      }
      "display the income type row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 2)
        row.selectHead("dt").text shouldBe ProductDetailsPage.incomeType

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.soleTrader
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.ukProperty
        detail.selectNth("p", 3).text shouldBe ProductDetailsPage.overseasProperty
      }
      "display the compatible with row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 3)
        row.selectHead("dt").text shouldBe ProductDetailsPage.compatibleWith

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.microsoftWindows
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.macOS
      }
      "display the mobile app row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 4)
        row.selectHead("dt").text shouldBe ProductDetailsPage.mobileApp

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.android
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.iOS
      }
      "display the software type row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 5)
        row.selectHead("dt").text shouldBe ProductDetailsPage.softwareType

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.browserBased
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.applicationBased
      }
      "display the software for row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 6)
        row.selectHead("dt").text shouldBe ProductDetailsPage.softwareFor

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.recordKeeping
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.bridging
      }
      "display the business type row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 7)
        row.selectHead("dt").text shouldBe ProductDetailsPage.businessType

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.individual
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.agent
      }
      "display the software compatibility row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 8)
        row.selectHead("dt").text shouldBe ProductDetailsPage.softwareCompatibility

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.incomeTax
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.vat
      }
      "display the language row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 9)
        row.selectHead("dt").text shouldBe ProductDetailsPage.language

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.welsh
      }
      "display the accessibility row" in {
        val row: Element = document().selectNth("dl", 2).selectNth("div", 10)
        row.selectHead("dt").text shouldBe ProductDetailsPage.accessibility

        val detail: Element = row.selectHead("dd")
        detail.selectNth("p", 1).text shouldBe ProductDetailsPage.visual
        detail.selectNth("p", 2).text shouldBe ProductDetailsPage.hearing
        detail.selectNth("p", 3).text shouldBe ProductDetailsPage.motor
        detail.selectNth("p", 4).text shouldBe ProductDetailsPage.cognitive
      }
    }
    "the software vendor has minimal product details" should {
      "display the software compatibility row" in {
        val row: Element = document(softwareVendorModelFull.copy(filters = Seq.empty[VendorFilter]))
          .selectNth("dl", 2)
          .selectNth("div", 1)

        row.selectHead("dt").text shouldBe ProductDetailsPage.softwareCompatibility
        row.selectHead("dd").text shouldBe ProductDetailsPage.incomeTax
      }
      "display no other rows" in {
        document(softwareVendorModelFull.copy(filters = Seq.empty[VendorFilter]))
          .selectNth("dl", 2)
          .selectOptionally("div:nth-of-type(2)") shouldBe None
      }
    }
    "the software vendor has a complete set of supported incomes and deductions" should {
      "display the incomes and deductions section with all values" in {
        document()
          .mainContent
          .selectHead("ol")
          .selectSeq("li")
          .map(_.text) shouldBe ProductDetailsPage.allIncomeAndDeductions
      }
    }
    "the software vendor has a minimal set of supported incomes and deductions" should {
      "display the section without values" in {
        document(softwareVendorModelFull.copy(incomeAndDeductions = Seq.empty))
          .mainContent
          .selectHead("ol")
          .selectSeq("li")
          .length shouldBe 0
      }
    }
    "the software vendor has an accessibility statement" should {
      "display the heading for accessibility section" in {
        document().selectNth("h2", 4).text shouldBe ProductDetailsPage.accessibilityHeading
      }
      "display the link to the statement" in {
        val row: Element = document()
          .selectNth("dl", 3)
        row.selectHead("dt").text shouldBe ProductDetailsPage.accessibilityStatement
        row.selectHead("dd").text shouldBe "software-vendor-accessibility.com"
        row.selectHead("dd").selectHead("a").attr("href") shouldBe "software-vendor-accessibility.com"
      }
    }
    "the software vendor does not have an accessibility statement" should {
      "not display the heading for the accessibility section" in {
        document(softwareVendorModelFull.copy(accessibilityStatementLink = None))
          .selectOptionally("h2:nth-of-type(4)") shouldBe None
      }
      "not display the link to the statement" in {
        document(softwareVendorModelFull.copy(accessibilityStatementLink = None))
          .selectOptionally("dl:nth-of-type(3)") shouldBe None
      }
    }
  }

  private def page(vendorModel: SoftwareVendorModel) = productDetailsPage(
    vendorModel
  )

  private def document(vendorModel: SoftwareVendorModel = softwareVendorModelFull): Document = Jsoup.parse(page(vendorModel).body)

}
