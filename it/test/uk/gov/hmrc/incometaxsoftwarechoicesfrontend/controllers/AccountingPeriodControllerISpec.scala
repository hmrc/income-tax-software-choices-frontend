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

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.AccountingPeriodContent.subHeading
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.AccountingPeriodForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod.{FirstAprilToThirtyFirstMarch, OtherAccountingPeriod, SixthAprilToFifthApril}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.Recognised
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.SoleTraderOrLandlord
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{AccountingPeriod, SoftwareProduct, UserAnswers, UserType}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.{AccountingPeriodPage, EnterSoftwareNamePage, UserTypePage}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class AccountingPeriodControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  private val RecognisedSoftwareProduct = SoftwareProduct(0, "Bright", Recognised)

  s"GET ${routes.AccountingPeriodController.show().url}" should {
    "redirect to the service index" when {
      "there is nothing saved in the database for this user" in {
        val res = SoftwareChoicesFrontend.getAccountingPeriod

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "display the page" when {
      "the accounting period question has not been answered previously and the user type has not been specified" in {
        setupAnswers(SessionId, None)

        val res = SoftwareChoicesFrontend.getAccountingPeriod

        res should have(httpStatus(OK))

        testView(res)(None, false, Seq(false, false, false))
      }
      "the accounting period question has been answered previously" which {
        "was the 6th April to 5th April option for an individual user in the check journey" in {
          val userAnswers = UserAnswers()
            .set(EnterSoftwareNamePage, RecognisedSoftwareProduct).get
            .set(AccountingPeriodPage, Seq(SixthAprilToFifthApril)).get
            .set(UserTypePage, SoleTraderOrLandlord).get
          setupAnswers(SessionId, Some(userAnswers))

          val res = SoftwareChoicesFrontend.getAccountingPeriod

          res should have(httpStatus(OK))

          testView(res)(Some(RecognisedSoftwareProduct.name), false, Seq(true, false, false))
        }
        "was the 1st April to 31st March option for an agent user in the check journey" in {
          val userAnswers = UserAnswers()
            .set(EnterSoftwareNamePage, RecognisedSoftwareProduct).get
            .set(AccountingPeriodPage, Seq(FirstAprilToThirtyFirstMarch)).get
            .set(UserTypePage, UserType.Agent).get
          setupAnswers(SessionId, Some(userAnswers))

          val res = SoftwareChoicesFrontend.getAccountingPeriod

          res should have(httpStatus(OK))

          testView(res)(Some(RecognisedSoftwareProduct.name), true, Seq(false, true, false))
        }
        "was the other accounting period option for an individual user in the find journey" in {
          val userAnswers = UserAnswers()
            .set(AccountingPeriodPage, Seq(OtherAccountingPeriod)).get
            .set(UserTypePage, SoleTraderOrLandlord).get
          setupAnswers(SessionId, Some(userAnswers))

          val res = SoftwareChoicesFrontend.getAccountingPeriod

          res should have(httpStatus(OK))

          testView(res)(None, false, Seq(false, false, true))
        }
        "were multiple options for an agent user in the find journey" in {
          val userAnswers = UserAnswers()
            .set(AccountingPeriodPage, Seq(SixthAprilToFifthApril, FirstAprilToThirtyFirstMarch)).get
            .set(UserTypePage, UserType.Agent).get
          setupAnswers(SessionId, Some(userAnswers))

          val res = SoftwareChoicesFrontend.getAccountingPeriod

          res should have(httpStatus(OK))

          testView(res)(None, true, Seq(true, true, false))
        }
      }
    }
  }

  s"POST ${routes.AccountingPeriodController.submit().url}" when {
    "there is nothing saved in the database for this user" should {
      "redirect to the service index" in {
        val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(Seq(SixthAprilToFifthApril.key)))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "not in edit mode" should {
      "redirect to the check your answers page" when {
        "the user has selected the 6th April to 5th April checkbox" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(Seq(SixthAprilToFifthApril.key)))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, AccountingPeriodPage) shouldBe Some(Seq(SixthAprilToFifthApril))
        }
        "the user has selected the 1st April to 31st March checkbox" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(Seq(FirstAprilToThirtyFirstMarch.key)))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, AccountingPeriodPage) shouldBe Some(Seq(FirstAprilToThirtyFirstMarch))
        }
        "the user has selected multiple checkboxes" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(Seq(SixthAprilToFifthApril.key, FirstAprilToThirtyFirstMarch.key)))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, AccountingPeriodPage) shouldBe Some(Seq(SixthAprilToFifthApril, FirstAprilToThirtyFirstMarch))
        }
      }
      "redirect to the accounting period not aligned page" when {
        "the user selected the different accounting period checkbox" in {
          setupAnswers(SessionId, None)

          val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(Seq(OtherAccountingPeriod.key)))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.AccountingPeriodNotAlignedController.show().url)
          )

          getPageData(SessionId, AccountingPeriodPage) shouldBe Some(Seq(OtherAccountingPeriod))
        }
      }
    }
    "in edit mode" should {
      "redirect to the check your answers page" when {
        "the user has selected the 6th April to 5th April checkbox" in {
          setPageData(SessionId, AccountingPeriodPage, Seq(FirstAprilToThirtyFirstMarch))

          val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(Seq(SixthAprilToFifthApril.key)))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, AccountingPeriodPage) shouldBe Some(Seq(SixthAprilToFifthApril))
        }
        "the user has selected the 1st April to 31st March checkbox" in {
          setPageData(SessionId, AccountingPeriodPage, Seq(OtherAccountingPeriod))

          val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(Seq(FirstAprilToThirtyFirstMarch.key)))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, AccountingPeriodPage) shouldBe Some(Seq(FirstAprilToThirtyFirstMarch))
        }
      }
      "redirect to the accounting period not aligned page" when {
        "the user selected the different accounting period checkbox" in {
          setPageData(SessionId, AccountingPeriodPage, Seq(SixthAprilToFifthApril))

          val res = SoftwareChoicesFrontend.submitAccountingPeriod(Some(Seq(OtherAccountingPeriod.key)))

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.AccountingPeriodNotAlignedController.show().url)
          )

          getPageData(SessionId, AccountingPeriodPage) shouldBe Some(Seq(OtherAccountingPeriod))
        }
      }
    }
    "no checkbox has been selected" should {
      "return a bad request" in {
        setupAnswers(SessionId, None)

        val res = SoftwareChoicesFrontend.submitAccountingPeriod(None)

        res should have(
          httpStatus(BAD_REQUEST),
          pageTitle(AccountingPeriodContent.errorTitle)
        )
      }
    }
  }

  "backUrl" must {
    "return to additional income page when not in edit mode" in {
      accountingPeriodController.backUrl(editMode = false) shouldBe routes.OtherItemsController.show().url
    }
    "return to check your answers when in edit mode" in {
      accountingPeriodController.backUrl(editMode = true) shouldBe routes.CheckYourAnswersController.show().url
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()

    await(userFiltersRepository.collection.drop().toFuture())
  }

  lazy val accountingPeriodController: AccountingPeriodController = app.injector.instanceOf[AccountingPeriodController]

  def testView(response: WSResponse)
              (softwareNameCaption: Option[String],
               isAgent: Boolean,
               checkedStatuses: Seq[Boolean]): Unit = {

    import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.AccountingPeriodContent.*

    val body = Jsoup.parse(response.body)

    body.title() shouldBe title

    val main = body.mainContent

    softwareNameCaption match {
      case Some(caption) => main.selectHead("span.govuk-caption-l").text() shouldBe caption
      case None => main.select("span.govuk-caption-l").isEmpty shouldBe true
    }

    main.selectHead("h1").text() shouldBe heading

    main.selectHead("p").text() shouldBe paraOne(isAgent)

    val link =  main.selectHead(".govuk-link")
    link.text() shouldBe linkText
    link.attr("href") shouldBe linkHref

    main.select("p").get(1).text() shouldBe paraTwo(isAgent)

    main.selectHead("h2").text() shouldBe subHeading(isAgent)

    val form = main.selectHead("form")

    form.attr("method") shouldBe "POST"
    form.attr("action") shouldBe routes.AccountingPeriodController.show().url

    val fieldSet = form.selectHead("fieldset")
    val hint = fieldSet.selectHead(".govuk-hint")
    hint.text shouldBe AccountingPeriodContent.hint
    fieldSet.attr("aria-describedby") should include(hint.attr("id"))

    validateCheckboxes(form)(isAgent, checkedStatuses)

    form.selectNth(".govuk-button", 1).text() shouldBe AccountingPeriodContent.continue

  }

  def validateCheckboxes(form: Element)(isAgent: Boolean, checkedStatuses: Seq[Boolean]): Unit = {
    import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.AccountingPeriodContent.AccountingPeriodCheckboxes.*

    val accountingPeriods = Seq(SixthToFifthBox, FirstToThirtyFirstBox, OtherBox)

    for (i <- checkedStatuses.indices) {
      form.mustHaveCheckbox("fieldSet")(
        checkbox = i + 1,
        legend = subHeading(isAgent),
        isHeading = false,
        isLegendHidden = true,
        name = s"${AccountingPeriodForm.formKey}[]",
        label = accountingPeriods(i).label,
        value = accountingPeriods(i).value,
        checked = checkedStatuses(i)
      )
    }
  }

}

