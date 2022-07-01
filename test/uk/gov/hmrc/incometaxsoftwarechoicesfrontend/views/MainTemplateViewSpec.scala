/*
 * Copyright 2022 HM Revenue & Customs
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
import play.twirl.api.Html
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.templates.MainTemplate

class MainTemplateViewSpec extends ViewSpec {
  "Main Template" should {
    "have the report technical issues link" in {
      document.getTechnicalHelpLink shouldBe "http://localhost:9250/contact/report-technical-problem?newTab=true&service=ITSC&referrerUrl=%2F"
      document.getTechnicalHelpLinkText shouldBe "Is this page not working properly? (opens in new tab)"
    }
  }

  private val mainTemplate = app.injector.instanceOf[MainTemplate]
  private def document: Document = Jsoup.parse(mainTemplate(title = "test title")(Html("Content")).body)
}
