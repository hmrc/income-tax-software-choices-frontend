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
import org.scalatest.BeforeAndAfterEach
import play.api.Environment
import play.api.http.Status
import play.api.mvc.{Codec, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.BetaFeatures
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.SoftwareChoicesService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.{SearchSoftwarePage}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.templates.{SoftwareVendorsTemplate, SoftwareVendorsTemplateAlpha}

import java.io.FileInputStream

class SearchSoftwareControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach with FeatureSwitching {

  private val mcc = app.injector.instanceOf[MessagesControllerComponents]
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  private val searchSoftwarePage = app.injector.instanceOf[SearchSoftwarePage]
  private val searchVendorsTemplateAlpha = app.injector.instanceOf[SoftwareVendorsTemplateAlpha]
  private val searchVendorsTemplate = app.injector.instanceOf[SoftwareVendorsTemplate]

  protected override def beforeEach() = disable(BetaFeatures)

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
      val result = controller.search(false)(FakeRequest("POST", "/")
        .withFormUrlEncodedBody(FiltersForm.searchTerm -> "Vendor", s"${FiltersForm.filters}[0]" -> "free-version"))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(HTML)
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }

    "return BAD_REQUEST" in withController { controller =>
      val result = controller.search(false)(FakeRequest("POST", "/").withFormUrlEncodedBody((FiltersForm.searchTerm, "test" * 65)))

      status(result) shouldBe Status.BAD_REQUEST
      contentType(result) shouldBe Some(HTML)
      charset(result) shouldBe Some(Codec.utf_8.charset)
    }
  }

  "ajaxSearch" when {
    "beta features are off" should {
      "return OK status with with the correct count returned for an empty filter" in withController { controller =>
        val message = getCountMessage(3)
        val tuple = Seq.empty[(String, String)]

        testSearch(controller, message, tuple)
      }

      "return OK status with the correct count returned for a vendor name search" in withController { controller =>
        val message = getCountMessage(1)
        val tuple = Seq(FiltersForm.searchTerm -> "test software vendor three")

        testSearch(controller, message, tuple)
      }

      "return OK status with the correct count returned for a vendor name search (case insensitive)" in withController { controller =>
        val message = getCountMessage(1)
        val tuple = Seq(FiltersForm.searchTerm -> "TEST SOFTWARE VENDOR THREE")

        testSearch(controller, message, tuple)
      }

      "return OK status with the correct count returned for one filter" in withController { controller =>
        val message = getCountMessage(2)
        val tuple = Seq(FiltersForm.searchTerm -> "Vendor", s"${FiltersForm.filters}[0]" -> "free-version")

        testSearch(controller, message, tuple)
      }

      "return OK status with the correct count returned for two filters" in withController { controller =>
        val message = getCountMessage(1)
        val tuple = Seq(FiltersForm.searchTerm -> "Vendor", s"${FiltersForm.filters}[0]" -> "free-version", s"${FiltersForm.filters}[1]" -> "free-trial")

        testSearch(controller, message, tuple)
      }
    }
    "beta features are on" should {
      "return OK status with with the correct count returned for an empty filter" in withController { controller =>
        enable(BetaFeatures)
        val message = getCountMessage(3, true)
        val tuple = Seq.empty[(String, String)]

        testSearch(controller, message, tuple)
      }

      "return OK status with the correct count returned for a vendor name search" in withController { controller =>
        enable(BetaFeatures)
        val message = getCountMessage(1, true)
        val tuple = Seq(FiltersForm.searchTerm -> "test software vendor three")

        testSearch(controller, message, tuple)
      }

      "return OK status with the correct count returned for a vendor name search (case insensitive)" in withController { controller =>
        enable(BetaFeatures)
        val message = getCountMessage(1, true)
        val tuple = Seq(FiltersForm.searchTerm -> "TEST SOFTWARE VENDOR THREE")

        testSearch(controller, message, tuple)
      }

      "return OK status with the correct count returned for one filter" in withController { controller =>
        enable(BetaFeatures)
        val message = getCountMessage(2, true)
        val tuple = Seq(FiltersForm.searchTerm -> "Vendor", s"${FiltersForm.filters}[0]" -> "free-version")

        testSearch(controller, message, tuple)
      }

      "return OK status with the correct count returned for two filters" in withController { controller =>
        enable(BetaFeatures)
        val message = getCountMessage(1, true)
        val tuple = Seq(FiltersForm.searchTerm -> "Vendor", s"${FiltersForm.filters}[0]" -> "free-version", s"${FiltersForm.filters}[1]" -> "free-trial")

        testSearch(controller, message, tuple)
      }
    }
    "ajaxSearch" when {
      "return BAD_REQUEST" in withController { controller =>
        val result = controller.search(true)(FakeRequest("POST", "/").withFormUrlEncodedBody((FiltersForm.searchTerm, "test" * 65)))

        status(result) shouldBe Status.BAD_REQUEST
        contentType(result) shouldBe Some(HTML)
        charset(result) shouldBe Some(Codec.utf_8.charset)
      }
    }
  }

  private def testSearch(controller: SearchSoftwareController, message: String, tuple: Seq[(String, String)]) = {
    val result = controller.search(true)(FakeRequest("POST", "/").withFormUrlEncodedBody(tuple: _*))
    status(result) shouldBe Status.OK
    contentType(result) shouldBe Some(HTML)
    contentAsString(result) should include(message)
    charset(result) shouldBe Some(Codec.utf_8.charset)
  }

  private def getCountMessage(value: Int, beta: Boolean = false) = (beta, value>1) match {
    case (true, true) => s"$value software providers"
    case (true, false) => s"$value software provider"
    case (false, true) => s"Currently there are $value software providers"
    case (false, false) => s"Currently there is 1 software provider"
  }

  private def withController(testCode: SearchSoftwareController => Any): Unit = {
    val mockEnvironment: Environment = mock[Environment]

    when(mockEnvironment.resourceAsStream(eqTo(appConfig.softwareChoicesVendorFileName)))
      .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

    lazy val softwareChoicesService: SoftwareChoicesService = new SoftwareChoicesService(appConfig, mockEnvironment)

    val controller = new SearchSoftwareController(
      mcc,
      appConfig,
      searchSoftwarePage,
      searchVendorsTemplateAlpha,
      searchVendorsTemplate,
      softwareChoicesService
    )

    testCode(controller)
  }

}
