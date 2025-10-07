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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ChoosingSoftwareView

class ChoosingSoftwareViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[ChoosingSoftwareView]

  "ChoosingSoftware view" must {

    "have a title" in {
      document.title() shouldBe ChoosingSoftwareContent.title
    }

    "have a h1" in {
      document.selectHead("h1").text() shouldBe ChoosingSoftwareContent.heading
    }

    "have a back link" in {
      document.selectHead(".govuk-back-link").attr("href") shouldBe routes.CheckYourAnswersController.show().url
    }

    "have the correct paragraphs" in {
      document.mainContent.selectNth("p", 1).text() shouldBe ChoosingSoftwareContent.para1
      document.mainContent.selectNth("p", 2).text() shouldBe ChoosingSoftwareContent.para2
      document.mainContent.selectNth("p", 3).text() shouldBe ChoosingSoftwareContent.para3
      document.mainContent.selectNth("p", 4).text() shouldBe ChoosingSoftwareContent.para4
      document.mainContent.selectNth("p", 5).text() shouldBe ChoosingSoftwareContent.para5
    }

    "have the correct bullet points" in {
      val bulletPoints: Seq[Element] = document.mainContent.selectSeq(".govuk-list.govuk-list--bullet li")
      bulletPoints(0).text() shouldBe ChoosingSoftwareContent.bullet1
      bulletPoints(1).text() shouldBe ChoosingSoftwareContent.bullet2
    }

    "have a form" which {
      val form: Element = document.selectHead("form")

      "has the correct method and action" in {
        form.attr("method") shouldBe testCall.method
        form.attr("action") shouldBe testCall.url
      }

      "has a continue button" in {
        form.selectHead(".govuk-button").text() shouldBe ChoosingSoftwareContent.continue
      }
    }
  }

  def page: HtmlFormat.Appendable = view(
    postAction = testCall,
    backLink = routes.CheckYourAnswersController.show().url
  )

  def document: Document = Jsoup.parse(page.body)
}

private object ChoosingSoftwareContent {
  val title = s"Before you continue - ${PageContentBase.title} - GOV.UK"
  val heading = "Before you continue"
  val para1 = "Software products may have free trials or free versions, but you’ll have to pay for most of the software listed."
  val para2 = "All the products listed will allow you to submit your quarterly updates. Some will show additional features that are either:"
  val bullet1 = "ready now"
  val bullet2 = "in development"
  val para3 = "In development means one or more features you need to complete your end-of-year tax return are still being built. " +
    "We expect these features will be ready for you to do your 2026 to 2027 return."
  val para4 = "We recommend that you look at software providers’ websites to check if the product is right for you."
  val para5 = "All of this software has been through a recognition process where HMRC check how ready it is for you to submit your taxes. " +
    "HMRC does not endorse or recommend any one product or software provider."
  val continue = "Continue"
}
