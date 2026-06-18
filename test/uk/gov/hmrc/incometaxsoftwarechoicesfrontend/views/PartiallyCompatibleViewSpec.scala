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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.PartiallyCompatibleView

class PartiallyCompatibleViewSpec extends ViewSpec {

  private val softwareName = "A1 Tax Stuff"
  private val view = app.injector.instanceOf[PartiallyCompatibleView]
  val page: HtmlFormat.Appendable = view(productDetailsUrl = testCall.url, backLink = testBackUrl, chosenSoftware = softwareName)
  val document: Document = Jsoup.parse(page.body)
  "PartiallyCompatible view" must {

    "have a title" in {
      document.title() shouldBe PartiallyCompatibleContent.title
    }

    "have a back link" in {
      document.selectHead(".govuk-back-link").attr("href") shouldBe testBackUrl
    }

    "have a main heading" in {
      document.selectHead("h1").text() shouldBe PartiallyCompatibleContent.heading1
    }

    "have an h2" in {
      document.selectHead("h2").text() shouldBe PartiallyCompatibleContent.heading2
    }

    "have the correct paragraphs" in {
      document.mainContent.selectNth("p", 1).text() shouldBe PartiallyCompatibleContent.para1
      document.mainContent.selectNth("p", 2).text() shouldBe PartiallyCompatibleContent.para2
      document.mainContent.selectNth("p", 2).selectHead("a").attribute("href").getValue shouldBe testCall.url
      document.mainContent.selectNth("p", 3).text() shouldBe PartiallyCompatibleContent.para3
    }

    "have the correct bullet points" in {
      document.selectNth("ul.govuk-list--bullet > li", 1).text() shouldBe PartiallyCompatibleContent.bullet1Text
      document.selectNth("ul.govuk-list--bullet > li", 1).selectHead("a").attribute("href").getValue shouldBe PartiallyCompatibleContent.bullet1Link
      document.selectNth("ul.govuk-list--bullet > li", 2).text() shouldBe PartiallyCompatibleContent.bullet2Text
      document.selectNth("ul.govuk-list--bullet > li", 2).selectHead("a").attribute("href").getValue shouldBe PartiallyCompatibleContent.bullet2Link
    }

    "display the exit survey link" in {
      val link = document.mainContent.select(".govuk-link").get(3)
      link.text shouldBe PartiallyCompatibleContent.exitSurveyLinkTitle
      link.attr("href") shouldBe PartiallyCompatibleContent.exitSurveyLink
    }

  }

}


private object PartiallyCompatibleContent {
  val heading1 = "A1 Tax Stuff can send quarterly updates and is developing your tax return features"
  val heading2 = "What you should do next"
  val title = s"$heading1 - ${PageContentBase.title} - GOV.UK"
  val para1 = "Your software provider has built the features you need to send your quarterly updates. They are now developing the remaining features required for your tax return. These should be available in time for your 2026 to 2027 tax return."
  val para2 = "Learn more about your software and its upcoming features."
  val para3 = "If you want to use this software, you need to:"
  val bullet1Text = "sign up for Making Tax Digital for Income Tax (opens in new tab)"
  val bullet1Link = "https://www.gov.uk/guidance/sign-up-for-making-tax-digital-for-income-tax"
  val bullet2Text = "authorise your software for HMRC (opens in new tab)"
  val bullet2Link = "https://www.gov.uk/guidance/use-making-tax-digital-for-income-tax/get-your-software-ready"
  val exitSurveyLinkTitle = "Give feedback on this service (opens in new tab)"
  val exitSurveyLink = "http://localhost:9514/feedback/SOFTWAREMTDIT"
}