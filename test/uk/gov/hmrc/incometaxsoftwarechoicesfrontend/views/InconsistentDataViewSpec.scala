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
import org.scalatest.matchers.must.Matchers._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.InconsistentDataView

class InconsistentDataViewSpec extends ViewSpec {

  private val view =
    app.injector.instanceOf[InconsistentDataView]

  "InconsistentDataView" should {
    val document = Jsoup.parse(view().body)
    "have the correct title" in {
      document.title() mustBe InconsistentDataContent.title
    }

    "have the correct heading" in {
      document.selectHead("h1").text mustBe InconsistentDataContent.heading
    }

    "have the correct first para" in {
      document.selectNth("p.govuk-body", 1).text mustBe InconsistentDataContent.p1
    }

    "have the correct second para" in {
      document.selectNth("p.govuk-body", 2).text mustBe InconsistentDataContent.p2
    }

    "have the correct link" in {
      document.selectNth("p.govuk-body", 2).selectHead("a").attribute("href").getValue.contains(InconsistentDataContent.link) mustBe true
    }
  }
}

private object InconsistentDataContent {
  val title = s"Sorry, we could not process your request - ${PageContentBase.title} - GOV.UK"
  val heading = "Sorry, we could not process your request"
  val p1 = "The information needed to display this page is missing or invalid."
  val p2 = "You need to start again."
  val link = "/find-making-tax-digital-income-tax-software"
}
