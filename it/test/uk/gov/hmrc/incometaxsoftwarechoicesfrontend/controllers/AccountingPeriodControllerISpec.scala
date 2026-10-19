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
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{await, defaultAwaitTimeout}
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

        testView(
          res,
          None,
          "individual",
          Seq(None, None, None)
        )
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

          testView(
            res,
            Some(RecognisedSoftwareProduct.name),
            "individual",
            Seq(Some(SixthAprilToFifthApril), None, None)
          )
        }
        "was the 1st April to 31st March option for an agent user in the check journey" in {
          val userAnswers = UserAnswers()
            .set(EnterSoftwareNamePage, RecognisedSoftwareProduct).get
            .set(AccountingPeriodPage, Seq(FirstAprilToThirtyFirstMarch)).get
            .set(UserTypePage, UserType.Agent).get
          setupAnswers(SessionId, Some(userAnswers))

          val res = SoftwareChoicesFrontend.getAccountingPeriod

          res should have(httpStatus(OK))

          testView(
            res,
            Some(RecognisedSoftwareProduct.name),
            "agent",
            Seq(None, Some(FirstAprilToThirtyFirstMarch), None)
          )
        }
        "was the other accounting period option for an individual user in the find journey" in {
          val userAnswers = UserAnswers()
            .set(AccountingPeriodPage, Seq(OtherAccountingPeriod)).get
            .set(UserTypePage, SoleTraderOrLandlord).get
          setupAnswers(SessionId, Some(userAnswers))

          val res = SoftwareChoicesFrontend.getAccountingPeriod

          res should have(httpStatus(OK))

          testView(
            res,
            None,
            "individual",
            Seq(None, None, Some(OtherAccountingPeriod))
          )
        }
        "were multiple options for an agent user in the find journey" in {
          val userAnswers = UserAnswers()
            .set(AccountingPeriodPage, Seq(SixthAprilToFifthApril, FirstAprilToThirtyFirstMarch)).get
            .set(UserTypePage, UserType.Agent).get
          setupAnswers(SessionId, Some(userAnswers))

          val res = SoftwareChoicesFrontend.getAccountingPeriod

          res should have(httpStatus(OK))

          testView(
            res,
            None,
            "agent",
            Seq(Some(SixthAprilToFifthApril), Some(FirstAprilToThirtyFirstMarch), None)
          )
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
          pageTitle(s"Error: ${messages("accounting-period.heading")} - ${PageContentBase.title} - GOV.UK")
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

  def testView(response: WSResponse,
               softwareNameCaption: Option[String],
               userType: String,
               checkboxes: Seq[Option[AccountingPeriod]]): Unit = {

    val body = Jsoup.parse(response.body)

    body.title() shouldBe s"${messages("accounting-period.heading")} - ${PageContentBase.title} - GOV.UK"

    softwareNameCaption match {
      case Some(caption) => body.select("span.govuk-caption-l").first().text() shouldBe caption
      case None => body.select("span.govuk-caption-l").isEmpty shouldBe true
    }

    body.select("h1").first().text() shouldBe messages("accounting-period.heading")

    val linkText = messages(s"accounting-period.para-one.link-text.$userType") + " (opens in new tab)"
    body.select("p").get(1).text() shouldBe messages(s"accounting-period.para-one.$userType", linkText)
    val link = body.select(".govuk-link").get(2)
    link.text() shouldBe linkText
    link.attr("href") shouldBe messages(s"accounting-period.para-one.link-href")

    body.select("p").get(2).text() shouldBe messages(s"accounting-period.para-two.$userType")

    body.select("h2").first().text() shouldBe messages(s"accounting-period.sub-heading.$userType")

    for (i <- checkboxes.indices) {
      val textIndex = i match {
        case 0 => ""
        case 1 => "-2"
        case 2 => "-3"
      }
      checkboxes(i) match {
        case Some(period) => checkboxSelected(s"accounting-period$textIndex", Some(period.key))
        case None => checkboxSelected(s"accounting-period$textIndex", None)
      }
    }
  }

}
