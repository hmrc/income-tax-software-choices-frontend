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
import org.scalatest.matchers.must.Matchers._
import play.api.data.FormError
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{RadioItem, Text}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.AccountingPeriodForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.AccountingPeriodView

class AccountingPeriodViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[AccountingPeriodView]
  private val formError = FormError(AccountingPeriodForm.formKey, "accounting-period.error")
  private val SoftwareName = "Bright"

  def page(hasError: Boolean = false): HtmlFormat.Appendable = {
    val form = if (hasError) {
      AccountingPeriodForm.accountingPeriodForm.withError(formError)
    } else {
      AccountingPeriodForm.accountingPeriodForm
    }
    view(accountingPeriodForm = form, postAction = testCall, backUrl = testBackUrl, softwareName = Some(SoftwareName))
  }

  def document(hasError: Boolean = false): Document = Jsoup.parse(page(hasError).body)

  "AccountingPeriodPage" when {
    "there is an error" should {
      "have an error title" in {
        document(hasError = true).title() shouldBe s"Error: ${AccountingPeriodContent.title}"
      }
      "have an error summary" in {
        document(hasError = true).selectSeq(".govuk-error-summary").size shouldBe 1
        document(hasError = true).selectHead(".govuk-error-summary").text() should include("There is a problem")
        document(hasError = true).select(".govuk-error-summary__body > ul > li > a").attr("href") shouldBe "#accounting-period"
      }
    }
    "there is no error" should {
      "have the correct title" in {
        document().title() mustBe AccountingPeriodContent.title
      }

      "have a paragraph with a link" in {
        document().mainContent.select("p").get(0).text mustBe AccountingPeriodContent.paraOne
        val link = document().mainContent.select(".govuk-link").first()
        link.text mustBe AccountingPeriodContent.linkText
        link.attr("href") mustBe AccountingPeriodContent.linkHref
      }

      "have a software name caption" in {
        document().mainContent.selectHead("span.govuk-caption-l").text() shouldBe SoftwareName
      }

      "have a heading" in {
        document().mainContent.selectHead("h1").text() shouldBe AccountingPeriodContent.heading
      }

      "have a secondary heading" in {
        document().mainContent.selectNth("h1", 2).text() shouldBe AccountingPeriodContent.legend
      }

      "have paragraph two" in {
        document().mainContent.select("p").get(1).text mustBe AccountingPeriodContent.paraTwo
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
          hint.text shouldBe AccountingPeriodContent.hint
          fieldSet.attr("aria-describedby") should include(hint.attr("id"))
        }

        "has a checkbox for sixth-april-to-fifth-april" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 1,
            legend = AccountingPeriodContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = s"${AccountingPeriodForm.formKey}[]",
            label = AccountingPeriodContent.sixthToFifth,
            value = "sixth-april-to-fifth-april"
          )
        }

        "has a checkbox for first-april-to-thirty-first-march" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 2,
            legend = AccountingPeriodContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = s"${AccountingPeriodForm.formKey}[]",
            label = AccountingPeriodContent.firstToThirtyFirst,
            value = "first-april-to-thirty-first-march"
          )
        }

        "has a checkbox for other" in {
          form.mustHaveCheckbox("fieldSet")(
            checkbox = 3,
            legend = AccountingPeriodContent.legend,
            isHeading = false,
            isLegendHidden = true,
            name = s"${AccountingPeriodForm.formKey}[]",
            label = AccountingPeriodContent.other,
            value = "other",
            isExclusive = true
          )
        }

        "has a continue button" in {
          form.selectNth(".govuk-button", 1).text() shouldBe AccountingPeriodContent.continue
        }
      }
    }
  }
}


private object AccountingPeriodContent {
  val title = s"Accounting period - ${PageContentBase.title} - GOV.UK"
  val heading = "Accounting period"
  val legend = "What accounting period do you use?"
  val paraOne = "If your accounting period is 1 April to 31 March, you’ll need software that supports calendar update periods (opens in new tab). This will make keeping records simpler."
  val paraTwo = "You can also select the accounting period you expect to use in the future, so we can recommend software that meets your needs."
  val linkText = "calendar update periods (opens in new tab)"
  val linkHref = "https://www.gov.uk/guidance/use-making-tax-digital-for-income-tax/send-quarterly-updates#using-calendar-update-periods"
  val hint = "Select all that apply"
  val sixthToFifth = "6 April to 5 April"
  val firstToThirtyFirst = "1 April to 31 March"
  val other = "A different accounting period"
  val continue = "Continue"
  val error = "Select an accounting period"
}
