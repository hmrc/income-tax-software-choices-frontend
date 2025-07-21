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
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.data.FormError
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{RadioItem, Text}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.UserTypeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.UserTypePage

class UserTypeViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[UserTypePage]
  private val formError = FormError(UserTypeForm.fieldName, "type-of-user.error")

  def page(hasError: Boolean = false): HtmlFormat.Appendable = {
    val form = if (hasError) {
      UserTypeForm.userTypeForm.withError(formError)
    } else {
      UserTypeForm.userTypeForm
    }
    view(userTypeForm = form, postAction = testCall, backUrl = testBackUrl)
  }

  def document(hasError: Boolean = false): Document = Jsoup.parse(page(hasError).body)

  "UserTypePage" when {
    "there is an error" should {
      "have an error title" in {
        document(hasError = true).title() shouldBe s"Error: ${UserTypeContent.title}"
      }
      "have an error summary" in {
        document(hasError = true).selectSeq(".govuk-error-summary").size shouldBe 1
        document(hasError = true).selectHead(".govuk-error-summary").text() should include("There is a problem")
        document(hasError = true).select(".govuk-error-summary__body > ul > li > a").attr("href") shouldBe "#type-of-user"
      }
    }
    "there is no error" should {
      "have the correct title" in {
        document().title() mustBe UserTypeContent.title
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
            name = UserTypeForm.fieldName,
            legend = UserTypeContent.legend,
            isHeading = false,
            isLegendHidden = true,
            hint = None,
            errorMessage = None,
            Seq(
              RadioItem(
                content = Text(UserTypeContent.soleTraderOrLandlord),
                value = Some(UserType.SoleTraderOrLandlord.key)
              ),
              RadioItem(
                content = Text(UserTypeContent.agent),
                value = Some(UserType.Agent.key)
              )
            )
          )
        }

        "have a continue button" in {
          form.selectNth(".govuk-button", 1).text() mustBe UserTypeContent.continue
        }
      }
    }
  }
}

private object UserTypeContent {
  val title = "How will you use this tool to choose software? - Find Software that works with Making Tax Digital for Income Tax - GOV.UK"
  val legend = "How will you use this tool to choose software?"
  val soleTraderOrLandlord = "As a sole trader or landlord doing my taxes"
  val agent = "As an agent working on behalf of a client"
  val continue = "Continue"
  val error = "Select if you’re a sole trader, landlord, or an agent"
}