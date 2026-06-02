/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.mvc.Result
import play.api.test.Helpers.{HTML, await, contentType, defaultAwaitTimeout, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.CheckJourney
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.mocks.{MockRequireUserDataRefiner, MockSessionIdentifierAction}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.UserTypeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod.FirstAprilToThirtyFirstMarch
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{JourneyType, SoftwareProduct, UserAnswers, UserFilters, UserType, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.SoleTraderOrLandlord
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{CapitalGainsTax, CharitableGiving, ConstructionIndustryScheme, Employment, ForeignDividends, ForeignInterest, HighIncomeChildBenefitCharge, Individual, MarriageAllowance, OverseasProperty, PaymentsIntoAPrivatePension, PrivatePensionIncome, SoleTrader, StatePensionIncome, StudentLoans, UkDividends, UkInterest, UkProperty, VoluntaryClass2NationalInsurance}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.Recognised
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.{AccountingPeriodPage, AdditionalIncomeSourcesPage, BusinessIncomePage, EnterSoftwareNamePage, HowYouFindSoftwarePage, OtherItemsPage, UserTypePage}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.PageAnswersService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.UserTypeView

import scala.concurrent.Future

class UserTypeControllerSpec extends ControllerBaseSpec with MockSessionIdentifierAction with MockRequireUserDataRefiner with FeatureSwitching{

  private val recognisedProduct = SoftwareProduct(3, "Vendor 03", Recognised)
  private val fullUserAnswers: UserAnswers = UserAnswers()
    .set(HowYouFindSoftwarePage, Check).get
    .set(EnterSoftwareNamePage, recognisedProduct).get
    .set(UserTypePage, SoleTraderOrLandlord).get
    .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
    .set(AdditionalIncomeSourcesPage, Seq(UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome,
      PrivatePensionIncome, ForeignDividends, ForeignInterest)).get
    .set(OtherItemsPage, Seq(PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans,
      MarriageAllowance, VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge)).get
    .set(AccountingPeriodPage, FirstAprilToThirtyFirstMarch).get
  private val userFilterWithFullAnswersForPage = UserFilters(sessionId, Some(fullUserAnswers), Seq.empty)


  "show" must {
    "return OK and display the user type page" when {
      "user has previously selected a user type" in new Setup(userFilters = Some(userFilterWithFullAnswersForPage)) {
        when(mockUserTypeView(
          eqTo(UserTypeForm.userTypeForm.fill(SoleTraderOrLandlord)),
          any(), any()
        )(any(), any()))
          .thenReturn(HtmlFormat.empty)

        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(UserTypePage))(any()))
          .thenReturn(Future.successful(Some(SoleTraderOrLandlord)))

        when(mockPageAnswersService.getPageAnswers(eqTo(Some(fullUserAnswers)), eqTo(EnterSoftwareNamePage))(any()))
          .thenReturn(Some(recognisedProduct))
        when(mockPageAnswersService.getPageAnswers(eqTo(Some(fullUserAnswers)), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(None)

        val result: Future[Result] = controller().show()(fakeRequest)

        status(result) shouldBe OK
        contentType(result) shouldBe Some(HTML)
      }
      "user has previously selected a user type and is in edit mode" in new Setup(userFilters = Some(userFilterWithFullAnswersForPage)) {
        when(mockUserTypeView(
          eqTo(UserTypeForm.userTypeForm.fill(SoleTraderOrLandlord)),
          any(), any()
        )(any(), any()))
          .thenReturn(HtmlFormat.empty)

        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(UserTypePage))(any()))
          .thenReturn(Future.successful(Some(SoleTraderOrLandlord)))
        when(mockPageAnswersService.getPageAnswers(eqTo(Some(fullUserAnswers)), eqTo(EnterSoftwareNamePage))(any()))
          .thenReturn(Some(recognisedProduct))

        val result: Future[Result] = controller().show(true)(fakeRequest)

        status(result) shouldBe OK
        contentType(result) shouldBe Some(HTML)
      }
      "user has not previously selected a user type" in new Setup {
        when(mockUserTypeView(
          eqTo(UserTypeForm.userTypeForm),
          any(), any()
        )(any(), any()))
          .thenReturn(HtmlFormat.empty)
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(UserTypePage))(any()))
          .thenReturn(Future.successful(None))
        when(mockPageAnswersService.getPageAnswers(eqTo(None), eqTo(EnterSoftwareNamePage))(any()))
          .thenReturn(None)
        when(mockPageAnswersService.getPageAnswers(eqTo(None), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(None)

        val result: Future[Result] = controller().show()(fakeRequest)

        status(result) shouldBe OK
        contentType(result) shouldBe Some(HTML)
      }
    }
  }

  "submit" when {
    "user in Find journey selects Sole Trader or Landlord" must {
      "save page answers and redirect to Business Income page" in new Setup {
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(SoleTraderOrLandlord))(any()))
          .thenReturn(Future.successful(true))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(Some(Find)))

        val result: Future[Result] = controller(journey = Some(Find)).submit()(fakeRequest.post(UserTypeForm.userTypeForm, SoleTraderOrLandlord))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.BusinessIncomeController.show().url)
      }
    }
    "user in Find journey and edit mode selects Agent" must {
      "save page answers and redirect to Check Your Answers page" in new Setup {
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.Agent))(any()))
          .thenReturn(Future.successful(true))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(Some(Find)))

        val result: Future[Result] = controller(journey = Some(Find)).submit(true)(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CheckYourAnswersController.show().url)
      }
    }
    "user in Check journey selects Agent" must {
      "save page answers and redirect to Business Income page" in new Setup {
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.Agent))(any()))
          .thenReturn(Future.successful(true))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(Some(Check)))

        val result: Future[Result] = controller(journey = Some(Check)).submit()(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.BusinessIncomeController.show().url)
      }
    }
    "user in Check journey and edit mode selects Sole Trader or Landlord" must {
      "save page answers and redirect to Check Your Answers page" in new Setup {
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(SoleTraderOrLandlord))(any()))
          .thenReturn(Future.successful(true))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(Some(Check)))

        val result: Future[Result] = controller(journey = Some(Check)).submit(true)(fakeRequest.post(UserTypeForm.userTypeForm, SoleTraderOrLandlord))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CheckYourAnswersController.show().url)
      }
    }
    "user in ViewAll journey selects Sole Trader or Landlord" must {
      "save page answers and redirect to Business Income page" in new Setup {
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(SoleTraderOrLandlord))(any()))
          .thenReturn(Future.successful(true))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(Some(ViewAll)))
        when(mockPageAnswersService.saveFiltersFromAnswers(sessionId))
          .thenReturn(Future(Seq(Individual)))
        val result: Future[Result] = controller(journey = Some(ViewAll)).submit()(fakeRequest.post(UserTypeForm.userTypeForm, SoleTraderOrLandlord))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.SearchSoftwareController.show().url)
      }
    }
    "user without journey (Check Feature Off) selects As an agent" must {
      "redirect to the Search Software page" when {
        "user filters are reset, page answer is set and saved to filters" in new Setup {
          when(mockPageAnswersService.resetUserAnswers(eqTo(sessionId)))
            .thenReturn(Future.successful(true))
          when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.Agent))(any()))
            .thenReturn(Future.successful(true))
          when(mockPageAnswersService.saveFiltersFromAnswers(sessionId))
            .thenReturn(Future(Seq(VendorFilter.Agent)))
          when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
            .thenReturn(Future.successful(None))

          val result: Future[Result] = controller(journey = None).submit()(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SearchSoftwareController.show().url)
        }
      }
    }
    "user without journey (Check Feature Off) selects Sole Trader or Landlord" must {
      "save page answers and redirect to Business Income page" in new Setup {
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(SoleTraderOrLandlord))(any()))
          .thenReturn(Future.successful(true))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(None))
         val result: Future[Result] = controller(journey = None).submit()(fakeRequest.post(UserTypeForm.userTypeForm, SoleTraderOrLandlord))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.BusinessIncomeController.show().url)
      }
    }
    "throw an exception" when {
      "user without journey (Check Feature Off) failed to reset user filters" in new Setup {
        when(mockPageAnswersService.resetUserAnswers(eqTo(sessionId)))
          .thenReturn(Future.successful(false))
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.Agent))(any()))
          .thenReturn(Future.successful(true))
        when(mockPageAnswersService.saveFiltersFromAnswers(eqTo(sessionId)))
          .thenReturn(Future.successful(Seq(VendorFilter.Agent)))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(None))

        val result: Future[Result] = controller(journey = None).submit()(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

        intercept[InternalServerException](await(result)).message shouldBe "[UserTypeController][submit] - Could not save agent user type"
      }
      "user without journey (Check Feature Off) failed to set page answers Agent" in new Setup {
        when(mockPageAnswersService.resetUserAnswers(eqTo(sessionId)))
          .thenReturn(Future.successful(true))
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.Agent))(any()))
          .thenReturn(Future.successful(false))
        when(mockPageAnswersService.saveFiltersFromAnswers(eqTo(sessionId)))
          .thenReturn(Future.successful(Seq(VendorFilter.Agent)))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(None))

        val result: Future[Result] = controller(journey = None).submit()(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

        intercept[InternalServerException](await(result)).message shouldBe "[UserTypeController][submit] - Could not save agent user type"
      }
      "user without journey (Check Feature Off) failed to set page answers Individual" in new Setup {
        when(mockPageAnswersService.resetUserAnswers(eqTo(sessionId)))
          .thenReturn(Future.successful(true))
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.SoleTraderOrLandlord))(any()))
          .thenReturn(Future.successful(false))
        when(mockPageAnswersService.saveFiltersFromAnswers(eqTo(sessionId)))
          .thenReturn(Future.successful(Seq(Individual)))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(None))

        val result: Future[Result] = controller(journey = None).submit()(fakeRequest.post(UserTypeForm.userTypeForm, UserType.SoleTraderOrLandlord))

        intercept[InternalServerException](await(result)).message shouldBe "[UserTypeController][submit] - Could not save sole trader or landlord user type"
      }

      "user without journey (Check Feature Off) failed to save answers to Filters" in new Setup {
        when(mockPageAnswersService.resetUserAnswers(eqTo(sessionId)))
          .thenReturn(Future.successful(true))
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.Agent))(any()))
          .thenReturn(Future.successful(true))
        when(mockPageAnswersService.saveFiltersFromAnswers(eqTo(sessionId)))
          .thenReturn(Future.successful(Seq.empty))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(None))

        val result: Future[Result] = controller(journey = None).submit()(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

        intercept[InternalServerException](await(result)).message shouldBe "[UserTypeController][submit] - Could not save agent user type"
      }

      "user in Check journey failed to save answers to Filters" in new Setup {
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.Agent))(any()))
          .thenReturn(Future.successful(false))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(Some(Check)))

        val result: Future[Result] = controller(journey = None).submit()(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

        intercept[InternalServerException](await(result)).message shouldBe "[UserTypeController][submit] - Could not save user type for find or check journey"
      }

      "user in Check journey in edit mode failed to save answers to Filters" in new Setup {
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.Agent))(any()))
          .thenReturn(Future.successful(false))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(Some(Check)))

        val result: Future[Result] = controller(journey = None).submit(editMode = true)(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

        intercept[InternalServerException](await(result)).message shouldBe "[UserTypeController][submit] - Could not save user type for find or check journey"
      }


      "user in Find journey failed to save answers to Filters" in new Setup {
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.Agent))(any()))
          .thenReturn(Future.successful(false))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(Some(Find)))

        val result: Future[Result] = controller(journey = None).submit()(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

        intercept[InternalServerException](await(result)).message shouldBe "[UserTypeController][submit] - Could not save user type for find or check journey"
      }

      "user in ViewAll journey failed to save answers to Filters" in new Setup {
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.Agent))(any()))
          .thenReturn(Future.successful(false))
        when(mockPageAnswersService.saveFiltersFromAnswers(eqTo(sessionId)))
          .thenReturn(Future.successful(Seq.empty))
        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(HowYouFindSoftwarePage))(any()))
          .thenReturn(Future.successful(Some(ViewAll)))

        val result: Future[Result] = controller(journey = None).submit()(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

        intercept[InternalServerException](await(result)).message shouldBe "[UserTypeController][submit] - Could not save user type for view all journey"
      }

    }
  }

  "backUrl" should {
    "return the guidance page url when CheckJourney feature switch is disabled" in new Setup(userFilters = Some(userFilterWithFullAnswersForPage)) {
      disable(CheckJourney)

      when(mockPageAnswersService.getPageAnswers(eqTo(Some(fullUserAnswers)), eqTo(EnterSoftwareNamePage))(any()))
        .thenReturn(Some(recognisedProduct))
      when(mockPageAnswersService.getPageAnswers(eqTo(Some(fullUserAnswers)), eqTo(HowYouFindSoftwarePage))(any()))
        .thenReturn(None)

      controller().backUrl(answers = Some(fullUserAnswers)) shouldBe appConfig.guidance
    }
    "return the HowYouFindSoftware page url when CheckJourney feature switch is enabled" in new Setup(userFilters = Some(userFilterWithFullAnswersForPage)) {
      enable(CheckJourney)

      when(mockPageAnswersService.getPageAnswers(eqTo(Some(fullUserAnswers)), eqTo(EnterSoftwareNamePage))(any()))
        .thenReturn(Some(recognisedProduct))
      when(mockPageAnswersService.getPageAnswers(eqTo(Some(fullUserAnswers)), eqTo(HowYouFindSoftwarePage))(any()))
        .thenReturn(Some(Find))

      controller().backUrl(answers = Some(fullUserAnswers)) shouldBe routes.HowYouFindSoftwareController.show().url
    }
    "return the CheckYourAnswers page url when in edit mode" in new Setup(userFilters = Some(userFilterWithFullAnswersForPage)) {
      when(mockPageAnswersService.getPageAnswers(eqTo(Some(fullUserAnswers)), eqTo(EnterSoftwareNamePage))(any()))
        .thenReturn(Some(recognisedProduct))

      controller().backUrl(answers = Some(fullUserAnswers), editMode = true) shouldBe routes.CheckYourAnswersController.show().url
    }
  }

  class Setup(userFilters: Option[UserFilters] = None) {
    val mockPageAnswersService: PageAnswersService = mock[PageAnswersService]
    lazy val mockUserTypeView: UserTypeView = mock[UserTypeView]
    val mockUserFiltersRepository: UserFiltersRepository = mock[UserFiltersRepository]

    when(mockUserFiltersRepository.get(any())).thenReturn(Future.successful(userFilters))


    def controller(journey: Option[JourneyType] = None): UserTypeController = new UserTypeController(
      mockUserTypeView,
      mockPageAnswersService,
      mockUserFiltersRepository,
      fakeSessionIdentifierAction,
      fakeRequireUserDataRefiner(journey = journey),
      appConfig
    )(ec, mcc)
  }

}
