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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.NeedAdditionalSoftwareView

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
    }
    
    "have the correct bullet points" in {
      document.mainContent.selectNth("li", 1).text() shouldBe NeedAdditionalSoftwareContent.bullet1
      document.mainContent.selectNth("li", 2).text() shouldBe NeedAdditionalSoftwareContent.bullet2
    }

    "have a link button" in {
      val link: Element = document.selectHead("a.govuk-button")
      link.text() shouldBe NeedAdditionalSoftwareContent.button
      link.attr("href") shouldBe testCall.url
    }
  }
}

private object NeedAdditionalSoftwareContent {
  val heading = "Additional software is required"
  val title = s"$heading - ${PageContentBase.title} - GOV.UK"
  val para1 = "If spreadsheets are used to record income and expenses, additional software will be required that connects to digital records."
  val para2 = "Software that creates digital records can also be used. This may be a better option if one product is needed to meet all of the requirements and let you:"
  val bullet1 = "send quarterly updates"
  val bullet2 = "submit tax returns"
  val button = "Find compatible software"
}