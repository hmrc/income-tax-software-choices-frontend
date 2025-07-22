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
import uk.gov.hmrc.govukfrontend.views.Aliases.ExclusiveCheckbox
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.AdditionalIncomeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.AdditionalIncomeSourcePage

class AdditionalIncomeSourceViewSpec extends ViewSpec {
  private val view = app.injector.instanceOf[AdditionalIncomeSourcePage]

  private val formEmpty: FormError = FormError("additionalIncome", "additional.income.source.error-non-empty")
  private val formNoneOnly: FormError = FormError("additionalIncome", "additional.income.source.error-none-only")

  def page(hasError: Boolean = false): HtmlFormat.Appendable = {
    val form = if (hasError) {
      AdditionalIncomeForm.form
        .withError(formEmpty)
        .withError(formNoneOnly)
    } else {
      AdditionalIncomeForm.form
    }
    view(
      additionalIncomeForm = form,
      postAction = testCall,
      backUrl = testBackUrl
    )
  }

  def document(hasError: Boolean = false): Document = Jsoup.parse(page(hasError).body)

  "AdditionalIncomePage" when {
    "there is an error" must {
      "have an error title" in {
        document(hasError = true).title() shouldBe s"Error: ${AdditionalIncomeSourcesPageContent.title}"
      }
      "have an error summary" in {
        document(hasError = true).selectSeq(".govuk-error-summary").size shouldBe 1
        document(hasError = true).selectHead(".govuk-error-summary").text() should include("There is a problem")
        document(hasError = true).select(".govuk-error-summary__body > ul > li > a").attr("href") shouldBe "#additionalIncome"
      }
    }

    "there is no error" must {
      "have a title" in {
        document().title() shouldBe AdditionalIncomeSourcesPageContent.title
      }
      "have a paragraph" in {
        document().mainContent.selectNth("p", 1).text shouldBe AdditionalIncomeSourcesPageContent.para
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

          hint.text shouldBe AdditionalIncomeSourcesPageContent.hint
          fieldSet.attr("aria-describedby") should include(hint.attr("id"))
        }
        "has a checkbox for uk-interest" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 1,
            legend = AdditionalIncomeSourcesPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.ukInterest,
            value = "uk-interest",
          )
        }
        "has a checkbox for construction-industry-scheme" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 2,
            legend = AdditionalIncomeSourcesPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.constructionIndustryScheme,
            value = "construction-industry-scheme",
          )
        }
        "has a checkbox for employment" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 3,
            legend = AdditionalIncomeSourcesPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.employment,
            value = "employment",
          )
        }
        "has a checkbox for uk-dividends" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 4,
            legend = AdditionalIncomeSourcesPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.ukDividends,
            value = "uk-dividends",
          )
        }
        "has a checkbox for state-pension-income" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 5,
            legend = AdditionalIncomeSourcesPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.statePension,
            value = "state-pension-income",
          )
        }
        "has a checkbox for private-pension-income" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 6,
            legend = AdditionalIncomeSourcesPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.privatePension,
            value = "private-pension-income",
          )
        }
        "has a checkbox for foreign-dividends" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 7,
            legend = AdditionalIncomeSourcesPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.foreignDividends,
            value = "foreign-dividends",
          )
        }
        "has a checkbox for foreign-interest" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 8,
            legend = AdditionalIncomeSourcesPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.foreignInterest,
            value = "foreign-interest",
          )
        }
        "has a checkbox for None" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 10,
            legend = AdditionalIncomeSourcesPageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.none,
            value = "none",
            isExclusive = true
          )
        }
        "has a continue button" in {
          form.selectNth(".govuk-button", 1).text() shouldBe AdditionalIncomeSourcesPageContent.continue
        }
      }
    }
  }
}

private object AdditionalIncomeSourcesPageContent {
  val title = s"Which other income do you have? - ${PageContentBase.title} - GOV.UK"
  val legend = "Which other income do you have?"
  val para = "This is additional income you’ll need to include in your tax return."
  val hint = "Select all that apply"
  val ukInterest = "UK Interest"
  val constructionIndustryScheme = "Construction Industry Scheme"
  val employment = "Employment (PAYE)"
  val ukDividends = "UK Dividends"
  val statePension = "State Pension income"
  val privatePension = "Private pension income"
  val foreignDividends = "Foreign dividends"
  val foreignInterest = "Foreign interest"
  val none = "None of these"
  val continue = "Continue"
}

