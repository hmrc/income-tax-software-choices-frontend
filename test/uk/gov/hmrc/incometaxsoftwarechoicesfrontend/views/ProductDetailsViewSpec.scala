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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{IncomeAndDeduction, SoftwareVendorModel}
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

    object Filters {

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
    }

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

  val softwareVendorModelBase: SoftwareVendorModel = SoftwareVendorModel(
    name = "abc",
    url = "/url",
    email = "test@software-vendor-name-three.com",
    phone = "00000 000 000",
    website = "software-vendor-name-three.com",
    filters = Seq.empty,
    incomeAndDeductions = Seq.empty,
    accessibilityStatementLink = None
  )

  private val softwareVendorModelFull = softwareVendorModelBase
    .copy(incomeAndDeductions = incomeAndDeductionKeyToIncomeAndDeduction.values.toList) // All income and deductions
    .copy(filters = filterKeyToFilter.values.toList) // All filters
    .copy(accessibilityStatementLink = Some("software-vendor-accessibility.com"))

  val pricingRow = 1
  val incomeTypeRow = 2
  val compatibleWithRow = 3
  val mobileAppRow = 4
  val softwareTypeRow = 5
  val softwareForRow = 6
  val businessTypeRow = 7
  val softwareCompatibilityRow = 8
  val languageRow = 9
  val accessibilityRow = 10

  private def accessibilitySectionHeading(incomeFs: Boolean) = if (incomeFs) 4 else 3

  import ProductDetailsPage.Filters
  private def checkList(detailsSection: Element, rowNum: Int, rowText: String, cellTexts: String*) = {
    val row: Element = detailsSection.selectNth("div", rowNum)
    row.selectHead("dt").text shouldBe rowText
    val detail: Element = row.selectHead("dd")
    Seq.range(1, cellTexts.size + 1).map(detail.selectNth("p", _).text).sorted shouldBe cellTexts.toList.sorted
  }

  "ProductDetailsPage" when {
    "the vendor has everything" when {
      Seq(true, false).foreach(incomeFs =>
        s"the income and deductions feature switch is $incomeFs" must {
          val document: Document = createAndParseDocument(softwareVendorModelFull, incomeFs)
          "have a breadcrumb menu" which {
            "contains the guidance page" in {
              val link = document.selectNth(".govuk-breadcrumbs__list-item", 1).selectHead("a")
              link.text shouldBe "Guidance"
              link.attr("href") shouldBe appConfig.guidance
            }

            "contains the filter page" in {
              val link = document.selectNth(".govuk-breadcrumbs__list-item", 2).selectHead("a")
              link.text shouldBe "Filter"
              link.attr("href") shouldBe routes.SearchSoftwareController.show.url
            }

            "contains the current page" in {
              document.selectNth(".govuk-breadcrumbs__list-item", 3).text shouldBe softwareVendorModelFull.name
            }
          }

          "have a title" in {
            document.title shouldBe s"""${ProductDetailsPage.title} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"""
          }

          "display the vendor name heading" in {
            document.selectNth("h1", 1).text() shouldBe softwareVendorModelFull.name
          }

          "display the vendor contact details heading" in {
            document.selectNth("h2", 1).text() shouldBe s"${ProductDetailsPage.contactDetailsHeading}"
          }

          val vendorInformationSection = document.selectNth("dl", 1)
          "display the vendor email address" in {
            val row: Element = vendorInformationSection.selectNth(".govuk-summary-list__row", 1)
            row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsEmail}:"

            val link = row.selectHead("dd").selectHead("a")
            link.text shouldBe softwareVendorModelBase.email
            link.attr("href") shouldBe s"mailto:${softwareVendorModelBase.email}"
          }

          "display the vendor phone number" in {
            val row: Element = vendorInformationSection.selectNth(".govuk-summary-list__row", 2)
            row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsPhone}:"
            row.selectHead("dd").text shouldBe softwareVendorModelBase.phone
          }

          "display the vendor website" in {
            val row: Element = vendorInformationSection.selectNth(".govuk-summary-list__row", 3)
            row.selectHead("dt").text shouldBe s"${ProductDetailsPage.contactDetailsWebsite}:"

            val link = row.selectHead("dd").selectHead("a")
            link.text shouldBe softwareVendorModelBase.website
            link.attr("href") shouldBe softwareVendorModelBase.website
          }

          "have a product details heading" in {
            document.selectNth("h2", 2).text shouldBe ProductDetailsPage.productDetailsHeading
          }

          "display the heading for accessibility section" in {
            document.selectNth("h2", accessibilitySectionHeading(incomeFs)).text shouldBe ProductDetailsPage.accessibilityHeading
          }

          val detailsSection = document.selectNth("dl", 2)
          "display the pricing row" in {
            checkList(detailsSection, pricingRow, Filters.pricing, Filters.freeTrial, Filters.freeVersion, Filters.paidFor)
          }

          "display the income type row" in {
            checkList(detailsSection, incomeTypeRow, Filters.incomeType, Filters.overseasProperty, Filters.soleTrader, Filters.ukProperty)
          }

          "display the compatible with row" in {
            checkList(detailsSection, compatibleWithRow, Filters.compatibleWith, Filters.microsoftWindows, Filters.macOS)
          }

          "display the mobile app row" in {
            checkList(detailsSection, mobileAppRow, Filters.mobileApp, Filters.android, Filters.iOS)
          }

          "display the software type row" in {
            checkList(detailsSection, softwareTypeRow, Filters.softwareType, Filters.browserBased, Filters.applicationBased)
          }

          "display the software for row" in {
            checkList(detailsSection, softwareForRow, Filters.softwareFor, Filters.recordKeeping, Filters.bridging)
          }

          "display the business type row" in {
            checkList(detailsSection, businessTypeRow, Filters.businessType, Filters.individual, Filters.agent)
          }

          "display the software compatibility row" in {
            checkList(detailsSection, softwareCompatibilityRow, Filters.softwareCompatibility, Filters.incomeTax, Filters.vat)
          }

          "display the language row" in {
            checkList(detailsSection, languageRow, Filters.language, Filters.welsh)
          }

          "display the accessibility row" in {
            checkList(detailsSection, accessibilityRow, Filters.accessibility, Filters.visual, Filters.hearing, Filters.motor, Filters.cognitive)
          }

          val accessibilityStatementSection = document.selectNth("dl", 3)
          "display the link to the accessibility statement" in {
            val row: Element = accessibilityStatementSection
            row.selectHead("dt").text shouldBe ProductDetailsPage.accessibilityStatement
            row.selectHead("dd").text shouldBe "software-vendor-accessibility.com"
            row.selectHead("dd").selectHead("a").attr("href") shouldBe "software-vendor-accessibility.com"
          }
        }
      )
      "the software vendor does not have an accessibility statement" when {
        Seq(true, false).foreach(incomeFs =>
          s"the income and deductions feature switch is $incomeFs" must {

            val document = createAndParseDocument(softwareVendorModelBase, incomeFs)
            "not display the heading for the accessibility section" in {
              document
                .selectOptionally(s"h2:nth-of-type(${accessibilitySectionHeading(incomeFs)})") shouldBe None
            }
            "not display the link to the statement" in {
              document
                .selectOptionally(s"dl:nth-of-type(3)") shouldBe None
            }
          }
        )
      }

      "the income and deductions feature switch is on" must {
        val incomeFs = true
        "the vendor has full incomes and deductions supported" must {
          val document: Document = createAndParseDocument(softwareVendorModelFull, incomeFs)
          "have a income and deductions heading" in {
            document.selectNth("h2", 3).text shouldBe ProductDetailsPage.incomesAndDeductionsHeading
          }

          "have an income and deductions summary" in {
            document.selectHead("h2:nth-of-type(3) + p").text shouldBe ProductDetailsPage.numberCovered(
              softwareVendorModelBase.name,
              softwareVendorModelFull.incomeAndDeductions.length
            )
          }

          "display the incomes and deductions section with all values" in {
            document
              .mainContent
              .selectHead("ol")
              .selectSeq("li")
              .map(_.text).toList.sorted shouldBe ProductDetailsPage.allIncomeAndDeductions.toList.sorted
          }
        }

        "the vendor has minimal incomes and deductions supported" must {
          val document = createAndParseDocument(softwareVendorModelBase, true)
          "display the correct number of incomes and deductions covered" in {
            document
              .selectHead("h2:nth-of-type(3) + p")
              .text shouldBe ProductDetailsPage.numberCovered(softwareVendorModelBase.name, 0)
          }
          "display the section without values" in {
            document
              .mainContent
              .selectHead("ol")
              .selectSeq("li")
              .length shouldBe 0
          }
        }
      }

      "the income and deductions feature switch is off" when {
        val incomeFs = false
        "the vendor has no accessibility statement" must {
          val document: Document = createAndParseDocument(softwareVendorModelBase, incomeFs)
          "not have a income and deductions heading" in {
            document.selectNthOptionally("h2", 3) shouldBe None
          }
        }
        "the vendor has an accessibility statement" must {
          val document: Document = createAndParseDocument(softwareVendorModelFull, incomeFs)
          "have an accessibility heading" in {
            document.selectNth("h2", 3).text() shouldBe Filters.accessibility
          }
        }
      }
    }

    "the vendor has minimal filters" when {
      Seq(true, false).foreach(incomeFs =>
        s"the income and deductions feature switch is $incomeFs" must {
          val document = createAndParseDocument(softwareVendorModelBase, incomeFs)
          "display the software compatibility row" in {
            val row: Element = document
              .selectNth("dl", 2)
              .selectNth("div", 1)

            row.selectHead("dt").text shouldBe Filters.softwareCompatibility
            row.selectHead("dd").text shouldBe Filters.incomeTax
          }
          "display no other rows" in {
            document
              .selectNth("dl", 2)
              .selectOptionally("div:nth-of-type(2)") shouldBe None
          }
        }
      )
    }
  }

  private def page(vendorModel: SoftwareVendorModel, displayIncomeAndDeductionTypes: Boolean) = productDetailsPage(
    vendorModel, displayIncomeAndDeductionTypes
  )

  private def createAndParseDocument(vendorModel: SoftwareVendorModel, displayIncomeAndDeductionTypes: Boolean): Document =
    Jsoup.parse(page(vendorModel, displayIncomeAndDeductionTypes).body)

}
