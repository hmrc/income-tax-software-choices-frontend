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
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.{Agent, SoleTraderOrLandlord}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class UserTypeControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper with FeatureSwitching {

  def testUserFilters(answers: UserAnswers): UserFilters = UserFilters(SessionId, Some(answers))

  override def beforeEach(): Unit = {
    await(userFiltersRepository.collection.drop().toFuture())
    super.beforeEach()
  }

  "GET /how-will-you-use-it" when {
    "there is nothing saved in the database for this user" should {
      "redirect to the index page" in {
        val res = SoftwareChoicesFrontend.getUserType()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }

    "there are pre-existing page answers with radio option selected" should {
      "display the page with the previously chosen radio checked and the guidance page as the back link" in {
        val userAnswers = UserAnswers().set(UserTypePage, SoleTraderOrLandlord).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getUserType()

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("type-of-user.heading")} - ${PageContentBase.title} - GOV.UK"),
          radioButtonSelected(id = "type-of-user", selectedRadioButton = Some(SoleTraderOrLandlord.key)),
          elementExists(s""".govuk-back-link[href="${appConfig.guidance}"]""", true)
        )
      }
    }

    "there are pre-existing page answers in edit mode" should {
      "display the page with the previously chosen radio checked and the check your answers page as the back link" in {
        val userAnswers = UserAnswers().set(UserTypePage, Agent).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getUserType(editMode = true)

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("type-of-user.heading")} - ${PageContentBase.title} - GOV.UK"),
          radioButtonSelected(id = "type-of-user-2", selectedRadioButton = Some(Agent.key)),
          elementExists(s""".govuk-back-link[href="${routes.CheckYourAnswersController.show().url}"]""", true)
        )
      }
    }

    "the user is in the Find journey" should {
      "display the page with how you find software page as the back link" in {
        val userAnswers = UserAnswers().set(HowYouFindSoftwarePage, Find).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getUserType()

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("type-of-user.heading")} - ${PageContentBase.title} - GOV.UK"),
          elementExists(s""".govuk-back-link[href="${routes.HowYouFindSoftwareController.show().url}"]""", true)
        )
      }
    }
    "the user is in the View All journey" should {
      "display the page with how you find software page as the back link" in {
        val userAnswers = UserAnswers().set(HowYouFindSoftwarePage, ViewAll).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getUserType()

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("type-of-user.heading")} - ${PageContentBase.title} - GOV.UK"),
          elementExists(s""".govuk-back-link[href="${routes.HowYouFindSoftwareController.show().url}"]""", true)
        )
      }
    }
    "the user is in the Check journey and has selected a spreadsheet as their product" should {
      "display the page with need additional software page as the back link" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, SoftwareProduct(1, "Spreadsheet100", Spreadsheet)).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getUserType()

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("type-of-user.heading")} - ${PageContentBase.title} - GOV.UK"),
          elementExists(s""".govuk-back-link[href="${routes.NeedAdditionalSoftwareController.show().url}"]""", true)
        )
      }
    }
    "the user is in the Check journey and has selected a future vendor as their product" should {
      "display the page with software in development page as the back link" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, SoftwareProduct(1, "Future100", FutureVendor)).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getUserType()

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("type-of-user.heading")} - ${PageContentBase.title} - GOV.UK"),
          elementExists(s""".govuk-back-link[href="${routes.SoftwareInDevelopmentController.show().url}"]""", true)
        )
      }
    }
    "the user is in the Check journey and has selected an unrecognised product" should {
      "display the page with no software listed page as the back link" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, SoftwareProduct(1, "Unrecognised100", Unrecognised)).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getUserType()

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("type-of-user.heading")} - ${PageContentBase.title} - GOV.UK"),
          elementExists(s""".govuk-back-link[href="${routes.NoSoftwareListedController.show().url}"]""", true)
        )
      }
    }
    "the user is in the Check journey and has selected an recognised product" should {
      "display the page with enter software name page as the back link" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, SoftwareProduct(1, "Recognised100", Recognised)).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getUserType()

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("type-of-user.heading")} - ${PageContentBase.title} - GOV.UK"),
          elementExists(s""".govuk-back-link[href="${routes.EnterSoftwareNameController.show().url}"]""", true),
          elementHasValue(".govuk-caption-l", "Recognised100")
        )
      }
    }
  }

  "POST /how-will-you-use-it" when {
    "there is nothing saved in the database for this user" must {
      "redirect to the index page" in {
        val res = SoftwareChoicesFrontend.submitUserType(Some(SoleTraderOrLandlord))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }
    "user in Find journey selects agent" must {
      s"return $SEE_OTHER and save page answer" in {
        await(userFiltersRepository.set(testUserFilters(UserAnswers()
          .set(HowYouFindSoftwarePage, Find).get
        )))

        val res = SoftwareChoicesFrontend.submitUserType(Some(UserType.Agent))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.BusinessIncomeController.show().url)
        )

        getPageData(SessionId, UserTypePage.toString).size shouldBe 1
        getAllPageData(SessionId).size shouldBe 2
        getFinalFilters(SessionId) shouldBe Seq.empty
      }
    }
    "user in Check journey selects sole trader and landlord" must {
      s"return $SEE_OTHER and save page answer" in {
        await(userFiltersRepository.set(testUserFilters(UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
        )))

        val res = SoftwareChoicesFrontend.submitUserType(Some(SoleTraderOrLandlord))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.BusinessIncomeController.show().url)
        )

        getPageData(SessionId, UserTypePage.toString).size shouldBe 1
        getAllPageData(SessionId).size shouldBe 2
        getFinalFilters(SessionId) shouldBe Seq.empty
      }
    }
    "user in ViewAll journey selects sole trader and landlord" must {
      s"return $SEE_OTHER and save page answer" in {
        await(userFiltersRepository.set(testUserFilters(UserAnswers()
          .set(HowYouFindSoftwarePage, ViewAll).get
        )))

        val res = SoftwareChoicesFrontend.submitUserType(Some(SoleTraderOrLandlord))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SearchSoftwareController.show().url)
        )

        getPageData(SessionId, UserTypePage.toString).size shouldBe 1
        getAllPageData(SessionId).size shouldBe 2
        getFinalFilters(SessionId) shouldBe Seq(Individual)
      }
    }

    "user in edit mode selects agent then changes to sole trader or landlord" must {
        s"return $SEE_OTHER and save page answer" in {
          await(userFiltersRepository.set(testUserFilters(UserAnswers()
            .set(HowYouFindSoftwarePage, Find).get
            .set(UserTypePage, UserType.Agent).get
            .set(BusinessIncomePage, Seq(SoleTrader, UkProperty)).get
          )))
          getAllPageData(SessionId).size shouldBe 3 //verify existing user answers

          val res = SoftwareChoicesFrontend.submitUserType(Some(SoleTraderOrLandlord), true)

          res should have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.CheckYourAnswersController.show().url)
          )

          getPageData(SessionId, UserTypePage) shouldBe Some(SoleTraderOrLandlord)
          getFinalFilters(SessionId) shouldBe Seq.empty
        }
      }

    "return BAD_REQUEST" when {
      "no answer is given" in {
        await(userFiltersRepository.set(testUserFilters(UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
        )))

        val res = SoftwareChoicesFrontend.submitUserType(None)

        res should have(
          httpStatus(BAD_REQUEST)
        )

        getPageData(SessionId, UserTypePage.toString).size shouldBe 0
      }
    }
  }
}
