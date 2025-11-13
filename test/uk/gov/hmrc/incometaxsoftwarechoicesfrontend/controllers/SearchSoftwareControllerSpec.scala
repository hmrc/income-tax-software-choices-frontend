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
import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.Environment
import play.api.http.Status
import play.api.mvc.Codec
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.mocks.{MockRequireUserDataRefiner, MockSessionIdentifierAction}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserFilters
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{Individual, SoleTrader, StandardUpdatePeriods}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.{DataService, PageAnswersService, SoftwareChoicesService}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwareView

import java.io.FileInputStream
import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

class SearchSoftwareControllerSpec extends ControllerBaseSpec
  with MockSessionIdentifierAction
  with MockRequireUserDataRefiner
  with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
  }
  val searchSoftwarePage: SearchSoftwareView = app.injector.instanceOf[SearchSoftwareView]

  "Show" when {
    "return OK status with the search software page" in withController { controller =>
      val result = controller.show(zeroResults = false)(fakeRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }
  }

  "search" when {
    "return OK status with the search software page" in withController { controller =>
      val result = controller.search(zeroResults = false)(FakeRequest("POST", "/")
        .withFormUrlEncodedBody(s"${FiltersForm.filters}[0]" -> "free-version"))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }
  }

  "backLinkUrl" when {
    "user type is sole trader or landlord" when {
      "redirect to the choosing software page" in withController { controller =>
        controller.backLinkUrl(zeroResults = false, isAgent = false) shouldBe routes.ChoosingSoftwareController.show().url
      }
    }
    "user type is Agent" should {
      "return to the user type page" in withController { controller =>
        controller.backLinkUrl(zeroResults = false, isAgent = true) shouldBe routes.UserTypeController.show().url
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
      fakeRequireUserDataRefiner
    )(ec, appConfig, mcc)

    testCode(controller)
  }

}
