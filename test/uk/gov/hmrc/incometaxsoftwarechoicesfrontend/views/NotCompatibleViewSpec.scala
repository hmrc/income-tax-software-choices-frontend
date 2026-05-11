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
import org.jsoup.nodes.{Document, Element}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.NotCompatibleView

class NotCompatibleViewSpec extends ViewSpec {

  private val softwareName = "A1 Tax Stuff"
  private val view = app.injector.instanceOf[NotCompatibleView]
  val page: HtmlFormat.Appendable = view(continueURL = testCall.url, backLink = testBackUrl, chosenSoftware = softwareName)
  val document: Document = Jsoup.parse(page.body)
  "NotCompatible view" must {

    "have a title" in {
      document.title() shouldBe NotCompatibleContent.title
    }

    "have a back link" in {
      document.selectHead(".govuk-back-link").attr("href") shouldBe testBackUrl
    }

    "have an h1" in {
      document.selectHead("h1").text() shouldBe NotCompatibleContent.heading
    }

    "have the correct paragraphs" in {
      document.mainContent.selectNth("p", 1).text() shouldBe NotCompatibleContent.para1
      document.mainContent.selectNth("p", 2).text() shouldBe NotCompatibleContent.para2
    }

    "have a link button" in {
    val link: Element = document.selectHead("a.govuk-button")
    link.text() shouldBe NeedAdditionalSoftwareContent.button
    link.attr("href") shouldBe testCall.url
    }

  }

}


private object NotCompatibleContent {
  val heading = "A1 Tax Stuff is not currently compatible"
  val title = s"$heading - ${PageContentBase.title} - GOV.UK"
  val para1 = "You may want to contact your current software provider to see if they are going to support Making Tax Digital for Income Tax for your circumstances."
  val para2 = "You can also search for other software packages that are currently ready for Making Tax Digital for Income Tax to see if they are suitable."
  val button = "Find compatible software"
}