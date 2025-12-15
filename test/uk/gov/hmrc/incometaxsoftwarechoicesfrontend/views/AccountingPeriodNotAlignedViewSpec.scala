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
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.AccountingPeriodNotAlignedView

class AccountingPeriodNotAlignedViewSpec extends ViewSpec {

  object AccountingPeriodNotAlignedViewContent {
    val heading = "Your accounting period is not aligned"
    val title = s"$heading - ${PageContentBase.title} - GOV.UK"
    val paraOne = "You’ve selected an accounting period that does not end on the 5th April or 31st March."
    val paraTwo = "You’ll need to use compatible software to submit additional adjustments after the end of the tax year."
    val paraThree = "HMRC will publish guidance on how to do this in due course."
    val continue = "Continue"
  }

  private val view = app.injector.instanceOf[AccountingPeriodNotAlignedView]

  val page: HtmlFormat.Appendable = view(postAction = testCall, backLink = testBackUrl)
  val document: Document = Jsoup.parse(page.body)

  "AccountingPeriodNotAlignedView" must {
    "have a title" in {
      document.title() shouldBe AccountingPeriodNotAlignedViewContent.title
    }
    "have a heading" in {
      document.mainContent.selectHead("h1").text shouldBe AccountingPeriodNotAlignedViewContent.heading
    }
    "have a first paragraph" in {
      document.mainContent.selectNth("p", 1).text shouldBe AccountingPeriodNotAlignedViewContent.paraOne
    }
    "has a second paragraph" in {
      document.mainContent.selectNth("p", 2).text shouldBe AccountingPeriodNotAlignedViewContent.paraTwo
    }
    "has a third paragraph" in {
      document.mainContent.selectNth("p", 3).text shouldBe AccountingPeriodNotAlignedViewContent.paraThree
    }
    "have a form" which {
      val form: Element = document.selectHead("form")

      "has the correct method and action" in {
        form.attr("method") shouldBe testCall.method
        form.attr("action") shouldBe testCall.url
      }

      "has a continue button" in {
        form.selectHead(".govuk-button").text() shouldBe AccountingPeriodNotAlignedViewContent.continue
      }
    }
  }

}