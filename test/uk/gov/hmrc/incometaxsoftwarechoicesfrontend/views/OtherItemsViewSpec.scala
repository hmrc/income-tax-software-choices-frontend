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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.OtherItemsPage

class OtherItemsViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[OtherItemsPage]

  private val formError: FormError = FormError("otherItems", "other-items.error.nonEmpty")

  def page(hasError: Boolean = false): HtmlFormat.Appendable = view(
    otherItemsForm = if (hasError) {
      OtherItemsForm.form.withError(formError)
    } else {
      OtherItemsForm.form
    },
    postAction = testCall,
    backLink = testBackUrl
  )

  def document(hasError: Boolean = false): Document = Jsoup.parse(page(hasError).body)


  "OtherItemsPage" when {
    "there is an error" must {
      "have an error title" in {
        document(hasError = true).title() shouldBe s"Error: ${OtherItemsPageContent.title}"
      }
      "have an error summary" in {
        document(hasError = true).selectSeq(".govuk-error-summary").size shouldBe 1
        document(hasError = true).selectHead(".govuk-error-summary").text() should include("There is a problem")
        document(hasError = true).select(".govuk-error-summary__body > ul > li > a").attr("href") shouldBe "#otherItems"
      }
    }

    "there is no error" must {
      "have a title" in {
        document().title() shouldBe OtherItemsPageContent.title
      }
      "have a back link" in {
        document().selectHead(".govuk-back-link").attr("href") shouldBe testBackUrl
      }
      "have a paragraph" in {
        document().mainContent.selectNth("p", 1).text shouldBe OtherItemsPageContent.para
      }
      "have a form" which {
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
            legend = OtherItemsPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.privatePensionContributions,
            value = "payments-into-a-private-pension",
          )
        }
        "has a checkbox for Charitable giving" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 2,
            legend = OtherItemsPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.charitableGiving,
            value = "charitable-giving",
          )
        }
        "has a checkbox for Capital Gains Tax" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 3,
            legend = OtherItemsPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.capitalGainsTax,
            value = "capital-gains-tax",
          )
        }
        "has a checkbox for Student Loan" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 4,
            legend = OtherItemsPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.studentLoan,
            value = "student-loans",
          )
        }
        "has a checkbox for Marriage Allowance" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 5,
            legend = OtherItemsPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.marriageAllowance,
            value = "marriage-allowance",
          )
        }
        "has a checkbox for Voluntary Class 2 National Insurance" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 6,
            legend = OtherItemsPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.voluntaryClass2Nic,
            value = "voluntary-class-2-national-insurance",
          )
        }
        "has a checkbox for High Income Child Benefit Charge" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 7,
            legend = OtherItemsPageContent.legend,
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
            checkbox = 9,
            legend = OtherItemsPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "otherItems[]",
            label = OtherItemsPageContent.noneOfThese,
            value = "none",
            isExclusive = true
          )
        }
        "has a continue button" in {
          form.selectNth(".govuk-button", 1).text() shouldBe OtherItemsPageContent.continue
        }
      }
    }
  }
}

private object OtherItemsPageContent {
  val title = s"What else do you need to tell HMRC? - ${PageContentBase.title} - GOV.UK"
  val legend = "What else do you need to tell HMRC?"
  val para = "These are other items you’ll need to include in your tax return if you have them."
  val hint = "Select all that apply"
  val privatePensionContributions = "Private pension contributions"
  val charitableGiving = "Charitable giving"
  val capitalGainsTax = "Capital Gains"
  val studentLoan = "Student loan"
  val marriageAllowance = "Marriage Allowance"
  val voluntaryClass2Nic = "Voluntary Class 2 National Insurance"
  val highIncomeChildBenefitCharge = "High Income Child Benefit Charge"
  val noneOfThese = "None of these"
  val continue = "Continue"
}