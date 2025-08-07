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
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.Call
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SessionExpiredView

class SessionExpiredViewSpec extends ViewSpec {

  private val view =
    app.injector.instanceOf[SessionExpiredView]

  private val document =
    Jsoup.parse(view(postAction = Call("", "")).body)

  private val form =
    document.mainContent.selectHead("form")

  "SessionExpiredView" when {
    "have the correct title" in {
      document.title() mustBe SessionExpiredContent.title
    }

    "have the correct heading" in {
      document.selectHead("h1").text mustBe SessionExpiredContent.heading
    }

    "have the correct paragraph" in {
      document.selectNth(".govuk-body", 1).text mustBe SessionExpiredContent.paragraph
    }

    "have a continue button" in {
      form.selectNth(".govuk-button", 1).text mustBe SessionExpiredContent.continue
    }
  }
}

private object SessionExpiredContent {
  val title = s"Session Expired - ${PageContentBase.title} - GOV.UK"
  val heading = "Session Expired"
  val paragraph = "Your session has expired and data has been lost."
  val continue = "Start again"
}
