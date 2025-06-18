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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.BusinessIncomeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.BusinessIncomePage

class BusinessIncomeSourcesViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[BusinessIncomePage]

  private val formError: FormError = FormError("businessIncome", "business-income.error.nonEmpty")

  def page(hasError: Boolean = false): HtmlFormat.Appendable = view(
    businessIncomeForm = if (hasError) {
      BusinessIncomeForm.form.withError(formError)
    } else {
      BusinessIncomeForm.form
    },
    postAction = testCall
  )

  def document(hasError: Boolean = false): Document = Jsoup.parse(page(hasError).body)


  "BusinessIncomePage" when {
    "there is an error" must {
      "have an error title" in {
        document(hasError = true).title() shouldBe s"Error: ${BusinessIncomePageContent.title}"
      }
      "have an error summary" in {
        document(hasError = true).selectSeq(".govuk-error-summary").size shouldBe 1
        document(hasError = true).selectHead(".govuk-error-summary").text() should include("There is a problem")
        document(hasError = true).select(".govuk-error-summary__body > ul > li > a").attr("href") shouldBe "#businessIncome"
      }
    }

    "there is no error" must {
      "have a title" in {
        document().title() shouldBe BusinessIncomePageContent.title
      }
      "have a back link" in {
        document().selectHead(".govuk-back-link").attr("href") shouldBe appConfig.guidance
      }
      "have a paragraph" in {
        document().mainContent.selectNth("p", 1).text shouldBe BusinessIncomePageContent.para
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

          hint.text shouldBe BusinessIncomePageContent.hint
          fieldSet.attr("aria-describedby") should include(hint.attr("id"))
        }
        "has a checkbox for self-employment" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 1,
            legend = BusinessIncomePageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "businessIncome[]",
            label = BusinessIncomePageContent.selfEmployment,
            value = "sole-trader",
          )
        }
        "has a checkbox for UK property" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 2,
            legend = BusinessIncomePageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "businessIncome[]",
            label = BusinessIncomePageContent.ukProperty,
            value = "uk-property",
          )
        }
        "has a checkbox for foreign property" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 3,
            legend = BusinessIncomePageContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = "businessIncome[]",
            label = BusinessIncomePageContent.foreignProperty,
            value = "overseas-property",
          )
        }
        "has a continue button" in {
          form.selectNth(".govuk-button", 1).text() shouldBe BusinessIncomePageContent.continue
        }
      }
    }
  }
}

private object BusinessIncomePageContent {
  val title = "Which business income do you have? - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"
  val legend = "Which business income do you have?"
  val para = "You’ll need to include this in your quarterly updates and tax return."
  val hint = "Select all that apply."
  val selfEmployment = "Sole trader"
  val ukProperty = "UK property"
  val foreignProperty = "Foreign property"
  val continue = "Continue"
}
