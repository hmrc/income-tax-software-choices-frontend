/*
 * Copyright 2026 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.QuarterlyOnlyView

class QuarterlyOnlyViewSpec extends ViewSpec {

  private val softwareName = "A1 Tax Stuff"
  private val view = app.injector.instanceOf[QuarterlyOnlyView]
  val resultsUrl = "/test-software-results-url"
  val page: HtmlFormat.Appendable = view(
    productDetailsUrl = testCall.url,
    backLink = testBackUrl,
    chosenSoftware = softwareName,
    softwareResultsUrl = resultsUrl
  )
  val document: Document = Jsoup.parse(page.body)
  "QuarterlyOnly view" must {

    "have a title" in {
      document.title() shouldBe QuarterlyOnlyContent.title
    }

    "have a back link" in {
      document.selectHead(".govuk-back-link").attr("href") shouldBe testBackUrl
    }

    "have a main heading" in {
      document.selectHead("h1").text() shouldBe QuarterlyOnlyContent.heading1
    }

    "have an h2" in {
      document.selectHead("h2").text() shouldBe QuarterlyOnlyContent.heading2
    }

    "have the correct paragraphs" in {
      document.mainContent.selectNth("p", 1).text() shouldBe QuarterlyOnlyContent.para1
      document.mainContent.selectNth("p", 2).text() shouldBe QuarterlyOnlyContent.para2
      document.mainContent.selectNth("p", 2).selectHead("a").attribute("href").getValue shouldBe testCall.url
      document.mainContent.selectNth("p", 3).text() shouldBe QuarterlyOnlyContent.para3
      document.mainContent.selectNth("p", 3).selectHead("a").attribute("href").getValue shouldBe resultsUrl
      document.mainContent.selectNth("p", 4).text() shouldBe QuarterlyOnlyContent.para4

    }

    "have the correct bullet points" in {
      document.selectNth("ul.govuk-list--bullet > li", 1).text() shouldBe QuarterlyOnlyContent.bullet1Text
      document.selectNth("ul.govuk-list--bullet > li", 1).selectHead("a").attribute("href").getValue shouldBe QuarterlyOnlyContent.bullet1Link
      document.selectNth("ul.govuk-list--bullet > li", 2).text() shouldBe QuarterlyOnlyContent.bullet2Text
      document.selectNth("ul.govuk-list--bullet > li", 2).selectHead("a").attribute("href").getValue shouldBe QuarterlyOnlyContent.bullet2Link
    }

    "display the exit survey link" in {
      val link = document.mainContent.select(".govuk-link").get(4)
      link.text shouldBe QuarterlyOnlyContent.exitSurveyLinkTitle
      link.attr("href") shouldBe QuarterlyOnlyContent.exitSurveyLink
    }

  }

}


private object QuarterlyOnlyContent {
  val heading1 = "A1 Tax Stuff can only send quarterly updates"
  val heading2 = "What you should do next"
  val title = s"$heading1 - ${PageContentBase.title} - GOV.UK"
  val para1 = "This software does not have the ability to submit a tax return."
  val para2 = "Learn more about your software and its upcoming features."
  val para3 = "If you intend to do your tax returns via software, you will need additional software to do this."
  val para4 = "If you want to use this software, you need to:"
  val bullet1Text = "sign up for Making Tax Digital for Income Tax (opens in new tab)"
  val bullet1Link = "https://www.gov.uk/guidance/sign-up-for-making-tax-digital-for-income-tax"
  val bullet2Text = "authorise your software for HMRC (opens in new tab)"
  val bullet2Link = "https://www.gov.uk/guidance/use-making-tax-digital-for-income-tax/get-your-software-ready"
  val exitSurveyLinkTitle = "Give feedback on this service (opens in new tab)"
  val exitSurveyLink = "http://localhost:9514/feedback/SOFTWAREMTDIT?useServiceNavigation"
}