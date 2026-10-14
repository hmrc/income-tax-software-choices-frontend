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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.BusinessIncomeView

class BusinessIncomeSourcesViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[BusinessIncomeView]
  private val SoftwareName = "Bright"
  private val formErrorIndividual: FormError = FormError("businessIncome", "business-income.error.non-empty.individual")
  private val formErrorAgent: FormError = FormError("businessIncome", "business-income.error.non-empty.agent")

  def page(hasError: Boolean = false, isAgent: Boolean = false): HtmlFormat.Appendable = {
    val form = (hasError, isAgent) match {
      case (true, true) => BusinessIncomeForm.form("agent").withError(formErrorAgent)
      case (true, false) => BusinessIncomeForm.form("individual").withError(formErrorIndividual)
      case (false, _) => BusinessIncomeForm.form("individual")
    }
    view(
      businessIncomeForm = form,
      postAction = testCall,
      backUrl = testBackUrl,
      softwareName = Some(SoftwareName),
      userTypeString = if (isAgent) "agent" else "individual"
    )
  }

  def document(hasError: Boolean = false, isAgent: Boolean = false): Document = Jsoup.parse(page(hasError, isAgent).body)


  "BusinessIncomePage" when {
    "there is an error with an individual" must {
      "have an individual-specific error title" in {
        document(hasError = true).title() shouldBe s"Error: ${BusinessIncomePageContent.headingIndividual} ${BusinessIncomePageContent.titleSuffix}"
      }
      "have an a valid error summary including the individual-specific error message" in {
        document(hasError = true).selectSeq(".govuk-error-summary").size shouldBe 1
        val errorSummary = document(hasError = true).selectHead(".govuk-error-summary")
        errorSummary.select(".govuk-error-summary__title").text() shouldBe "There is a problem"
        errorSummary.select(".govuk-error-summary__body > ul > li > a").text() shouldBe BusinessIncomePageContent.errorIndividual
        document(hasError = true).select(".govuk-error-summary__body > ul > li > a").attr("href") shouldBe "#businessIncome"
      }
    }

    "there is an error with an agent" must {
      "have an agent-specific error title" in {
        document(hasError = true, isAgent = true).title() shouldBe s"Error: ${BusinessIncomePageContent.headingAgent} ${BusinessIncomePageContent.titleSuffix}"
      }
      "have an agent-specific error message" in {
        document(hasError = true, isAgent = true).select(".govuk-error-summary__body > ul > li > a").text() shouldBe BusinessIncomePageContent.errorAgent
      }
    }

    "there is no error" must {
      "have an individual-specific title" in {
        document().title() shouldBe s"${BusinessIncomePageContent.headingIndividual} ${BusinessIncomePageContent.titleSuffix}"
      }
      "have an agent-specific title" in {
        document(isAgent = true).title() shouldBe s"${BusinessIncomePageContent.headingAgent} ${BusinessIncomePageContent.titleSuffix}"
      }
      "have a back link" in {
        document().selectHead(".govuk-back-link").attr("href") shouldBe testBackUrl
      }
      "have individual-specific paragraphs" in {
        document().mainContent.selectNth("p", 1).text shouldBe BusinessIncomePageContent.para1Individual
        document().mainContent.selectNth("p", 2).text shouldBe BusinessIncomePageContent.para2Individual
      }
      "have agent-specific paragraphs" in {
        document(isAgent = true).mainContent.selectNth("p", 1).text shouldBe BusinessIncomePageContent.para1Agent
        document(isAgent = true).mainContent.selectNth("p", 2).text shouldBe BusinessIncomePageContent.para2Agent
      }
      "have a software name caption" in {
        document().mainContent.selectHead("span.govuk-caption-l").text() shouldBe SoftwareName
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
            legend = BusinessIncomePageContent.headingIndividual,
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
            legend = BusinessIncomePageContent.headingIndividual,
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
            legend = BusinessIncomePageContent.headingIndividual,
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
  val titleSuffix = s"- ${PageContentBase.title} - GOV.UK"
  val headingIndividual = "Which of these income sources do you need to include in your quarterly updates?"
  val headingAgent = "Which of these income sources need to be included in your client’s quarterly updates?"
  val para1Individual = "You’ll also need to send these in your tax return."
  val para1Agent = "These income sources must also be included in their tax return."
  val para2Individual = "If you expect your income sources to change, include your current and future income sources."
  val para2Agent = "If your client’s income sources are expected to change, include their current and future income sources."
  val hint = "Select all that apply"
  val selfEmployment = "Being self-employed as a sole trader"
  val ukProperty = "Renting out a UK property"
  val foreignProperty = "Renting out a foreign property"
  val continue = "Continue"
  val errorIndividual = "Select if you get income from being self-employed as a sole trader, or renting out a UK or foreign property"
  val errorAgent = "Select if your client gets income from being self-employed as a sole trader, or renting out a UK or foreign property"
}
