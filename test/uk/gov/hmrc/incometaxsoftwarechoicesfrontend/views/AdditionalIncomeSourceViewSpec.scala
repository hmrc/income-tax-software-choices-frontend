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
import org.scalatest.BeforeAndAfterEach
import play.api.data.FormError
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.AdditionalIncomeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.AdditionalIncomeSourceView

class AdditionalIncomeSourceViewSpec extends ViewSpec  with BeforeAndAfterEach {
  private val view = app.injector.instanceOf[AdditionalIncomeSourceView]

  private val formEmptyIndividual: FormError = FormError("additionalIncome", "additional-income-source.error-non-empty.individual")
  private val formEmptyAgent: FormError = FormError("additionalIncome", "additional-income-source.error-non-empty.agent")
  private val formNoneOnly: FormError = FormError("additionalIncome", "additional-income-source.error-none-only")
  private val SoftwareName = "Bright"

  def page(hasError: Boolean = false, isAgent: Boolean = false): HtmlFormat.Appendable = {
    val form = (hasError, isAgent) match {
      case (true, true) =>
        AdditionalIncomeForm.form("agent")
          .withError(formEmptyAgent)
          .withError(formNoneOnly)
      case (true, false) =>
        AdditionalIncomeForm.form("individual")
          .withError(formEmptyIndividual)
          .withError(formNoneOnly)
      case (false, _) =>
        AdditionalIncomeForm.form("individual")
    }
    view(
      additionalIncomeForm = form,
      postAction = testCall,
      backUrl = testBackUrl,
      softwareName = Some(SoftwareName),
      userTypeString = if (isAgent) "agent" else "individual"
    )
  }

  def document(hasError: Boolean = false, isAgent: Boolean = false): Document = Jsoup.parse(page(hasError, isAgent).body)

  "AdditionalIncomePage" when {
    "there is an error with an individual's form" must {
      val individualErrorForm = document(hasError = true)
      "have an error title" in {
        individualErrorForm.title() shouldBe s"Error: ${AdditionalIncomeSourcesPageContent.headingIndividual} ${AdditionalIncomeSourcesPageContent.titleSuffix}"
      }
      "have an error summary" in {
        individualErrorForm.selectSeq(".govuk-error-summary").size shouldBe 1
        val errorText = individualErrorForm.selectHead(".govuk-error-summary").text()
        errorText should include("There is a problem")
        errorText should include("Select the income you need to submit in your tax return or select ‘none of these’")
        errorText should include("Select income or select ‘none of these’")
        individualErrorForm.select(".govuk-error-summary__body > ul > li > a").attr("href") shouldBe "#additionalIncome"
      }
    }

    "there is an error with an agent's form" must {
      val agentErrorForm = document(hasError = true, isAgent = true)
      "have an error title" in {
        agentErrorForm.title() shouldBe s"Error: ${AdditionalIncomeSourcesPageContent.headingAgent} ${AdditionalIncomeSourcesPageContent.titleSuffix}"
      }
      "have an error summary including the agent-specific error" in {
        agentErrorForm.selectHead(".govuk-error-summary").text() should include("Select the income your client needs to submit in their tax return or select ‘none of these’")
      }
    }

    "there is no error" must {

      "have an individual-specific title for an individual" in {
        document().title() shouldBe s"${AdditionalIncomeSourcesPageContent.headingIndividual} ${AdditionalIncomeSourcesPageContent.titleSuffix}"
      }

      "have an agent-specific title for an agent" in {
        document(isAgent = true).title() shouldBe s"${AdditionalIncomeSourcesPageContent.headingAgent} ${AdditionalIncomeSourcesPageContent.titleSuffix}"
      }

      "have a software name caption" in {
        document().mainContent.selectHead("span.govuk-caption-l").text() shouldBe SoftwareName
      }

      "have an individual-specific paragraph for an individual" in {
        document().mainContent.select("p").get(0).text shouldBe AdditionalIncomeSourcesPageContent.paraIndividual
      }

      "have an agent-specific paragraph for an agent" in {
        document(isAgent = true).mainContent.select("p").get(0).text shouldBe AdditionalIncomeSourcesPageContent.paraAgent
      }

      "have a correctly formatted form (for an individual)" which {
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
            legend = AdditionalIncomeSourcesPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.ukInterest,
            value = "uk-interest",
          )
        }
        "has a checkbox for employment" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 2,
            legend = AdditionalIncomeSourcesPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.employment,
            value = "employment",
          )
        }
        "has a checkbox for uk-dividends" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 3,
            legend = AdditionalIncomeSourcesPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.ukDividends,
            value = "uk-dividends",
          )
        }
        "has a checkbox for state-pension-income" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 4,
            legend = AdditionalIncomeSourcesPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.statePension,
            value = "state-pension-income",
          )
        }
        "has a checkbox for private-pension-income" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 5,
            legend = AdditionalIncomeSourcesPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.privatePension,
            value = "private-pension-income",
          )
        }
        "has a checkbox for partner-income" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 6,
            legend = AdditionalIncomeSourcesPageContent.headingIndividual,
            isHeading = false,
            isLegendHidden = true,
            name = "additionalIncome[]",
            label = AdditionalIncomeSourcesPageContent.partnerIncomeFromPartnership,
            value = "partner-income",
          )
        }

        "has a checkbox for foreign-dividends" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 7,
            legend = AdditionalIncomeSourcesPageContent.headingIndividual,
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
            legend = AdditionalIncomeSourcesPageContent.headingIndividual,
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
            legend = AdditionalIncomeSourcesPageContent.headingIndividual,
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

      "have the correct legend for an agent form" in {
        val form = document(isAgent = true).mainContent.selectHead("form")
        val fieldSet = form.selectHead("fieldset")
        val legend = fieldSet.selectHead("legend")

        legend.text shouldBe AdditionalIncomeSourcesPageContent.headingAgent
        legend.hasClass("govuk-visually-hidden") shouldBe true
      }
    }
  }
}

private object AdditionalIncomeSourcesPageContent {
  val titleSuffix = s"- ${PageContentBase.title} - GOV.UK"
  val headingIndividual = "Which of the following income do you need to submit in your tax return?"
  val headingAgent = "Which of the following income needs to be submitted in your client’s tax return?"
  val paraIndividual = "If you expect your income to change, include your current and future income."
  val paraAgent = "If your client’s income is expected to change, include their current and future income."
  val hint = "Select all that apply"
  val ukInterest = "UK interest"
  val employment = "Employment (PAYE)"
  val ukDividends = "UK dividends"
  val statePension = "State Pension income"
  val privatePension = "Private pension incomes"
  val partnerIncomeFromPartnership = "Partner income from a partnership"
  val foreignDividends = "Foreign dividends"
  val foreignInterest = "Foreign interest"
  val none = "None of these"
  val continue = "Continue"
}

