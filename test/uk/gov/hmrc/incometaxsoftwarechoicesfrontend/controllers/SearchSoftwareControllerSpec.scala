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

import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.{mock, when}
import play.api.Environment
import play.api.http.Status
import play.api.mvc.{Codec, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.SoftwareChoicesService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.{SearchSoftwarePage, SoftwareVendorsTemplate}

import java.io.FileInputStream

class SearchSoftwareControllerSpec extends ControllerBaseSpec {

  private val mcc = app.injector.instanceOf[MessagesControllerComponents]
  private val searchSoftwarePage = app.injector.instanceOf[SearchSoftwarePage]
  private val searchVendorsTemplate = app.injector.instanceOf[SoftwareVendorsTemplate]

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

    "return BAD_REQUEST" in withController { controller =>
      val result = controller.search(FakeRequest("POST", "/").withFormUrlEncodedBody((FiltersForm.searchTerm, "test" * 65)))

      status(result) shouldBe Status.BAD_REQUEST
      contentType(result) shouldBe Some(HTML)
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }
  }

  "ajaxSearch" should {
    "return OK status with with the correct count returned for an empty filter" in withController { controller =>
      val result = controller.ajaxSearch(FakeRequest("POST", "/")
        .withFormUrlEncodedBody())

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      contentAsString(result) should include("Currently there are 3 software providers")
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }

    "return OK status with the correct count returned for a vendor name search" in withController { controller =>
      val result = controller.ajaxSearch(FakeRequest("POST", "/")
        .withFormUrlEncodedBody(FiltersForm.searchTerm -> "test software vendor three"))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      contentAsString(result) should include("Currently there are 1 software providers")
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }

    "return OK status with the correct count returned for a vendor name search (case insensitive)" in withController { controller =>
      val result = controller.ajaxSearch(FakeRequest("POST", "/")
        .withFormUrlEncodedBody(FiltersForm.searchTerm -> "TEST SOFTWARE VENDOR THREE"))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      contentAsString(result) should include("Currently there are 1 software providers")
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }

    "return OK status with the correct count returned for one filter" in withController { controller =>
      val result = controller.ajaxSearch(FakeRequest("POST", "/")
        .withFormUrlEncodedBody(FiltersForm.searchTerm -> "Vendor", s"${FiltersForm.filters}[0]" -> "free-version"))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      contentAsString(result) should include("Currently there are 2 software providers")
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }

    "return OK status with the correct count returned for two filters" in withController { controller =>
      val result = controller.ajaxSearch(FakeRequest("POST", "/")
        .withFormUrlEncodedBody(FiltersForm.searchTerm -> "Vendor", s"${FiltersForm.filters}[0]" -> "free-version", s"${FiltersForm.filters}[1]" -> "free-trial"))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      contentAsString(result) should include("Currently there are 1 software providers")
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }

    "return BAD_REQUEST" in withController { controller =>
      val result = controller.ajaxSearch(FakeRequest("POST", "/").withFormUrlEncodedBody((FiltersForm.searchTerm, "test" * 65)))

      status(result) shouldBe Status.BAD_REQUEST
      contentType(result) shouldBe Some(HTML)
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }
  }

  private def withController(testCode: SearchSoftwareController => Any): Unit = {
    val mockEnvironment: Environment = mock[Environment]

    when(mockEnvironment.resourceAsStream(eqTo(SoftwareChoicesService.softwareVendorsFileName)))
      .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

    lazy val softwareChoicesService: SoftwareChoicesService = new SoftwareChoicesService(mockEnvironment)

    val controller = new SearchSoftwareController(
      mcc,
      searchSoftwarePage,
      searchVendorsTemplate,
      softwareChoicesService
    )

    testCode(controller)
  }

}
