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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers

import org.mockito.{ArgumentMatchers, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.i18n.{Lang, Langs, Messages, MessagesApi}
import play.api.mvc.{Cookie, MessagesControllerComponents}
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.BetaFeatures
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.GlossaryPage
import uk.gov.hmrc.play.language.LanguageUtils

class GlossaryControllerSpec extends ControllerBaseSpec with FeatureSwitching with BeforeAndAfterEach with MockitoSugar {

  private val mcc = app.injector.instanceOf[MessagesControllerComponents]
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val languageUtils: LanguageUtils = app.injector.instanceOf[LanguageUtils]
  private val glossaryPage = app.injector.instanceOf[GlossaryPage]

  protected override def beforeEach(): Unit = {
    disable(BetaFeatures)
  }

  private val softwareChoicesInWelsh = "Dewisiadau Meddalwedd"
  private val softwareChoicesInEnglish = "Software Choices"

  private val FrenchCode = "fr"
  private val French = Lang(FrenchCode)

  private val EnglishCode = "en"
  private val English = Lang(EnglishCode)

  "Show" when {
    "invoked with no language" in withController { controller =>
      val result = controller.show(true)(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "invoked with unknown language (French) results in default language" in withController { controller =>
      // NB This is actually handled by Play - it will give you the default language, which will be English.
      val frenchRequest = fakeRequest.withCookies(Cookie("PLAY_LANG", FrenchCode))
      val result = controller.show(true)(frenchRequest)
      status(result) shouldBe Status.OK
      contentAsString(result).indexOf(softwareChoicesInWelsh) > 0 shouldBe false
      contentAsString(result).indexOf(softwareChoicesInEnglish) > 0 shouldBe true
    }

    "invoked with welsh language" in withController { controller =>
      val result = controller.show(true)(fakeRequest.withTransientLang(Lang("cy")))
      status(result) shouldBe Status.OK
      contentAsString(result).indexOf(softwareChoicesInWelsh) > 0 shouldBe true
      contentAsString(result).indexOf(softwareChoicesInEnglish) > 0 shouldBe false
    }
    "built with no default (english) language" should {
      "throw a glossary exception" in {
        val mccWithNoEnglish: MessagesControllerComponents = getMockMccWithLangs(Seq(French))
        try {
          new GlossaryController(
            mccWithNoEnglish,
            appConfig,
            glossaryPage,
            languageUtils)
          fail("Expected glossary exception")
        } catch {
          case e: GlossaryException => e.getMessage shouldBe "No English glossary found, only: fr"
        }
      }
    }
    "no last changed date language" should {
      "have an empty string for last changed" in {
        val mccWithNoLastChangedMessage: MessagesControllerComponents = getMockMccWithNoLastChangedContent()
        implicit val lang = English
        implicit val messages: Messages = mock[Messages]
        new GlossaryController(
          mccWithNoLastChangedMessage,
          appConfig,
          glossaryPage,
          languageUtils).getLastChangedString should be ("")

      }
    }
  }

  private def getMockMccWithLangs(langsSeq: Seq[Lang]) = {
    val langs = mock[Langs]
    when(langs.availables).thenReturn(langsSeq)
    val mccWithLangs = mock[MessagesControllerComponents]
    val messagesApi = mock[MessagesApi]
    when(mccWithLangs.messagesApi).thenReturn(messagesApi)
    when(messagesApi.messages).thenReturn(Map.empty)
    when(mccWithLangs.langs).thenReturn(langs)
    mccWithLangs
  }

  private def getMockMccWithNoLastChangedContent() = {
    val langs = mock[Langs]
    when(langs.availables).thenReturn(Seq(English))
    val mccWithLangs = mock[MessagesControllerComponents]
    val messagesApi = mock[MessagesApi]
    when(mccWithLangs.messagesApi).thenReturn(messagesApi)
    when(messagesApi.translate(ArgumentMatchers.eq("glossary.last-changed"), ArgumentMatchers.eq(Seq.empty))(ArgumentMatchers.any())).thenReturn(None)
    when(messagesApi.messages).thenReturn(Map.empty)
    when(mccWithLangs.langs).thenReturn(langs)
    mccWithLangs
  }

  private def withController(testCode: GlossaryController => Any): Unit = {
    val controller = new GlossaryController(
      mcc,
      appConfig,
      glossaryPage,
      languageUtils
    )
    testCode(controller)
  }

}
