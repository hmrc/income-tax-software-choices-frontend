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
  private val formError = FormError(AccountingPeriodForm.fieldName, "accounting-period.error")

  def page(hasError: Boolean = false): HtmlFormat.Appendable = {
    val form = if (hasError) {
      AccountingPeriodForm.accountingPeriodForm.withError(formError)
    } else {
      AccountingPeriodForm.accountingPeriodForm
    }
    view(accountingPeriodForm = form, postAction = testCall, backUrl = testBackUrl)
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

      "have a form" which {
        def form: Element = document().mainContent.selectHead("form")

        "has the correct method and action" in {
          form.attr("method") shouldBe testCall.method
          form.attr("action") shouldBe testCall.url
        }

        "render the radio inputs" in {
          document().mainContent.mustHaveRadioInput(
            selector = "fieldset"
          )(
            name = AccountingPeriodForm.fieldName,
            legend = AccountingPeriodContent.legend,
            isHeading = false,
            isLegendHidden = true,
            hint = None,
            errorMessage = None,
            Seq(
              RadioItem(
                content = Text(AccountingPeriodContent.sixthToFifth),
                value = Some(AccountingPeriod.SixthAprilToFifthApril.key)
              ),
              RadioItem(
                content = Text(AccountingPeriodContent.firstToThirtyFirst),
                value = Some(AccountingPeriod.FirstAprilToThirtyFirstMarch.key),
              ),
              RadioItem(
                divider = Some(AccountingPeriodContent.or)
              ),
              RadioItem(
                content = Text(AccountingPeriodContent.neither),
                value = Some(AccountingPeriod.OtherAccountingPeriod.key),
              )
            )
          )
        }

        "have a continue button" in {
          form.selectNth(".govuk-button", 1).text() mustBe AccountingPeriodContent.continue
        }
      }
    }
  }
}

private object AccountingPeriodContent {
  val title = s"What accounting period do you use? - ${PageContentBase.title} - GOV.UK"
  val legend = "What accounting period do you use?"
  val paraOne = "If your accounting period ends on 31 March, you’ll need software that supports calendar update periods (opens in new tab). This will make keeping records simpler."
  val linkText = "calendar update periods (opens in new tab)"
  val linkHref = "https://www.gov.uk/guidance/use-making-tax-digital-for-income-tax/send-quarterly-updates#using-calendar-update-periods"
  val sixthToFifth = "6 April to 5 April"
  val firstToThirtyFirst = "1 April to 31 March"
  val or = "or"
  val neither = "Neither of these"
  val continue = "Continue"
  val error = "Select either 6 April to 5 April, 1 April to 31 March, or neither of these"
}
