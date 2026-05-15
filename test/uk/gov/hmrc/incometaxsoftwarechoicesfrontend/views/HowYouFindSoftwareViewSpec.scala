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
import org.scalatest.matchers.must.Matchers.*
import play.api.data.FormError
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{Hint, RadioItem, Text}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.HowYouFindSoftwareForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.HowYouFindSoftwareView

class HowYouFindSoftwareViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[HowYouFindSoftwareView]
  private val formError = FormError(HowYouFindSoftwareForm.fieldName, "how-you-find-software.error")

  def page(hasError: Boolean = false): HtmlFormat.Appendable = {
    val form = if (hasError) {
      HowYouFindSoftwareForm.form.withError(formError)
    } else {
      HowYouFindSoftwareForm.form
    }
    view(howYouFindSoftwareForm = form, postAction = testCall, backUrl = HowYouFindSoftwareContent.guidanceLink)
  }

  def document(hasError: Boolean = false): Document = Jsoup.parse(page(hasError).body)

  "HowYouFindSoftwareView" when {
    "there is an error" should {
      "have an error title" in {
        document(hasError = true).title() shouldBe s"Error: ${HowYouFindSoftwareContent.title}"
      }
      "have an error summary" in {
        document(hasError = true).selectSeq(".govuk-error-summary").size shouldBe 1
        document(hasError = true).selectHead(".govuk-error-summary").text() should include("There is a problem")
        document(hasError = true).select(".govuk-error-summary__body > ul > li > a").attr("href") shouldBe "#how-you-find-software"
      }
    }
    "there is no error" should {
      "have the correct title" in {
        document().title() mustBe HowYouFindSoftwareContent.title
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
            name = HowYouFindSoftwareForm.fieldName,
            legend = HowYouFindSoftwareContent.legend,
            isHeading = true,
            isLegendHidden = false,
            hint = None,
            errorMessage = None,
            Seq(
              RadioItem(
                content = Text(HowYouFindSoftwareContent.find),
                value = Some(Find.key),
                hint = Some(Hint(content = Text(HowYouFindSoftwareContent.findHint)))
              ),
              RadioItem(
                content = Text(HowYouFindSoftwareContent.viewAll),
                value = Some(ViewAll.key),
                hint = None
              ),
              RadioItem(
                content = Text(HowYouFindSoftwareContent.check),
                value = Some(Check.key),
                hint = Some(Hint(content = Text(HowYouFindSoftwareContent.checkHint)))
              )
            )
          )
        }

        "have a continue button" in {
          form.selectNth(".govuk-button", 1).text() mustBe PageContentBase.continue
        }
      }
    }
  }
}

private object HowYouFindSoftwareContent {
  val title = s"How would you like to find software? - ${PageContentBase.title} - GOV.UK"
  val legend = "How would you like to find software?"
  val find = "Find software based on my answers to questions"
  val findHint = "We will ask you questions about your income sources to show you the best software for your needs"
  val viewAll = "Show a list of all available software and let me select filters for my needs"
  val check = "Check if my current software is compatible"
  val checkHint = "This includes using spreadsheets"
  val guidanceLink = "https://www.gov.uk/guidance/find-software-thats-compatible-with-making-tax-digital-for-income-tax"
  val error = "Select an option"
}