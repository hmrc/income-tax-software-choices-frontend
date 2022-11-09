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
import org.jsoup.nodes.{Document, Element}
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.GlossaryForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.GlossaryPage

class GlossaryViewSpec extends ViewSpec {

  import GlossaryPageContent._
  import GlossaryViewSpec._

  "Glossary page" when {
    "no actual content(!)" should {
      val document = getDocument(noItems, 1, lastChanged)

      "have a breadcrumb menu containing a link to the guidance page" in {
        val link = document.selectNth(".govuk-breadcrumbs__list-item", 1).selectHead("a")
        link.text shouldBe "Guidance"
        link.attr("href") shouldBe appConfig.guidance
      }

      "have a title" in {
        document.title shouldBe title
      }

      "have a header block" which {
        "has software choices in the heading" in {
          document.select(".inverse-header").select(".govuk-caption-l").text() shouldBe softwareChoices
        }
        "has glossary in the heading" in {
          document.select(".inverse-header").select(".govuk-heading-xl").text() shouldBe glossary
        }
        "has a caption in the heading" in {
          document.select(".inverse-header").select(".govuk-body").text() shouldBe s"$caption: $lastChanged"
        }
      }

      "have a form" which {
        def form: Element = document.mainContent.selectHead("form")

        "has the correct attributes" in {
          form.attr("method") shouldBe "POST"
          form.attr("action") shouldBe routes.GlossaryController.search(ajax = false).url
        }
        "has a search bar section" which {
          "has a heading label" in {
            val label: Element = form.selectNth("h2", 1).selectHead("label")
            label.attr("for") shouldBe GlossaryForm.searchTerm
            label.text shouldBe searchFor
          }
          "has a text field" in {
            val textField: Element = form.selectHead("input[type=text]")
            textField.attr("name") shouldBe GlossaryForm.searchTerm
            textField.attr("id") shouldBe GlossaryForm.searchTerm
          }
        }
        "has a sort section" which {
          "has a heading label" in {
            val label: Element = form.selectNth("h2", 2).selectHead("label")
            label.attr("for") shouldBe GlossaryForm.sortTerm
            label.text shouldBe sortBy
          }
          "has a dropdown" in {
            val textField: Element = form.selectHead("select")
            textField.attr("name") shouldBe GlossaryForm.sortTerm
            textField.attr("id") shouldBe GlossaryForm.sortTerm
          }
        }
      }
    }
    "given three initials for items of content, with max without links at four" should {
      val moreThanThreeToShowLinks = 4
      val document = getDocument(threeItems, moreThanThreeToShowLinks, lastChanged)
      "have no magic links" in {
        getHashLinks(document).size() shouldBe 0
      }
    }
    "given three initials for items of content, with max without links at two" should {
      val moreThanTwoToShowLinks = 2
      val document = getDocument(threeItems, moreThanTwoToShowLinks, lastChanged)
      "have three magic links" in {
        getHashLinks(document).size() shouldBe 3
      }
    }
  }

  private def getHashLinks(document: Document) = document.select("a[href^=#]").select("a[href!=#main-content]")
}

object GlossaryViewSpec extends ViewSpec {

  private val glossaryPage = app.injector.instanceOf[GlossaryPage]

  private val glossaryCall: Call = routes.GlossaryController.search(false)

  def page(initialsToMessagePairsList: List[(String, List[(String, String)])], maxWithoutLinks: Int, lastChanged: String): HtmlFormat.Appendable =
    glossaryPage(
      initialsToMessagePairsList,
      maxWithoutLinks,
      lastChanged,
      GlossaryForm.form,
      glossaryCall
    )


  def getDocument(initialsToMessagePairsList: List[(String, List[(String, String)])], maxWithoutLinks: Int, lastChanged: String): Document = {
    Jsoup.parse(page(initialsToMessagePairsList, maxWithoutLinks, lastChanged).body)
  }

}

private object GlossaryPageContent {
  val glossary = "Self Assessment income and deduction types explained"
  val title = s"$glossary - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"
  val content = "Content"
  val softwareChoices = "Software Choices"
  val caption: String = "Last changed"
  val lastChanged: String = "St Crispin's Day"

  val searchFor: String = "Search for the term you are looking for"
  val sortBy: String = "Sort by"

  val noItems: List[Nothing] = List.empty
  val threeItems: List[(String, List[(String, String)])] =
    List("A" -> List("Apple" -> "Apple"), "B" -> List("Banana" -> "Banana"), "C" -> List("Coconut" -> "Coconut"))
}