private object AccountingPeriodContent {
  val heading = "Accounting period"
  val title = s"$heading - ${PageContentBase.title} - GOV.UK"
  val errorTitle = s"Error: $title"
  val paraOneIndividual = "If your accounting period is 1 April to 31 March, you’ll need software that supports calendar update periods (opens in new tab). This will make keeping records simpler."
  val paraOneAgent = "If your client’s accounting period is 1 April to 31 March, you or your client will need to choose software that supports calendar update periods (opens in new tab). This will make keeping records simpler."
  val paraTwoIndividual = "If you plan to change your accounting period, include your current accounting period and any you plan to use in the future."
  val paraTwoAgent = "If your client’s accounting period is expected to change, include their current accounting period and any they might use in the future."
  val linkText = "calendar update periods (opens in new tab)"
  val linkHref = "https://www.gov.uk/guidance/use-making-tax-digital-for-income-tax/send-quarterly-updates#using-calendar-update-periods"
  val subHeadingIndividual = "What accounting period do you use?"
  val subHeadingAgent = "What accounting period does your client use?"
  val hint = "Select all that apply"
  val continue = "Continue"
  val error = "Select an accounting period"

  sealed trait AccountingPeriodCheckboxes {
    val label: String
    val value: String
  }

  object AccountingPeriodCheckboxes {
    case object SixthToFifthBox extends AccountingPeriodCheckboxes {
      override val label = "6 April to 5 April"
      override val value = "sixth-april-to-fifth-april"
    }

    case object FirstToThirtyFirstBox extends AccountingPeriodCheckboxes {
      override val label = "1 April to 31 March"
      override val value = "first-april-to-thirty-first-march"
    }

    case object OtherBox extends AccountingPeriodCheckboxes {
      override val label = "A different accounting period"
      override val value = "other"
    }
  }

  def paraOne(isAgent: Boolean): String = {
    if (isAgent) paraOneAgent else paraOneIndividual
  }

  def paraTwo(isAgent: Boolean): String = {
    if (isAgent) paraTwoAgent else paraTwoIndividual
  }

  def subHeading(isAgent: Boolean): String = {
    if (isAgent) subHeadingAgent else subHeadingIndividual
  }
}
