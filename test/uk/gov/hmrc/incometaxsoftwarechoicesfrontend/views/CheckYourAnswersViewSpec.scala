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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.CheckYourAnswersView

class CheckYourAnswersViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[CheckYourAnswersView]

  private val rowContent: Seq[(String, String)] = Seq(
    ("Test Key 1", "Test Value 1"),
    ("Test Key 2", "Test Value 2"),
    ("Test Key 3", "Test Value 3"),
    ("Test Key 4", "Test Value 4")
  )

  "CheckYourAnswersView" must {
    "have a title" in {
      document.title() shouldBe CheckYourAnswersViewContent.title
    }
    "have a backlink" in {
      document.selectHead(".govuk-back-link").attr("href") shouldBe testBackUrl
    }
    "have a h1 heading" in {
      document.selectHead("h1").text() shouldBe CheckYourAnswersViewContent.heading
    }
    "render the summary list correctly" which {
      def getRow(index: Int): Element = document.getSummaryListRow(index)

      (1 to rowContent.length).zip(rowContent).foreach { case (row, (key, value)) =>

        s"has the correct details for $key" in {
          getRow(row).getSummaryListKey.text() shouldBe key
          getRow(row).getSummaryListValue.text() shouldBe value
          getRow(row).getSummaryListActions.selectHead("a").text() shouldBe s"Change $key"
          getRow(row).getSummaryListActions.selectHead("a").attr("href") shouldBe s"#$key"
          getRow(row).getSummaryListActions.selectHead("a").selectHead("span.govuk-visually-hidden").text() shouldBe s"$key"
        }
      }
    }
    "have a continue button" in {
      document.select("form").select(".govuk-button").text() shouldBe CheckYourAnswersViewContent.continue
    }
  }

  def page: HtmlFormat.Appendable = view(summaryList, testCall, testBackUrl)

  def document: Document = Jsoup.parse(page.body)

  private val summaryList: SummaryList = SummaryList(
    rows = rowContent.map {
      case (key, value) => SummaryListRow(
        key = Key(
          content = Text(key),
          classes = "govuk-!-static-padding-right-1"
        ),
        value = Value(
          content = HtmlContent(value),
          classes = "govuk-!-static-padding-right-1"
        ),
        actions = Some(Actions(
          items = Seq(
            ActionItem(
              href = s"#$key",
              content = Text("Change"),
              visuallyHiddenText = Some(key)
            )
          )
        ))
      )
    }
  )

  object CheckYourAnswersViewContent {
    val title = "Check your answers before viewing compatible software - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"
    val heading = "Check your answers before viewing compatible software"
    val continue = "Continue"
  }

}
