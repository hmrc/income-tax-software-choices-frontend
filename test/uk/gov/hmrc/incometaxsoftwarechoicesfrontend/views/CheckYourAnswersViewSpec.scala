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

import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.CheckYourAnswersView


class CheckYourAnswersViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[CheckYourAnswersView]

  val summaryList: SummaryList = SummaryList(
    rows = Seq(
      SummaryListRow(
        key = Key(
          content = Text("Test Key"),
          classes = "govuk-!-static-padding-right-1"
        ),
        value = Value(
          content = HtmlContent("Test Value"),
          classes = "govuk-!-static-padding-right-1"
        ),
        actions = Some(Actions(
          items = Seq(
            ActionItem(
              href = "#",
              content = Text("Change"),
              visuallyHiddenText = Some("Change Test Key")
            )
          )
        ))
      )
    )
  )

  def page: HtmlFormat.Appendable = view(summaryList, testCall, backLink)


}
