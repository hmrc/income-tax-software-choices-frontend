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
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.GlossaryPage

class GlossaryViewSpec extends ViewSpec {


  import GlossaryPageContent._
  import GlossaryViewSpec._

  "Glossary page" when {
    "no actual content(!)" should {
      val document = getDocument(noItems, 1)

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
          document.select(".inverse-header").select(".govuk-body").text() shouldBe caption
        }
        "has content in the heading" in {
          document.select(".govuk-label").text() shouldBe content
        }
      }
    }
    "given three initials for items of content, with max without links at four" should {
      val document = getDocument(threeItems, 4)
      "have no magic links" in {
        getHashLinks(document).size() shouldBe 0
      }
    }
    "given three initials for items of content, with max without links at two" should {
      val document = getDocument(threeItems, 2)
      "have three magic links" in {
        getHashLinks(document).size() shouldBe 3
      }
    }
  }

  private def getHashLinks(document: Document) = document.select("a[href^=#]").select("a[href!=#main-content]")
}

object GlossaryViewSpec extends ViewSpec {

  private val glossaryPage = app.injector.instanceOf[GlossaryPage]

  def page(initialsToMessagePairsList: List[(String, List[(String, String)])], maxWithoutLinks: Int): HtmlFormat.Appendable =
    glossaryPage(
      initialsToMessagePairsList,
      maxWithoutLinks
    )


  def getDocument(initialsToMessagePairsList: List[(String, List[(String, String)])], maxWithoutLinks: Int): Document = {
    Jsoup.parse(page(initialsToMessagePairsList, maxWithoutLinks).body)
  }

}

private object GlossaryPageContent {
  val title = "Glossary - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"
  val glossary = "Glossary"
  val content = "Content"
  val softwareChoices = "Software Choices"
  val caption: String = "Lorem Ipsum"

  val noItems: List[Nothing] = List.empty
  val threeItems = List("A" -> List("Apple" -> "Apple"), "B" -> List("Banana" -> "Banana"), "C" -> List("Coconut" -> "Coconut"))
}
