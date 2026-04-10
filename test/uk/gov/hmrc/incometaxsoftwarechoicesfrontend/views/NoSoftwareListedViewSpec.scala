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
import org.scalatest.matchers.must.Matchers.*
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.NoSoftwareListedView

class NoSoftwareListedViewSpec extends ViewSpec {
  private val view = app.injector.instanceOf[NoSoftwareListedView]

  def page(): HtmlFormat.Appendable = {
    view(finishAction = testCall, backLink = testBackUrl)
  }

  def document(): Document = Jsoup.parse(page().body)

  "NoSoftwareListedView" when {

    "have the correct main heading" in {
      document().selectHead("h1").text() mustBe NoSoftwareListedViewContent.heading
    }

    "have the correct first sub heading" in {
      document().mainContent.select("h2").get(0).text() mustBe NoSoftwareListedViewContent.subHeading1
    }

    "have the correct first paragraph text" in {
      document().mainContent.select("p").get(0).text mustBe NoSoftwareListedViewContent.paragraph1
    }

    "have the correct second sub heading" in {
      document().mainContent.select("h2").get(1).text() mustBe NoSoftwareListedViewContent.subHeading2
    }

    "have the correct second paragraph text" in {
      document().mainContent.select("p").get(1).text mustBe NoSoftwareListedViewContent.paragraph2
    }

    "have the correct third paragraph text" in {
      document().mainContent.select("p").get(2).text mustBe NoSoftwareListedViewContent.paragraph3
    }

    "have the correct forth paragraph text" in {
      document().mainContent.select("p").get(3).text mustBe NoSoftwareListedViewContent.paragraph4
    }

    "have a bullet list" which {
      lazy val bulletList = document().mainContent.selectHead("ul.govuk-list.govuk-list--bullet")

      "has a first point" in {
        bulletList.selectNth("li", 1).text mustBe NoSoftwareListedViewContent.bulletPoint1
      }
      "has a second point" in {
        bulletList.selectNth("li", 2).text mustBe NoSoftwareListedViewContent.bulletPoint2
      }
      "has a third point" in {
        bulletList.selectNth("li", 3).text mustBe NoSoftwareListedViewContent.bulletPoint3
      }
    }

    "have a finish button" in {
      document().select("form").select(".govuk-button").text() shouldBe NoSoftwareListedViewContent.finishSoftwareButton
    }
  }
}

private object NoSoftwareListedViewContent {
  val title = "You may need additional software"
  val heading = "You may need additional software"
  val subHeading1 = "If your software is not listed"
  val paragraph1 = "You may want to contact your current software provider to see if they are going to support Making Tax Digital for Income Tax. You can also search for other software packages to see if they are suitable."
  val subHeading2 = "If you are using spreadsheets to record your income and expenses"
  val paragraph2 = "You will need to use software that connects to your digital records if you want to keep using your spreadsheets. This is called bridging software."
  val paragraph3 = "You can also choose software that creates digital records. This may be the better option if you want one product to meet all your needs."
  val paragraph4 = "This software is able to:"
  val bulletPoint1 = "create digital records"
  val bulletPoint2 = "send quarterly updates to HMRC"
  val bulletPoint3 = "submit tax returns"
  val finishSoftwareButton = "Find compatible software"
}
