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

    "have the correct sub headings" in {
      document.mainContent.selectNth("h2", 1).text() shouldBe ChoosingSoftwareContent.h2one
      document.mainContent.selectNth("h2", 2).text() shouldBe ChoosingSoftwareContent.h2two
    }

    "have the correct paragraphs" in {
      document.mainContent.selectNth("p", 1).text() shouldBe ChoosingSoftwareContent.para1
      document.mainContent.selectNth("p", 2).text() shouldBe ChoosingSoftwareContent.para2
      document.mainContent.selectNth("p", 3).text() shouldBe ChoosingSoftwareContent.para3
      document.mainContent.selectNth("p", 4).text() shouldBe ChoosingSoftwareContent.para4
      document.mainContent.selectNth("p", 5).text() shouldBe ChoosingSoftwareContent.para5
      document.mainContent.selectNth("p", 6).text() shouldBe ChoosingSoftwareContent.para6
      document.mainContent.selectNth("p", 7).text() shouldBe ChoosingSoftwareContent.para7
      document.mainContent.selectNth("p", 8).text() shouldBe ChoosingSoftwareContent.para8
    }

    "have the correct bullet points" in {
      val bulletPoints: Seq[Element] = document.mainContent.selectSeq(".govuk-list.govuk-list--bullet li")
      bulletPoints(0).text() shouldBe ChoosingSoftwareContent.bullet1
      bulletPoints(1).text() shouldBe ChoosingSoftwareContent.bullet2
      bulletPoints(2).text() shouldBe ChoosingSoftwareContent.bullet3
      bulletPoints(3).text() shouldBe ChoosingSoftwareContent.bullet4
    }

    "have a form" which {
      val form: Element = document.selectHead("form")

      "has the correct method and action" in {
        form.attr("method") shouldBe testCall.method
        form.attr("action") shouldBe testCall.url
      }

      "has a continue button" in {
        form.selectHead(".govuk-button").text() shouldBe ChoosingSoftwareContent.button
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
  val heading = "How to choose software from your results"
  val title = s"$heading - ${PageContentBase.title} - GOV.UK"
  val para1 = "HMRC have committed to build all the features you need, ready for when you submit your 2026 to 2027 " +
    "tax return. The software providers listed have also either already built, or have committed to build these " +
    "features ready for when you need to submit your 2026 to 2027 tax return."
  val para2 = "All software listed has been through HMRC’s recognition process. HMRC does not endorse or recommend any one product or software provider."
  val h2one = "Future software updates for your 2026 to 2027 tax return"
  val para3 = "Some of the features you will need to submit your tax return are still being developed by some software " +
    "providers and HMRC. They are either shown as:"
  val bullet1 = "‘ready now’ or"
  val bullet2 = "‘in development’"
  val para4 = "All the software products in your results will let you or your agent:"
  val bullet3 = "create, store and correct digital records of your self-employment and property income and expenses"
  val bullet4 = "send your quarterly updates to HMRC"
  val para5 = "This is everything you need to start using Making Tax Digital for Income Tax from 6 April 2026."
  val h2two = "How to make sure you get the right software"
  val para6 = "Some of the listed products may have free trials or free versions, but you’ll have to pay for others."
  val para7 = "HMRC is not responsible for the availability of products or making sure that the product you chose meets your current and future needs."
  val para8 = "We recommend that you visit software providers’ websites to do more research before choosing a product."
  val button = "View your results"
}
