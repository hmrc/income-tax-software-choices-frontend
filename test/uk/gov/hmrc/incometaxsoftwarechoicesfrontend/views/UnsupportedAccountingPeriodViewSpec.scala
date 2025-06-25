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
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.UnsupportedAccountingPeriod

class UnsupportedAccountingPeriodViewSpec extends ViewSpec {

  object UnsupportedAccountingPeriodContent {
    val heading = "There is no software product or products that support your accounting period"
    val title = s"$heading - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"
    val paraOne = "Making Tax Digital is only available to people who use the business accounting periods:"
    val bulletOne = "6 April to 5 April"
    val bulletTwo = "1 April to 31 March"
    val linkText = "Self Assessment tax return"
    val linkHref = "https://www.gov.uk/self-assessment-tax-returns/sending-return"
    val paraTwo = s"We may extend this service to people with other accounting periods in the future. You’ll need to continue to submit your $linkText as normal."
    val continue = "Continue"
  }

  private val view = app.injector.instanceOf[UnsupportedAccountingPeriod]

  val page: HtmlFormat.Appendable = view(backLink = testBackUrl)
  val document: Document = Jsoup.parse(page.body)

  "UnsupportedAccountingPeriod" must {
    "have a title" in {
      document.title() shouldBe UnsupportedAccountingPeriodContent.title
    }
    "have a heading" in {
      document.mainContent.selectHead("h1").text shouldBe UnsupportedAccountingPeriodContent.heading
    }
    "have a first paragraph" in {
      document.mainContent.selectNth("p", 1).text shouldBe UnsupportedAccountingPeriodContent.paraOne
    }
    "have a bullet list" which {
      lazy val bulletList = document.mainContent.selectHead("ul.govuk-list.govuk-list--bullet")

      "has a first point" in {
        bulletList.selectNth("li", 1).text shouldBe UnsupportedAccountingPeriodContent.bulletOne
      }
      "has a second point" in {
        bulletList.selectNth("li", 2).text shouldBe UnsupportedAccountingPeriodContent.bulletTwo
      }
    }
    "has a second paragraph" which {
      lazy val paraTwo = document.mainContent.selectNth("p", 2)

      "has the correct text" in {
        paraTwo.text shouldBe UnsupportedAccountingPeriodContent.paraTwo
      }
      "has a link contained within the text" which {
        lazy val link = paraTwo.selectHead("a.govuk-link")

        "has the correct link text" in {
          link.text shouldBe UnsupportedAccountingPeriodContent.linkText
        }
        "has the correct link href" in {
          link.attr("href") shouldBe UnsupportedAccountingPeriodContent.linkHref
        }
      }
    }
  }

}