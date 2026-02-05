/*
 * Copyright 2023 HM Revenue & Customs
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
import play.twirl.api.Html
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.templates.MainTemplate

class MainTemplateViewSpec extends ViewSpec {
  "Main Template" should {

    "have link to Index page" in {
      document.getServiceLink shouldBe "/find-making-tax-digital-income-tax-software"
      document.getServiceName shouldBe "Find software that works with Making Tax Digital for Income Tax"
    }

    "have the report technical issues link" in {
      document.getTechnicalHelpLink shouldBe "http://localhost:9250/contact/report-technical-problem?service=ITSC&referrerUrl=%2F"
      document.getTechnicalHelpLinkText shouldBe "Is this page not working properly? (opens in new tab)"
    }

    "have a Beta banner" in {
      val banner: Element = document.selectHead(".govuk-phase-banner")
      banner.selectHead(".govuk-phase-banner__content__tag").text shouldBe "Beta"

      val bannerContent: Element = banner.selectHead(".govuk-phase-banner__text")
      bannerContent.text shouldBe "This is a new service. Help us improve it and give your feedback (opens in new tab)"

      val feedbackLink: Element = bannerContent.selectHead("a")
      feedbackLink.text shouldBe "give your feedback (opens in new tab)"
      feedbackLink.attr("href") shouldBe "http://localhost:9514/feedback/SOFTWAREMTDIT"
    }

    "have a link to the accessibility statement for the service" in {
      val accessibilityStatementLink: Element = document.selectNth(".govuk-footer__inline-list-item", 2).selectHead("a")

      accessibilityStatementLink.text shouldBe "Accessibility statement"
      accessibilityStatementLink.attr("href") shouldBe "http://localhost:12346/accessibility-statement/income-tax-software-choices?referrerUrl=%2F"
    }
  }

  private val mainTemplate = app.injector.instanceOf[MainTemplate]
  private def document: Document = Jsoup.parse(mainTemplate(title = "test title")(Html("Content")).body)
}
