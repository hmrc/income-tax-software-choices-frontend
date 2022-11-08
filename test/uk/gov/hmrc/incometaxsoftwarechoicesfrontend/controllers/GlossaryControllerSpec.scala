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
import play.api.data.Form
import play.api.i18n.Lang
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.GlossaryForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.GlossaryFormModel
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.GlossaryService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.{GlossaryPage, GlossaryPageList}
import uk.gov.hmrc.play.language.LanguageUtils

import scala.concurrent.Future

class GlossaryControllerSpec extends ControllerBaseSpec with FeatureSwitching with BeforeAndAfterEach with MockitoSugar {

  private val mcc = app.injector.instanceOf[MessagesControllerComponents]
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val languageUtils: LanguageUtils = app.injector.instanceOf[LanguageUtils]

  private val EnglishCode = "en"
  private val English = Lang(EnglishCode)

  private val WelshCode = "cy"
  private val Welsh = Lang(WelshCode)

  val testGlossaryList: List[(String, List[(String, String)])] = List(
    "en" -> List(
      "english key" -> "english value"
    ),
    "cy" -> List(
      "welsh key" -> "welsh value"
    )
  )

  val testFormattedLastChangedString: String = "3 February 2022"

  "show" when {
    "invoked with no language" should {
      s"return OK ($OK) with the page content from an english source" in new Setup {
        when(mockGlossaryService.getGlossaryList(ArgumentMatchers.eq(English))).thenReturn(testGlossaryList)
        when(mockGlossaryService.getLastChangedString(ArgumentMatchers.eq(English), ArgumentMatchers.any())).thenReturn(testFormattedLastChangedString)
        when(mockGlossaryPage.apply(
          ArgumentMatchers.eq(testGlossaryList),
          ArgumentMatchers.eq(GlossaryController.glossaryMaxLabelsWithoutLinks),
          ArgumentMatchers.eq(testFormattedLastChangedString),
          ArgumentMatchers.any[Form[GlossaryFormModel]](),
          ArgumentMatchers.eq(routes.GlossaryController.search(ajax = false))
        )(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(HtmlFormat.empty)

        val result: Future[Result] = controller.show(fakeRequest)

        status(result) shouldBe OK
        contentType(result) shouldBe Some(HTML)
      }
    }
    "invoked with a welsh language request" should {
      s"return OK ($OK) with the page content from a welsh source" in new Setup {
        when(mockGlossaryService.getGlossaryList(ArgumentMatchers.eq(Welsh))).thenReturn(testGlossaryList)
        when(mockGlossaryService.getLastChangedString(ArgumentMatchers.eq(Welsh), ArgumentMatchers.any())).thenReturn(testFormattedLastChangedString)
        when(mockGlossaryPage.apply(
          ArgumentMatchers.eq(testGlossaryList),
          ArgumentMatchers.eq(GlossaryController.glossaryMaxLabelsWithoutLinks),
          ArgumentMatchers.eq(testFormattedLastChangedString),
          ArgumentMatchers.any[Form[GlossaryFormModel]](),
          ArgumentMatchers.eq(routes.GlossaryController.search(ajax = false))
        )(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(HtmlFormat.empty)

        val result: Future[Result] = controller.show(fakeRequest.withTransientLang(Welsh))

        status(result) shouldBe OK
        contentType(result) shouldBe Some(HTML)
      }
    }
  }

  "search" when {
    "ajax = false" should {
      "return the glossary page filtered by the search term requested" in new Setup {
        val glossarySearchModel: GlossaryFormModel = GlossaryFormModel(searchTerm = Some("search"))
        val glossaryListResult: List[(String, List[(String, String)])] = List("S" -> List("Search word" -> "Search description"))

        when(mockGlossaryService.getFilteredGlossaryList(ArgumentMatchers.eq(glossarySearchModel))(ArgumentMatchers.eq(English)))
          .thenReturn(glossaryListResult)
        when(mockGlossaryService.getLastChangedString(ArgumentMatchers.eq(English), ArgumentMatchers.any()))
          .thenReturn(testFormattedLastChangedString)
        when(mockGlossaryPage(
          ArgumentMatchers.eq(glossaryListResult),
          ArgumentMatchers.eq(GlossaryController.glossaryMaxLabelsWithoutLinks),
          ArgumentMatchers.eq(testFormattedLastChangedString),
          ArgumentMatchers.any[Form[GlossaryFormModel]](),
          ArgumentMatchers.eq(routes.GlossaryController.search(ajax = false))
        )(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(HtmlFormat.empty)

        controller.search(ajax = false)(FakeRequest("POST", "/").withFormUrlEncodedBody(GlossaryForm.searchTerm -> "search"))
      }
    }
    "ajax = true" should {
      "return the glossary list section of the page filtered by the search term requested" in new Setup {
        val glossarySearchModel: GlossaryFormModel = GlossaryFormModel(searchTerm = Some("search"))
        val glossaryListResult: List[(String, List[(String, String)])] = List("S" -> List("Search word" -> "Search description"))

        when(mockGlossaryService.getFilteredGlossaryList(ArgumentMatchers.eq(glossarySearchModel))(ArgumentMatchers.eq(English)))
          .thenReturn(glossaryListResult)
        when(mockGlossaryPageList(
          ArgumentMatchers.eq(glossaryListResult),
          ArgumentMatchers.eq(GlossaryController.glossaryMaxLabelsWithoutLinks),
          ArgumentMatchers.eq(true)
        )(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(HtmlFormat.empty)

        controller.search(ajax = true)(FakeRequest("POST", "/").withFormUrlEncodedBody(GlossaryForm.searchTerm -> "search"))
      }
    }
  }

  trait Setup {
    val mockGlossaryPage: GlossaryPage = mock[GlossaryPage]
    val mockGlossaryPageList: GlossaryPageList = mock[GlossaryPageList]
    val mockGlossaryService: GlossaryService = mock[GlossaryService]

    lazy val controller = new GlossaryController(
      mcc,
      appConfig,
      mockGlossaryPage,
      mockGlossaryPageList,
      mockGlossaryService
    )
  }

}
