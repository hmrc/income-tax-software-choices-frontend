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
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Environment
import play.api.http.Status
import play.api.mvc.{Codec, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserFilters
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.FreeVersion
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.SoftwareChoicesService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwarePage

import java.io.FileInputStream
import scala.concurrent.{ExecutionContext, Future}

class SearchSoftwareControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val mcc = app.injector.instanceOf[MessagesControllerComponents]
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  private val searchSoftwarePage = app.injector.instanceOf[SearchSoftwarePage]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockUserFiltersRepo: UserFiltersRepository = mock[UserFiltersRepository]

  "Show" should {
    "return OK status with the search software page" in withController { controller =>
      val result = controller.show(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }
  }

  "search" should {
    "return OK status with the search software page" in withController { controller =>
      val result = controller.search(FakeRequest("POST", "/")
        .withFormUrlEncodedBody(FiltersForm.searchTerm -> "Vendor", s"${FiltersForm.filters}[0]" -> "free-version"))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }

    "return OK status with the search software page when filter already exists" in withController { controller =>
      when(mockUserFiltersRepo.get(ArgumentMatchers.any())).thenReturn(Future.successful(Some(UserFilters("sessionId", Map.empty, Seq(FreeVersion)))))
      val result = controller.search(FakeRequest("POST", "/")
        .withFormUrlEncodedBody(FiltersForm.searchTerm -> "Vendor", s"${FiltersForm.filters}[0]" -> "free-version"))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }

    "return BAD_REQUEST" in withController { controller =>
      val result = controller.search(FakeRequest("POST", "/").withFormUrlEncodedBody((FiltersForm.searchTerm, "test" * 65)))

      status(result) shouldBe Status.BAD_REQUEST
      contentType(result) shouldBe Some(HTML)
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }
  }

  private def withController(testCode: SearchSoftwareController => Any): Unit = {
    val mockEnvironment: Environment = mock[Environment]

    when(mockUserFiltersRepo.get(ArgumentMatchers.any())).thenReturn(Future.successful(None))
    when(mockUserFiltersRepo.set(ArgumentMatchers.any())).thenReturn(Future.successful(true))
    when(mockEnvironment.resourceAsStream(eqTo(appConfig.softwareChoicesVendorFileName)))
      .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

    lazy val softwareChoicesService: SoftwareChoicesService = new SoftwareChoicesService(appConfig, mockEnvironment)

    val controller = new SearchSoftwareController(
      mcc,
      appConfig,
      searchSoftwarePage,
      softwareChoicesService,
      mockUserFiltersRepo,
      ec
    )

    testCode(controller)
  }

}
