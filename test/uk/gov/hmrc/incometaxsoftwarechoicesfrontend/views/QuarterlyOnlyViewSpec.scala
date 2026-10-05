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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.{Agent, SoleTraderOrLandlord}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.QuarterlyOnlyView

class QuarterlyOnlyViewSpec extends ViewSpec {

  private val softwareName = "A1 Tax Stuff"
  private val view = app.injector.instanceOf[QuarterlyOnlyView]
  val resultsUrl = "/test-software-results-url"
  val page: HtmlFormat.Appendable = view(
    productDetailsUrl = testCall.url,
    backLink = testBackUrl,
    chosenSoftware = softwareName,
    softwareResultsUrl = resultsUrl,
    userType = Some(SoleTraderOrLandlord)
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

    "have the correct paragraphs" in {
      document.mainContent.selectNth("p", 1).text() shouldBe QuarterlyOnlyContent.para1
      document.mainContent.selectNth("p", 2).text() shouldBe QuarterlyOnlyContent.para2
      document.mainContent.selectNth("p", 2).selectHead("a").attribute("href").getValue shouldBe testCall.url
      document.mainContent.selectNth("p", 3).text() shouldBe QuarterlyOnlyContent.para3
      document.mainContent.selectNth("p", 3).selectHead("a").attribute("href").getValue shouldBe resultsUrl

    }

    "display the getting started section" which {

      "has the getting started heading" in {
        document.selectHead("h2").text shouldBe QuarterlyOnlyContent.heading2
      }

      "has the getting started text" in {
        document.select(".app-getting-started-box > p").text shouldBe QuarterlyOnlyContent.para4
      }

      "for an individual (SoleTraderOrLandlord) user type" should {

        "have a link to sign up for MTD for an individual" in {
          document.selectNth("ul.govuk-list--bullet > li", 1).text() shouldBe QuarterlyOnlyContent.bullet1TextIndividual
          document.selectNth("ul.govuk-list--bullet > li", 1).selectHead("a").attribute("href").getValue shouldBe QuarterlyOnlyContent.bullet1LinkIndividual
        }
      }

      "for an agent user type" should {
        val agentPage: HtmlFormat.Appendable = view(
          productDetailsUrl = testCall.url,
          backLink = testBackUrl,
          chosenSoftware = softwareName,
          softwareResultsUrl = resultsUrl,
          userType = Some(Agent)
        )
        val agentDocument: Document = Jsoup.parse(agentPage.body)

        "have a link to sign up for MTD as an agent" in {
          agentDocument.selectNth("ul.govuk-list--bullet > li", 1).text() shouldBe QuarterlyOnlyContent.bullet1TextAgent
          agentDocument.selectNth("ul.govuk-list--bullet > li", 1).selectHead("a").attribute("href").getValue shouldBe QuarterlyOnlyContent.bullet1LinkAgent
        }
      }

      "for an unspecified user type (no answer given)" should {
        val unspecifiedPage: HtmlFormat.Appendable = view(
          productDetailsUrl = testCall.url,
          backLink = testBackUrl,
          chosenSoftware = softwareName,
          softwareResultsUrl = resultsUrl,
          userType = None
        )
        val unspecified: Document = Jsoup.parse(unspecifiedPage.body)
        "have a link to sign up for MTD for an individual" in {
          unspecified.selectNth("ul.govuk-list--bullet > li", 1).text() shouldBe QuarterlyOnlyContent.bullet1TextIndividual
          unspecified.selectNth("ul.govuk-list--bullet > li", 1).selectHead("a").attribute("href").getValue shouldBe QuarterlyOnlyContent.bullet1LinkIndividual
        }
      }

      "has a link to authorise this software for HMRC" in {
        document.selectNth("ul.govuk-list--bullet > li", 2).text() shouldBe QuarterlyOnlyContent.bullet2Text
        document.selectNth("ul.govuk-list--bullet > li", 2).selectHead("a").attribute("href").getValue shouldBe QuarterlyOnlyContent.bullet2Link
      }
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
  val heading2 = "Getting started with this software"
  val title = s"$heading1 - ${PageContentBase.title} - GOV.UK"
  val para1 = "This software does not have the ability to submit a tax return."
  val para2 = "Learn more about this software and its upcoming features"
  val para3 = "Additional software will be required to complete tax returns."
  val para4 = "The following will need to be completed, if not already done so:"
  val bullet1TextIndividual = "sign up for Making Tax Digital for Income Tax (opens in new tab)"
  val bullet1TextAgent = "sign up your client for Making Tax Digital for Income Tax (opens in new tab)"
  val bullet1LinkIndividual = "https://www.gov.uk/guidance/sign-up-for-making-tax-digital-for-income-tax"
  val bullet1LinkAgent = "https://www.gov.uk/guidance/sign-up-your-client-for-making-tax-digital-for-income-tax"
  val bullet2Text = "authorise this software for HMRC (opens in new tab)"
  val bullet2Link = "https://www.gov.uk/guidance/use-making-tax-digital-for-income-tax/get-your-software-ready"
  val exitSurveyLinkTitle = "Give feedback on this service (opens in new tab)"
  val exitSurveyLink = "http://localhost:9514/feedback/SOFTWAREMTDIT?useServiceNavigation"
}