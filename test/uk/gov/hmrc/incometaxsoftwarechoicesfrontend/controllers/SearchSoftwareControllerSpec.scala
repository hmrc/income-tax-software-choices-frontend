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

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import play.api.Environment
import play.api.http.Status
import play.api.mvc.Codec
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.ExplicitAudits
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.mocks.{MockRequireUserDataRefiner, MockSessionIdentifierAction}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserFilters
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{Individual, SoleTrader, StandardUpdatePeriods}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.audit.{AuditEvent, SearchResultsEvent}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.{AuditService, DataService, PageAnswersService, SoftwareChoicesService}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwareView

import java.io.FileInputStream
import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

class SearchSoftwareControllerSpec extends ControllerBaseSpec
  with MockSessionIdentifierAction
  with MockRequireUserDataRefiner
  with BeforeAndAfterEach
  with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(auditService)
    disable(ExplicitAudits)
  }

  val searchSoftwarePage: SearchSoftwareView = app.injector.instanceOf[SearchSoftwareView]
  val auditService: AuditService = mock[AuditService]

  "Show" must {
    "return OK status with the search software page" in withController { controller =>
      val result = controller.show()(fakeRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      charset(result) shouldBe Some(Codec.utf_8.charset)
      verify(auditService, times(0)).audit(any[SearchResultsEvent]())(any(), any())
    }

    "send audit when ExplicitAudits feature switch is enabled" in withController { controller =>
      enable(ExplicitAudits)

      doNothing().when(auditService).audit(any[AuditEvent]())(any(), any())

      val result = controller.show()(fakeRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      charset(result) shouldBe Some(Codec.utf_8.charset)
      verify(auditService, times(1)).audit(any[SearchResultsEvent]())(any(), any())
    }
  }

  "search" must {
    "return OK status with the search software page" in withController { controller =>
      val result = controller.search()(FakeRequest("POST", "/")
        .withFormUrlEncodedBody(s"${FiltersForm.filters}[0]" -> "free-version"))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      charset(result) shouldBe Some(Codec.utf_8.charset)
      verify(auditService, times(0)).audit(any[SearchResultsEvent]())(any(), any())

    }

    "send audit when ExplicitAudits feature switch is enabled" in withController { controller =>
      enable(ExplicitAudits)

      doNothing().when(auditService).audit(any[AuditEvent]())(any(), any())

      val result = controller.search()(FakeRequest("POST", "/")
        .withFormUrlEncodedBody(s"${FiltersForm.filters}[0]" -> "free-version"))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      verify(auditService, times(1)).audit(any[SearchResultsEvent]())(any(), any())
    }
  }

  "backLinkUrl" when {
    "user type is sole trader or landlord" when {
      "redirect to the choosing software page" in withController { controller =>
        controller.backLinkUrl(isAgent = false) shouldBe routes.ChoosingSoftwareController.show().url
      }
    }
    "user type is Agent" should {
      "return to the user type page" in withController { controller =>
        controller.backLinkUrl(isAgent = true) shouldBe routes.UserTypeController.show().url
      }
    }
  }

  private def withController(testCode: SearchSoftwareController => Any): Unit = {
    val mockEnvironment: Environment = mock[Environment]
    val mockUserFiltersRepo: UserFiltersRepository = mock[UserFiltersRepository]

    when(mockUserFiltersRepo.get(ArgumentMatchers.any())).thenReturn(
      Future.successful(Some(UserFilters(
        id = UUID.randomUUID().toString,
        answers = None,
        finalFilters = Seq(Individual, SoleTrader, StandardUpdatePeriods),
        lastUpdated = Instant.now
      )))
    )
    when(mockUserFiltersRepo.set(ArgumentMatchers.any())).thenReturn(Future.successful(true))
    when(mockEnvironment.resourceAsStream(eqTo(appConfig.softwareChoicesVendorFileName)))
      .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

    lazy val softwareChoicesService: SoftwareChoicesService = new SoftwareChoicesService(new DataService(appConfig, mockEnvironment))
    lazy val pageAnswerService: PageAnswersService = new PageAnswersService(mockUserFiltersRepo, ec)

    val controller = new SearchSoftwareController(
      searchSoftwarePage,
      softwareChoicesService,
      pageAnswerService,
      mockUserFiltersRepo,
      fakeSessionIdentifierAction,
      fakeRequireUserDataRefiner,
      auditService
    )(ec, appConfig, mcc)

    testCode(controller)
  }

}
