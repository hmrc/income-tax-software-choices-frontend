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

    "have the correct first paragraph text" in {
      document().mainContent.select("p").get(0).text mustBe NoSoftwareListedViewContent.paragraph1
    }

    "have a finish button" in {
      document().select("form").select(".govuk-button").text() shouldBe NoSoftwareListedViewContent.finishSoftwareButton
    }
  }
}

private object NoSoftwareListedViewContent {
  val title = "This software is not recognised for Making Tax Digital for Income Tax"
  val heading = "This software is not recognised for Making Tax Digital for Income Tax"
  val paragraph1 = "You may want to contact your current software provider to see if they are going to support Making Tax Digital for Income Tax. You can also search for other software packages to see if they are suitable."
  val finishSoftwareButton = "Find compatible software"
}
