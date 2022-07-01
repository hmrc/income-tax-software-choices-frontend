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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwarePage

class SearchSoftwareViewSpec extends ViewSpec {
  private val lastUpdateTest = "01/07/2022"

  "Search software page" should {
    "have a breadcrumb menu" which {
      "contains to guidance page" in {
        document.selectNth(".govuk-breadcrumbs__list-item", 1).text shouldBe "Guidance"
      }

      "contains the current page" in {
        document.selectNth(".govuk-breadcrumbs__list-item", 2).text shouldBe "Filter"
      }
    }

    "have a title" in {
      document.title shouldBe s"""${SearchSoftwarePage.title} - Find software for Making Tax Digital for Income Tax - GOV.UK"""
    }

    "have the last updated date" in {
      document.selectNth("p", 1).text shouldBe SearchSoftwarePage.lastUpdate
    }

    "have a heading" in {
      document.select("h1").text shouldBe SearchSoftwarePage.heading
    }

    "have paragraph1" in {
      document.selectNth("p", 2).text shouldBe SearchSoftwarePage.paragraph1
    }

    "have paragraph2" in {
      document.selectNth("p", 3).text shouldBe SearchSoftwarePage.paragraph2
    }

    "have inset text" in {
      document.select(".govuk-inset-text").text shouldBe SearchSoftwarePage.insetText
    }
  }

  object SearchSoftwarePage {
    val title = "Choose the right software for your needs"
    val lastUpdate = "This page was last updated: 01/07/2022"
    val heading = "Choose the right software for your needs"
    val paragraph1 = "All software on this page has been through HMRC’s recognition process. " +
                     "But we do not endorse or recommend any one product or software provider."
    val paragraph2 = "Some software has features suitable if you have accessibility needs, like visual impairment or limited movement."
    val insetText = "If you need help to choose software, contact the software provider before making a decision. We are not able to help you choose software."
  }

  private val searchSoftwarePage = app.injector.instanceOf[SearchSoftwarePage]
  private def page = searchSoftwarePage(lastUpdate = lastUpdateTest)
  private def document: Document = Jsoup.parse(page.body)
}
