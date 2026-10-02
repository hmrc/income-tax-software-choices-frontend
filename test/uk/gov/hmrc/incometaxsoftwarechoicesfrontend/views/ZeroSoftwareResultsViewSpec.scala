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

      "have the correct title" in {
        document().title() mustBe ZeroSoftwareResultsViewContent.title
      }

      "have the correct first heading" in {
        document().selectHead("h1").text() mustBe ZeroSoftwareResultsViewContent.heading1
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

       "display the exit survey link" in {
          val link = document().mainContent.select(".govuk-link").get(0)
          link.text shouldBe ZeroSoftwareResultsViewContent.exitSurveyLinkTitle
          link.attr("href") shouldBe ZeroSoftwareResultsViewContent.exitSurveyLink
       }

      "have a finish button" in {
        document().select("form").select(".govuk-button").text() shouldBe ZeroSoftwareResultsViewContent.finish
      }
  }
}

private object ZeroSoftwareResultsViewContent {
  val title = s"There is currently no compatible software that meets all the requirements - ${PageContentBase.title} - GOV.UK"
  val heading1 = s"There is currently no compatible software that meets all the requirements"
  val paragraph1 = "Based on the information provided, no all-in-one software product currently meets all of these requirements."
  val heading2 = "More software will be available soon"
  val paragraph2 = "Several all-in-one products are currently being developed."
  val paragraph3 = "We update this tool regularly to show what’s available. Please check back later for updates, which will include new software and changes to the features of existing software."
  val finish = "Finish"
  val exitSurveyLinkTitle = "Give feedback on this service (opens in new tab)"
  val exitSurveyLink = "http://localhost:9514/feedback/SOFTWAREMTDIT?useServiceNavigation"
}
