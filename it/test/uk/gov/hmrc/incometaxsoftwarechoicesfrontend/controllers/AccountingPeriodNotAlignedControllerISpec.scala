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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers

import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareProduct, UserAnswers, UserType}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.Recognised
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.SoleTraderOrLandlord
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.{EnterSoftwareNamePage, UserTypePage}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import play.api.libs.ws.WSResponse



class AccountingPeriodNotAlignedControllerISpec
  extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {
  
  private val RecognisedSoftwareProduct = SoftwareProduct(0, "Bright", Recognised)
  
  s"GET ${routes.AccountingPeriodNotAlignedController.show().url}" should {
    "redirect to the service index" when {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.getAccountingPeriodNotAligned

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }

    "display the page correctly for individuals" in {
      val userAnswers = UserAnswers()
        .set(EnterSoftwareNamePage, RecognisedSoftwareProduct).get
        .set(UserTypePage, SoleTraderOrLandlord).get

      setupAnswers(SessionId, Some(userAnswers))

      val res = SoftwareChoicesFrontend.getAccountingPeriodNotAligned

      res should have(httpStatus(OK))

      testView(res)("individual")
    }

    "display the page correctly for agents" in {
      val userAnswers = UserAnswers()
        .set(EnterSoftwareNamePage, RecognisedSoftwareProduct).get
        .set(UserTypePage, UserType.Agent).get

      setupAnswers(SessionId, Some(userAnswers))

      val res = SoftwareChoicesFrontend.getAccountingPeriodNotAligned

      res should have(httpStatus(OK))

      testView(res)("agent")
    }

     "display the page correctly when user type is undefined" in {
         val userAnswers = UserAnswers()
           .set(EnterSoftwareNamePage, RecognisedSoftwareProduct).get

         setupAnswers(SessionId, Some(userAnswers))

         val res = SoftwareChoicesFrontend.getAccountingPeriodNotAligned

         res should have(httpStatus(OK))

         testView(res)("indivdual")
    }
    "have correct back link when not in edit mode" in {
      setupAnswers(SessionId, Some(UserAnswers()))

      val res = SoftwareChoicesFrontend.getAccountingPeriodNotAligned

      res.body.contains("/accounting-period-check?editMode=true") shouldBe false
    }
    "have correct back link in edit mode" in {
      setupAnswers(SessionId, Some(UserAnswers()))

      val res = SoftwareChoicesFrontend.getAccountingPeriodNotAlignedEditMode

      res.body.contains("/accounting-period-check?editMode=true") shouldBe true
    }
  }

  s"POST ${routes.AccountingPeriodNotAlignedController.submit().url}" should {
    "redirect to the service index" when {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.postAccountingPeriodNotAligned

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "redirect to software results page" in {
      setupAnswers(SessionId, Some(UserAnswers()))

      val res = SoftwareChoicesFrontend.postAccountingPeriodNotAligned

      res should have(
        httpStatus(SEE_OTHER),
        redirectURI(routes.CheckYourAnswersController.show().url)
      )
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(userFiltersRepository.collection.drop().toFuture())
  }

  private def testView(response: WSResponse)(userType: String): Unit = {

    import AccountingPeriodNotAlignedViewContent.*

    val document = Jsoup.parse(response.body)
    val main = document.mainContent

    document.title() shouldBe title(userType)

    main.selectHead("h1").text() shouldBe heading(userType)
    main.selectHead("h2").text() shouldBe subHeading(userType)

    main.selectHead("span.govuk-caption-l").text() shouldBe RecognisedSoftwareProduct.name

    main.selectNth("p", 1).text() shouldBe paraOne(userType)
    main.selectNth("p", 2).text() shouldBe paraTwo(userType)
    main.selectNth("p", 3).text() shouldBe paraThree
    main.selectNth("p", 4).text() shouldBe paraFour(userType)

    val link = main.selectHead("a.govuk-link")

    link.text() shouldBe linkText(userType)
    link.attr("href") shouldBe englishLinkUrl

    val form = main.selectHead("form")

    form.attr("method") shouldBe "POST"
    form.attr("action") shouldBe routes.AccountingPeriodNotAlignedController.submit().url

    form.selectHead(".govuk-button").text() shouldBe continue
  }

  private def testLanguageLink(response: WSResponse, expectedUrl: String): Unit = {
    val document = Jsoup.parse(response.body)

    val link = document.selectHead("a.govuk-link")

    link.attr("href") shouldBe expectedUrl
  }

  private def testWelshLink(response: WSResponse)(userType: String): Unit = {
    import AccountingPeriodNotAlignedViewContent.*

    val document = Jsoup.parse(response.body)
    val link = document.select("main p").get(1).select("a.govuk-link")

    link.text() shouldBe welshLinkText(userType)
    link.attr("href") shouldBe welshLinkUrl
  }
}

private object AccountingPeriodNotAlignedViewContent {

  val headingIndividual = "You’ll need to adjust your income figures before reporting them to HMRC"
  val headingAgent = "Your client’s income figures will need to be adjusted before they are reported to HMRC"

  val titleIndividual = s"$headingIndividual - ${PageContentBase.title} - GOV.UK"
  val titleAgent = s"$headingAgent - ${PageContentBase.title} - GOV.UK"

  val subHeadingIndividual = "Reporting your income to HMRC with a non-aligned accounting period"
  val subHeadingAgent = "Reporting income to HMRC for a non-aligned accounting period"

  val paraOneIndividual = "Your accounting period does not end on 5 April or 31 March. This means it is not aligned with the tax year, so you have a non-aligned accounting period."
  val paraOneAgent = "Your client’s accounting period does not end on 5 April or 31 March. This means it is not aligned with the tax year, so they have a non-aligned accounting period."

  val paraTwoIndividual = "At the end of the tax year, you’ll need to adjust the income figures shown in your chosen software (opens in new tab). This is so they align with the tax year before you report your property or self-employment income to HMRC."
  val paraTwoAgent = "At the end of the tax year, you or your client will need to adjust the income figures shown in the chosen software (opens in new tab). This is so they align with the tax year before reporting property or self-employment income to HMRC."

  val paraThree = "HMRC will publish guidance on how to do this in due course."

  val paraFourIndividual = "You can continue to choose your software."
  val paraFourAgent = "You can continue to choose software."

  val englishLinkUrl =
    "https://www.gov.uk/guidance/use-making-tax-digital-for-income-tax/adjust-your-self-employment-and-property-income"

  val welshLinkUrl =
    "https://www.gov.uk/guidance/defnyddio-r-cynllun-troi-treth-yn-ddigidol-ar-gyfer-treth-incwm/addasu-eich-incwm-o-hunangyflogaeth-ac-eiddo"

  val linkTextIndividual =
    "adjust the income figures shown in your chosen software (opens in new tab)"

  val linkTextAgent =
    "adjust the income figures shown in the chosen software (opens in new tab)"

  val welshLinkTextIndividual =
    "addasu’r ffigurau incwm a ddangosir yn eich meddalwedd (yn agor tab newydd)"

  val welshLinkTextAgent =
    "addasu’r ffigurau incwm a ddangosir yn y feddalwedd a ddewiswyd (yn agor tab newydd)"

  val continue = "Continue"

  def heading(userType: String): String =
    if (userType == "agent") headingAgent else headingIndividual

  def title(userType: String): String =
    if (userType == "agent") titleAgent else titleIndividual

  def subHeading(userType: String): String =
    if (userType == "agent") subHeadingAgent else subHeadingIndividual

  def paraOne(userType: String): String =
    if (userType == "agent") paraOneAgent else paraOneIndividual

  def paraTwo(userType: String): String =
    if (userType == "agent") paraTwoAgent else paraTwoIndividual

  def paraFour(userType: String): String =
    if (userType == "agent") paraFourAgent else paraFourIndividual

  def linkText(userType: String): String =
    if (userType == "agent") linkTextAgent else linkTextIndividual

  def welshLinkText(userType: String): String =
    if (userType == "agent") welshLinkTextAgent else welshLinkTextIndividual
}
