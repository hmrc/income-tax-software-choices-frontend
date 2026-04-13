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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.{ChoosingSoftwareView, NeedAdditionalSoftwareView}

class NeedAdditionalSoftwareViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[NeedAdditionalSoftwareView]
  val page: HtmlFormat.Appendable = view(continueURL = testCall.url, backLink = testBackUrl)
  val document: Document = Jsoup.parse(page.body)
  "NeedAdditionalSoftware view" must {

    "have a title" in {
      document.title() shouldBe NeedAdditionalSoftwareContent.title
    }
    "have a back link" in {
      document.selectHead(".govuk-back-link").attr("href") shouldBe testBackUrl
    }
    "have a h1" in {
      document.selectHead("h1").text() shouldBe NeedAdditionalSoftwareContent.heading
    }

    "have the correct paragraphs" in {
      document.mainContent.selectNth("p", 1).text() shouldBe NeedAdditionalSoftwareContent.para1
      document.mainContent.selectNth("p", 2).text() shouldBe NeedAdditionalSoftwareContent.para2
      document.mainContent.selectNth("p", 3).text() shouldBe NeedAdditionalSoftwareContent.para3
    }

    "have the correct bullet points" in {
      val bulletPoints: Seq[Element] = document.mainContent.selectSeq(".govuk-list.govuk-list--bullet li")
      bulletPoints(0).text() shouldBe NeedAdditionalSoftwareContent.bullet1
      bulletPoints(1).text() shouldBe NeedAdditionalSoftwareContent.bullet2
      bulletPoints(2).text() shouldBe NeedAdditionalSoftwareContent.bullet3
    }

    "have a link button" in {
      val link: Element = document.selectHead("a.govuk-button")
      link.text() shouldBe NeedAdditionalSoftwareContent.button
      link.attr("href") shouldBe testCall.url
    }
  }
}

private object NeedAdditionalSoftwareContent {
  val heading = "You will need additional software"
  val title = s"$heading - ${PageContentBase.title} - GOV.UK"
  val para1 = "If you want to keep using your spreadsheets to record your income and expenses, you will need to use new software that connects to your digital records."
  val para2 = "You can also choose software that creates digital records. This may be the better option if you want one product to meet all your needs."
  val para3 = "This software is able to:"
  val bullet1 = "create digital records"
  val bullet2 = "send quarterly updates to HMRC"
  val bullet3 = "submit tax returns"
  val button = "Find compatible software"
}