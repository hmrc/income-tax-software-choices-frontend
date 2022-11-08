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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services

import org.mockito.ArgumentMatchers
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Langs, Messages, MessagesApi}
import play.api.test.StubMessagesFactory
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.GlossaryFormModel
import uk.gov.hmrc.play.language.LanguageUtils

class GlossaryServiceSpec extends PlaySpec with BeforeAndAfterEach with GuiceOneAppPerSuite with StubMessagesFactory {

  val languageUtils: LanguageUtils = app.injector.instanceOf[LanguageUtils]

  val englishLang: Lang = Lang("en")
  val welshLang: Lang = Lang("cy")

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val langs: Langs = app.injector.instanceOf[Langs]

  val fakeMessages: Map[String, Map[String, String]] = Map(
    "en" -> Map(
      "glossary.contents.alpha.key" -> "Alpha key one",
      "glossary.contents.alpha.value" -> "Alpha value two",
      "glossary.contents.beta.key" -> "Beta key three",
      "glossary.contents.beta.value" -> "Beta value four",
      "glossary.contents.gamma.key" -> "Gamma key one",
      "glossary.contents.gamma.value" -> "Gamma value two",
      "glossary.contents.delta.key" -> "Delta key three",
      "glossary.contents.delta.value" -> "Delta value four"
    ),
    "cy" -> Map(
      "glossary.contents.alpha.key" -> "Epsilon key one",
      "glossary.contents.alpha.value" -> "Epsilon value two",
      "glossary.contents.beta.key" -> "Zeta key three",
      "glossary.contents.beta.value" -> "Zeta value four",
      "glossary.contents.gamma.key" -> "Eta key one",
      "glossary.contents.gamma.value" -> "Eta value two",
      "glossary.contents.delta.key" -> "Theta key three",
      "glossary.contents.delta.value" -> "Theta value four"
    )
  )

  val fullEnglishGlossaryList: List[(String, List[(String, String)])] = List(
    "A" -> List("Alpha key one" -> "Alpha value two"),
    "B" -> List("Beta key three" -> "Beta value four"),
    "D" -> List("Delta key three" -> "Delta value four"),
    "G" -> List("Gamma key one" -> "Gamma value two")
  )

  val fullWelshGlossaryList: List[(String, List[(String, String)])] = List(
    "E" -> List("Epsilon key one" -> "Epsilon value two", "Eta key one" -> "Eta value two"),
    "T" -> List("Theta key three" -> "Theta value four"),
    "Z" -> List("Zeta key three" -> "Zeta value four"),
  )

  class Setup {

    val mockMessagesApi: MessagesApi = mock[MessagesApi]

    val mockEnglishMessages: Messages = mock[Messages]
    val mockWelshMessages: Messages = mock[Messages]

    when(mockMessagesApi.messages) thenReturn fakeMessages

    fakeMessages("en") map { case (key, value) =>
      when(mockEnglishMessages(key)) thenReturn value
    }

    fakeMessages("cy") map { case (key, value) =>
      when(mockWelshMessages(key)) thenReturn value
    }

    when(mockMessagesApi.preferred(Seq(englishLang))) thenReturn mockEnglishMessages
    when(mockMessagesApi.preferred(Seq(welshLang))) thenReturn mockWelshMessages

    when(mockMessagesApi(ArgumentMatchers.eq("glossary.last-changed"))(ArgumentMatchers.any))
      .thenReturn("2022-02-03")

    val service: GlossaryService = new GlossaryService(
      mockMessagesApi,
      langs,
      languageUtils
    )

  }

  "GlossaryService" should {
    "throw a GlossaryException" when {
      "english is not available in the language mappings" in {
        intercept[GlossaryException](
          new GlossaryService(
            stubMessagesApi(),
            stubLangs(Seq(welshLang)),
            languageUtils
          )
        ).getMessage mustBe "No English glossary found, only: cy"
      }
    }
  }

  "getGlossaryList" should {
    "return the full letter, word and description mappings" when {
      "in english" in new Setup {
        service.getGlossaryList(englishLang) mustBe fullEnglishGlossaryList
      }
      "in welsh" in new Setup {
        service.getGlossaryList(welshLang) mustBe fullWelshGlossaryList
      }
    }
  }

  "getFilteredGlossaryList" should {
    "return the letter, word and description mappings which contain the provided search term" when {
      "the search term is not provided" in new Setup {
        service.getFilteredGlossaryList(GlossaryFormModel(searchTerm = None))(englishLang) mustBe service.getGlossaryList(englishLang)
      }
      "the search terms provided and matches against words" in new Setup {
        service.getFilteredGlossaryList(GlossaryFormModel(searchTerm = Some("one")))(englishLang) mustBe List(
          "A" -> List("Alpha key one" -> "Alpha value two"),
          "G" -> List("Gamma key one" -> "Gamma value two")
        )
      }
      "the search terms provided and matches against descriptions" in new Setup {
        service.getFilteredGlossaryList(GlossaryFormModel(searchTerm = Some("two")))(englishLang) mustBe List(
          "A" -> List("Alpha key one" -> "Alpha value two"),
          "G" -> List("Gamma key one" -> "Gamma value two")
        )
      }
      "the search terms provided match against both words and descriptions" in new Setup {
        service.getFilteredGlossaryList(GlossaryFormModel(searchTerm = Some("Alpha")))(englishLang) mustBe List(
          "A" -> List("Alpha key one" -> "Alpha value two")
        )
      }
      "the search terms provided do not match against anything" in new Setup {
        service.getFilteredGlossaryList(GlossaryFormModel(searchTerm = Some("unknown")))(englishLang) mustBe List.empty
      }
    }
  }

  "getLastChangedString" should {
    "return a formatted date sources from messages" when {
      "in english" in new Setup {
        service.getLastChangedString(englishLang, messagesApi.preferred(Seq(englishLang))) mustBe "3 February 2022"
      }
      "in welsh" in new Setup {
        service.getLastChangedString(welshLang, messagesApi.preferred(Seq(welshLang))) mustBe "3 Chwefror 2022"
      }
    }
  }

}
