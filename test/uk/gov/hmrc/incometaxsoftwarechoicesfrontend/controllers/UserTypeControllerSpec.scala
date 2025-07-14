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

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.mvc.Result
import play.api.test.Helpers.{HTML, await, contentType, defaultAwaitTimeout, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.UserTypeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.SoleTraderOrLandlord
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.Agent
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.UserTypePage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.PageAnswersService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.UserTypePage

import scala.concurrent.Future

class UserTypeControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  lazy val mockPageAnswersService: PageAnswersService = mock[PageAnswersService]
  lazy val mockUserTypeView: UserTypePage = mock[UserTypePage]

  "show" must {
    "return OK and display the user type page" when {
      "user has previously selected a user type" in new Setup {
        when(mockUserTypeView(
          eqTo(UserTypeForm.userTypeForm.fill(SoleTraderOrLandlord)),
          any(), any()
        )(any(), any()))
          .thenReturn(HtmlFormat.empty)

        when(mockPageAnswersService.getPageAnswers(eqTo(sessionId), eqTo(UserTypePage))(any()))
          .thenReturn(Future.successful(Some(SoleTraderOrLandlord)))

        val result: Future[Result] = controller.show()(fakeRequest)

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

        val result: Future[Result] = controller.show()(fakeRequest)

        status(result) shouldBe OK
        contentType(result) shouldBe Some(HTML)
      }
    }
  }

  "submit" when {
    "user selects Sole Trader or Landlord" must {
      "save page answers and redirect to Business Income page" in new Setup {
        when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(SoleTraderOrLandlord))(any()))
          .thenReturn(Future.successful(true))

        val result: Future[Result] = controller.submit()(fakeRequest.post(UserTypeForm.userTypeForm, SoleTraderOrLandlord))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.BusinessIncomeController.show().url)
      }
    }
    "user selects As an agent" must {
      "redirect to the Search Software page" when {
        "user filters are reset, page answer is set and saved to filters" in new Setup {
          when(mockPageAnswersService.resetUserAnswers(eqTo(sessionId)))
            .thenReturn(Future.successful(true))
          when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.Agent))(any()))
            .thenReturn(Future.successful(true))
          when(mockPageAnswersService.saveFiltersFromAnswers(sessionId))
            .thenReturn(Future(Seq(Agent)))

          val result: Future[Result] = controller.submit()(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

          status(result) shouldBe SEE_OTHER
        }

      }
      "throw an exception" when {
        "failed to reset user filters" in new Setup {
          when(mockPageAnswersService.resetUserAnswers(eqTo(sessionId)))
            .thenReturn(Future.successful(false))

          val result: Future[Result] = controller.submit()(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

          intercept[InternalServerException](await(result)).message shouldBe "[UserTypeController][submit] - Could not save agent user type"
        }
        "failed to set page answers" in new Setup {
          when(mockPageAnswersService.resetUserAnswers(eqTo(sessionId)))
            .thenReturn(Future.successful(false))
          when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.Agent))(any()))
            .thenReturn(Future.successful(false))

          val result: Future[Result] = controller.submit()(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

          intercept[InternalServerException](await(result)).message shouldBe "[UserTypeController][submit] - Could not save agent user type"
        }

        "failed to save answers to Filters" in new Setup {
          when(mockPageAnswersService.resetUserAnswers(eqTo(sessionId)))
            .thenReturn(Future.successful(false))
          when(mockPageAnswersService.setPageAnswers(eqTo(sessionId), eqTo(UserTypePage), eqTo(UserType.Agent))(any()))
            .thenReturn(Future.successful(false))
          when(mockPageAnswersService.saveFiltersFromAnswers(sessionId))
            .thenReturn(Future(Seq.empty))

          val result: Future[Result] = controller.submit()(fakeRequest.post(UserTypeForm.userTypeForm, UserType.Agent))

          intercept[InternalServerException](await(result)).message shouldBe "[UserTypeController][submit] - Could not save agent user type"
        }

      }
    }
  }

  trait Setup {
    val controller: UserTypeController = new UserTypeController(
      mockUserTypeView,
      mockPageAnswersService,
      appConfig
    )(ec, mcc)
    val mockUserFiltersRepository: UserFiltersRepository = mock[UserFiltersRepository]
  }

}
