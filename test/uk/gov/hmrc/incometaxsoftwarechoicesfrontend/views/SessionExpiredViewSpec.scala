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
import org.scalatest.matchers.must.Matchers.*
import play.api.mvc.Call
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.DigitalAssistant
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SessionExpiredView
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.TimeoutType.*

class SessionExpiredViewSpec extends ViewSpec with FeatureSwitching{

  private val view =
    app.injector.instanceOf[SessionExpiredView]

  private val document =
    Jsoup.parse(view(postAction = Call("", ""), Manual).body)

  private val form =
    document.mainContent.selectHead("form")

  "SessionExpiredView" when {
    "user deletes answers (manual)" should {
      val document = Jsoup.parse(view(postAction = Call("", ""), Manual).body)
      "have the correct title" in {
        document.title() mustBe SessionExpiredContent.titleManual
      }

      "have the correct heading" in {
        document.selectHead("h1").text mustBe SessionExpiredContent.userHeading
      }

      "have a continue button" in {
        form.selectNth(".govuk-button", 1).text mustBe SessionExpiredContent.continue
      }

      "not insert the Nuance elements, even when the Digital Assistant feature switch is enabled" in {
        enable(DigitalAssistant)
        document.getElementById("HMRC_CIAPI_Anchored_1") mustBe null
        document.getElementById("webchat-tag") mustBe null
        document.getElementById("tc-nuance-chat-container") mustBe null
      }
    }
    "session expired due to inactivity (expired)" should {
      val document = Jsoup.parse(view(postAction = Call("", ""), Timeout).body)
      "have the correct title" in {
        document.title() mustBe SessionExpiredContent.titleTimeout
      }

      "have the correct heading" in {
        document.selectHead("h1").text mustBe SessionExpiredContent.timeoutHeading
      }
      "have the correct paragraph" in {
        document.mainContent.selectHead("p").text mustBe SessionExpiredContent.timeoutparagraph
      }

      "have a continue button" in {
        form.selectNth(".govuk-button", 1).text mustBe SessionExpiredContent.continue
      }
    }
    "no all-in-one product found (auto)" should {
      val document = Jsoup.parse(view(postAction = Call("", ""), Auto).body)
      "have the correct title" in {
        document.title() mustBe SessionExpiredContent.titleAuto
      }
      "have the correct heading" in {
        document.selectHead("h1").text mustBe SessionExpiredContent.autoHeading
      }
      "have a continue button" in {
        form.selectNth(".govuk-button", 1).text mustBe SessionExpiredContent.continue
      }
    }
  }
}

private object SessionExpiredContent {
  val titleManual = s"You deleted your answers - ${PageContentBase.title} - GOV.UK"
  val titleTimeout = s"Session timed out due to inactivity - ${PageContentBase.title} - GOV.UK"
  val titleAuto = s"For security, we deleted your answers - ${PageContentBase.title} - GOV.UK"
  val userHeading = "You deleted your answers"
  val timeoutHeading = "Session timed out due to inactivity"
  val autoHeading = "For security, we deleted your answers"
  val continue = "Start again"
  val timeoutparagraph = "Your session timed out after 15 minutes of inactivity, so we cleared your information to keep it secure. You will need to start again."

}
