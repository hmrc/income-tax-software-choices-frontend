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
import org.scalatest.matchers.must.Matchers._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.NotFoundView

class NotFoundViewSpec extends ViewSpec {

  private val view =
    app.injector.instanceOf[NotFoundView]

  "NotFoundView" when {
    "passed a simple url" should {
      val document = Jsoup.parse(view(url = "simple").body)
      "have the correct title" in {
        document.title() mustBe NotFoundContent.title
      }

      "have the correct heading" in {
        document.selectHead("h1").text mustBe NotFoundContent.heading
      }

      "have the correct first para" in {
        document.selectNth("p.govuk-body", 1).text mustBe NotFoundContent.p1
      }

      "have the correct second para" in {
        document.selectNth("p.govuk-body", 2).text mustBe NotFoundContent.p2
      }

      "have the correct third para" in {
        document.selectNth("p.govuk-body", 3).text mustBe NotFoundContent.p3
      }

      "have the correct first bullet item" in {
        document.selectNth("ul.govuk-list--bullet > li", 1).text mustBe NotFoundContent.bullet1
      }

      "have the correct first bullet link" in {
        document.selectNth("ul.govuk-list--bullet > li", 1).selectHead("a").attribute("href").getValue.contains(NotFoundContent.link1) mustBe true
      }

      "have the correct second bullet item" in {
        document.selectNth("ul.govuk-list--bullet > li", 2).text mustBe NotFoundContent.bullet2
      }

      "have the correct second bullet link" in {
        document.selectNth("ul.govuk-list--bullet > li", 2).selectHead("a").attribute("href").getValue.contains(NotFoundContent.link2) mustBe true
      }
    }

    "passed a complex url" should {
      val document = Jsoup.parse(view(url = "@complex/").body)

      "have the correct complex bullet item" in {
        document.selectNth("ul.govuk-list--bullet > li", 1).text mustBe NotFoundContent.bullet1
      }

      "have the correct complex bullet link" in {
        document.selectNth("ul.govuk-list--bullet > li", 1).selectHead("a").attribute("href").getValue.contains(NotFoundContent.link3) mustBe true
      }
    }
  }
}

private object NotFoundContent {
  val title = s"Page not found - ${PageContentBase.title} - GOV.UK"
  val heading = "Page not found"
  val p1 = "If you typed the web address, check it is correct."
  val p2 = "If you pasted the web address, check you copied the entire address."
  val p3 = "If the web address is correct or you selected a link or button, you can:"
  val bullet1 = "contact us about this problem (opens in new tab) or"
  val bullet2 = "find software that works with Making Tax Digital for Income Tax"
  val link1 = "/contact/report-technical-problem?service=ITSC&referrerUrl=simple"
  val link2 = "find-making-tax-digital-income-tax-software"
  val link3 = "/contact/report-technical-problem?service=ITSC&referrerUrl=%40complex%2F"
}
