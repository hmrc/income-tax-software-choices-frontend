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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.EnterSoftwareNameForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.TestModels.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareProduct
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.{FutureVendor, Recognised, Spreadsheet}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.helpers.SelectBuilder
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.EnterSoftwareNameView

class EnterSoftwareNameViewSpec extends ViewSpec with SelectBuilder {

  private val view = app.injector.instanceOf[EnterSoftwareNameView]

  private val formError: FormError = FormError("enterSoftwareName", "enterSoftwareName.error.nonEmpty")

  private val testProducts: Seq[SoftwareProduct] = Seq(
    SoftwareProduct(1, "Vendor One", Recognised),
    SoftwareProduct(2, "Vendor Two", Spreadsheet),
    SoftwareProduct(3, "Vendor Three", FutureVendor)
  )

  private val notListedLink = "testNotListedLink"

  def page(hasError: Boolean = false): HtmlFormat.Appendable = view(
    enterSoftwareNameForm = if (hasError) {
      EnterSoftwareNameForm.form.withError(formError)
    } else {
      EnterSoftwareNameForm.form
    },
    selectOptions = buildSelects(testProducts),
    postAction = testCall,
    notListedLink = notListedLink,
    backLink = testBackUrl
  )

  def document(hasError: Boolean = false): Document = Jsoup.parse(page(hasError).body)

  "EnterSoftwareNameView" when {
    "there is an error" must {
      "have an error title" in {
        document(hasError = true).title() shouldBe s"Error: ${EnterSoftwareNameViewContent.title}"
      }
      "have an error summary" in {
        document(hasError = true).selectSeq(".govuk-error-summary").size shouldBe 1
        document(hasError = true).selectHead(".govuk-error-summary").text() should include("There is a problem")
        document(hasError = true).select(".govuk-error-summary__body > ul > li > a").attr("href") shouldBe "#enterSoftwareName"
      }
    }
    "there is no error" must {
      "have a title" in {
        document().title() shouldBe EnterSoftwareNameViewContent.title
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
          label.text() shouldBe EnterSoftwareNameViewContent.h1
          label.attr("for") shouldBe "enter-software-name"
        }
        "has a hint" in {
          form.selectHead(".govuk-hint").text shouldBe EnterSoftwareNameViewContent.hint
        }
        "has a select with all items" in {
          val select = form.selectHead("select")
          select.attr("name") shouldBe "enter-software-name"

          select.select("option").size shouldBe 4
          select.select("option").get(0).text shouldBe ""
          select.select("option").get(1).text shouldBe "Vendor One"
          select.select("option").get(2).text shouldBe "Vendor Three"
          select.select("option").get(3).text shouldBe "Vendor Two" // Sorted alphabetically
        }
        "has a continue button" in {
          form.selectNth(".govuk-button", 1).text() shouldBe EnterSoftwareNameViewContent.continue
        }
        "has a 'My software not listed' secondary button" in {
          form.selectNth(".govuk-button--secondary", 1).text() shouldBe EnterSoftwareNameViewContent.softwareNotListed
          form.selectNth(".govuk-button--secondary", 1).attr("href") shouldBe notListedLink
        }
      }

    }
  }

}

private object EnterSoftwareNameViewContent {
  val title = s"What is the name of your software? - ${PageContentBase.title} - GOV.UK"
  val h1 = "What is the name of your software?"
  val hint = "Start typing and select from the list"
  val continue = "Continue"
  val softwareNotListed = "My software is not listed"
  val emptyError = "The software you entered is not on the list. Select software from the list or click 'My software is not listed'"
}
