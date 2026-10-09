/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.data.FormError
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.OtherItemsForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.OtherItemsView

class OtherItemsViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[OtherItemsView]
  private val formEmptyIndividual: FormError = FormError("otherItems", "other-items.error.non-empty.individual")
  private val formEmptyAgent: FormError = FormError("otherItems", "other-items.error.non-empty.agent")
  private val formNoneOnly: FormError = FormError("otherItems", "other-items.error.invalid-selection")
  private val SoftwareName = "Bright"

  def page(hasError: Boolean = false, isAgent: Boolean = false): HtmlFormat.Appendable = {
    val form = (hasError, isAgent) match {
      case (true, true) =>
        OtherItemsForm.form("agent")
          .withError(formEmptyAgent)
          .withError(formNoneOnly)
      case (true, false) =>
        OtherItemsForm.form("individual")
          .withError(formEmptyIndividual)
          .withError(formNoneOnly)
      case (false, _) =>
        OtherItemsForm.form("individual")
    }
    view(
      otherItemsForm = form,
      postAction = testCall,
      backLink = testBackUrl,
      softwareName = Some(SoftwareName),
      userTypeString = if (isAgent) "agent" else "individual"
    )
  }

  def document(hasError: Boolean = false, isAgent: Boolean = false): Document = Jsoup.parse(page(hasError, isAgent).body)


  "OtherItemsPage" when {
    "there is an error with an individual's form" must {
      val individualErrorForm = document(hasError = true)
      "have an error title" in {
        individualErrorForm.title() shouldBe s"Error: ${OtherItemsPageContent.headingIndividual} ${OtherItemsPageContent.titleSuffix}"
      }
      "have an error summary" in {
        individualErrorForm.selectSeq(".govuk-error-summary").size shouldBe 1
        val errorText = individualErrorForm.selectHead(".govuk-error-summary").text()
        errorText should include("There is a problem")
        errorText should include("Select items you need to submit with your tax return or select ‘none of these’")
        errorText should include("Select items or select ‘none of these’")
        individualErrorForm.select(".govuk-error-summary__body > ul > li > a").attr("href") shouldBe "#otherItems"
      }
    }

    "there is an error with an agent's form" must {
      val agentErrorForm = document(hasError = true, isAgent = true)
      "have an error title" in {
        agentErrorForm.title() shouldBe s"Error: ${OtherItemsPageContent.headingAgent} ${OtherItemsPageContent.titleSuffix}"
      }
      "have an error summary including the agent-specific error" in {
        agentErrorForm.selectHead(".govuk-error-summary").text() should include("Select items that need to be submitted with your client’s tax return or select ‘none of these’")
      }
    }

    "there is no error" must {
      "have a title" in {
        document().title() shouldBe s"${OtherItemsPageContent.headingIndividual} ${OtherItemsPageContent.titleSuffix}"
        document(isAgent = true).title() shouldBe s"${OtherItemsPageContent.headingAgent} ${OtherItemsPageContent.titleSuffix}"
      }
      "have a software name caption" in {
        document().mainContent.selectHead("span.govuk-caption-l").text() shouldBe SoftwareName
      }
      "have a paragraph" in {
        document().mainContent.select("p").get(0).text shouldBe OtherItemsPageContent.paraIndividual
        document(isAgent = true).mainContent.select("p").get(0).text shouldBe OtherItemsPageContent.paraAgent
      }
      "have a back link" in {
        document().selectHead(".govuk-back-link").attr("href") shouldBe testBackUrl
      }
      "have a correctly-formatted form (for an individual)" which {
        def form: Element = document().mainContent.selectHead("form")

        "has the correct method and action" in {
          form.attr("method") shouldBe testCall.method
          form.attr("action") shouldBe testCall.url
        }
        "has a hint" in {
          val fieldSet = form.selectHead("fieldset")
          val hint = fieldSet.selectHead(".govuk-hint")

          hint.text shouldBe OtherItemsPageContent.hint
          fieldSet.attr("aria-describedby") should include(hint.attr("id"))
        }
        "has a checkbox for Private pension contributions" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 1,
            legend = OtherItemsPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.privatePensionContributions,
            value = "payments-into-a-private-pension",
          )
        }
        "has a checkbox for construction-industry-scheme" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 2,
            legend = OtherItemsPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.constructionIndustryScheme,
            value = "construction-industry-scheme",
          )
        }
        "has a checkbox for Charitable giving" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 3,
            legend = OtherItemsPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.charitableGiving,
            value = "charitable-giving",
          )
        }
        "has a checkbox for Capital Gains Tax" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 4,
            legend = OtherItemsPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.capitalGainsTax,
            value = "capital-gains-tax",
          )
        }
        "has a checkbox for Student Loan" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 5,
            legend = OtherItemsPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.studentLoan,
            value = "student-loans",
          )
        }
        "has a checkbox for Marriage Allowance" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 6,
            legend = OtherItemsPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.marriageAllowance,
            value = "marriage-allowance",
          )
        }
        "has a checkbox for Voluntary Class 2 National Insurance" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 7,
            legend = OtherItemsPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.voluntaryClass2Nic,
            value = "voluntary-class-2-national-insurance",
          )
        }
        "has a checkbox for High Income Child Benefit Charge" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 8,
            legend = OtherItemsPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.highIncomeChildBenefitCharge,
            value = "high-income-child-benefit-charge",
          )
        }
        "has a divider checkbox" in {
          form.select(".govuk-checkboxes__divider").text shouldBe "or"
        }
        "has a checkbox for None of these" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 10,
            legend = OtherItemsPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.noneOfThese,
            value = "none",
            isExclusive = true
          )
        }

        "have the correct legend for an agent form" in {
          val form = document(isAgent = true).mainContent.selectHead("form")
          val fieldSet = form.selectHead("fieldset")
          val legend = fieldSet.selectHead("legend")

          legend.text shouldBe OtherItemsPageContent.headingAgent
          legend.hasClass("govuk-visually-hidden") shouldBe true
        }

        "has a continue button" in {
          form.selectNth(".govuk-button", 1).text() shouldBe OtherItemsPageContent.continue
        }
      }
    }
  }
}

private object OtherItemsPageContent {
  val titleSuffix = s"- ${PageContentBase.title} - GOV.UK"
  val headingIndividual = "Which of these items do you need to submit with your tax return?"
  val headingAgent = "Which of these items need to be submitted with your client’s tax return?"
  val paraIndividual = "If you expect the items you submit to change, include your current and future items."
  val paraAgent = "If the items submitted are expected to change, include their current and future items."
  val hint = "Select all that apply"
  val privatePensionContributions = "Private pension contributions"
  val constructionIndustryScheme = "Construction Industry Scheme"
  val charitableGiving = "Charitable giving"
  val capitalGainsTax = "Capital Gains"
  val studentLoan = "Student loan"
  val marriageAllowance = "Marriage Allowance"
  val voluntaryClass2Nic = "Voluntary Class 2 National Insurance"
  val highIncomeChildBenefitCharge = "High Income Child Benefit Charge"
  val noneOfThese = "None of these"
  val continue = "Continue"
}