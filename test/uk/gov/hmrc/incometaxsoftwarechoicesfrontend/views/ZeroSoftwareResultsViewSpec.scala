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
import org.scalatest.matchers.must.Matchers.*
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ZeroSoftwareResultsView

class ZeroSoftwareResultsViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[ZeroSoftwareResultsView]

  val finishActionUrl: Call = Call("POST", "/zero-results-finish-action-test-url")
  val testBackLink: Call = Call("GET", "/zero-results-back-link-test-url")

  def page(): HtmlFormat.Appendable = {
    view(finishAction = finishActionUrl, backLink = testBackLink)
  }

  def document(): Document = Jsoup.parse(page().body)

  "ZeroResultsView" when {

      "have the correct first heading" in {
        document().title() mustBe ZeroSoftwareResultsViewContent.heading1
      }

      "have the correct first paragraph text" in {
        document().mainContent.select("p").get(0).text mustBe ZeroSoftwareResultsViewContent.paragraph1
      }

      "have the correct second heading" in {
        document().selectHead("h2").text() mustBe ZeroSoftwareResultsViewContent.heading2
      }

      "have the correct second paragraph text" in {
        document().mainContent.select("p").get(1).text mustBe ZeroSoftwareResultsViewContent.paragraph2
      }

      "have the correct third paragraph text" in {
        document().mainContent.select("p").get(2).text mustBe ZeroSoftwareResultsViewContent.paragraph3
      }

      "have the correct forth paragraph text" in {
        document().mainContent.select("p").get(3).text mustBe ZeroSoftwareResultsViewContent.paragraph4
      }

      "have a bullet list" which {
        lazy val bulletList = document().mainContent.selectHead("ul.govuk-list.govuk-list--bullet")

        "has a first point" in {
          bulletList.selectNth("li", 1).text mustBe ZeroSoftwareResultsViewContent.bulletPoint1
        }
        "has a second point" in {
          bulletList.selectNth("li", 2).text mustBe ZeroSoftwareResultsViewContent.bulletPoint2
        }
      }
      "have a finish button" in {
        document().select("form").select(".govuk-button").text() shouldBe ZeroSoftwareResultsViewContent.finish
      }
  }
}

private object ZeroSoftwareResultsViewContent {
  val title = s"Your results - ${PageContentBase.title} - GOV.UK"
  val heading1 = s"Your results - ${PageContentBase.title} - GOV.UK"
  val paragraph1 = "Based on what you’ve told us, there is not currently an all-in-one software product that meets all of your needs."
  val heading2 = "More software will be available soon"
  val paragraph2 = "Several all-in-one products are currently being developed."
  val paragraph3 = "We update this tool regularly to show what’s available. Please check back later for updates, which will include new software and changes to the features of existing software."
  val paragraph4 = "You can also:"
  val bulletPoint1 = "visit software providers’ websites to find out more about what they’re developing"
  val bulletPoint2 = "ask your agent or accountant about software, if you have one"
  val heading3 = "Get software now"
  val paragraph5 = "If you’re familiar with Making Tax Digital for Income Tax and would like to use more than one software product at once, you can browse software that’s currently available."
  val linkText = "browse software that’s currently available"
  val finish = "Finish"
}
