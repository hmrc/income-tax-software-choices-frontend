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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.CheckSoftwareForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.TestModels.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.helpers.SelectBuilder
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.CheckSoftwareView

class CheckSoftwareViewSpec extends ViewSpec with SelectBuilder {

  private val view = app.injector.instanceOf[CheckSoftwareView]

  private val formError: FormError = FormError("checkSoftware", "check-software.error.nonEmpty")

  def page(hasError: Boolean = false): HtmlFormat.Appendable = view(
    checkSoftwareForm = if (hasError) {
      CheckSoftwareForm.form.withError(formError)
    } else {
      CheckSoftwareForm.form
    },
    selectOptions = buildSelects(Seq(testVendorOne, testVendorTwo, testVendorThree, testVendorFour)),
    postAction = testCall,
    backLink = testBackUrl
  )

  def document(hasError: Boolean = false): Document = Jsoup.parse(page(hasError).body)

  "CheckSoftwareView" when {
    "there is an error" must {
      "have an error title" in {
        document(hasError = true).title() shouldBe s"Error: ${CheckSoftwareViewContent.title}"
      }
      "have an error summary" in {
        document(hasError = true).selectSeq(".govuk-error-summary").size shouldBe 1
        document(hasError = true).selectHead(".govuk-error-summary").text() should include("There is a problem")
        document(hasError = true).select(".govuk-error-summary__body > ul > li > a").attr("href") shouldBe "#checkSoftware"
      }
    }
    "there is no error" must {
      "have a title" in {
        document().title() shouldBe CheckSoftwareViewContent.title
      }
      "have a back link" in {
        document().selectHead(".govuk-back-link").attr("href") shouldBe testBackUrl
      }
      "have a form" which {
        def form: Element = document().mainContent.selectHead("form")

        "has the correct method and action" in {
          form.attr("method") shouldBe testCall.method
          form.attr("action") shouldBe testCall.url
        }
        "has a h1 which is the label" in {
          val label = document().mainContent.selectHead("h1 > .govuk-label")
          label.text() shouldBe CheckSoftwareViewContent.h1
          label.attr("for") shouldBe "check-software"
        }
        "has a hint" in {
          form.selectHead(".govuk-hint").text shouldBe CheckSoftwareViewContent.hint
        }
        "has a select with all items" in {
          val select = form.selectHead("select")
          select.attr("name") shouldBe "check-software"

          select.select("option").size shouldBe 7
          select.select("option").get(0).text shouldBe ""
          select.select("option").get(1).text shouldBe testVendorOne.name
          select.select("option").get(2).text shouldBe testVendorTwo.name
          select.select("option").get(3).text shouldBe testVendorThree.name
          select.select("option").get(4).text shouldBe testVendorFour.name
          select.select("option").get(5).text shouldBe "Google Sheets"
          select.select("option").get(6).text shouldBe "Microsoft Excel"
        }
        "has a continue button" in {
          form.selectNth(".govuk-button", 1).text() shouldBe CheckSoftwareViewContent.continue
        }
      }

    }
  }

}

private object CheckSoftwareViewContent {
  val title = s"What is the name of your software? - ${PageContentBase.title} - GOV.UK"
  val h1 = "What is the name of your software?"
  val hint = "Start typing and select from the list"
  val continue = "Continue"
}
