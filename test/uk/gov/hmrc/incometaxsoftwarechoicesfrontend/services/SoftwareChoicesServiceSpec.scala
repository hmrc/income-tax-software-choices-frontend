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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services

import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.Environment
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{FreeTrial, FreeVersion}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, SoftwareVendors}

import java.io.FileInputStream

class SoftwareChoicesServiceSpec extends PlaySpec with BeforeAndAfterEach {

  class Setup {
    val mockEnvironment: Environment = mock[Environment]
    lazy val service: SoftwareChoicesService = new SoftwareChoicesService(mockEnvironment)
  }

  val expectedSoftwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = "06/07/2022",
    vendors = Seq(
      SoftwareVendorModel("test software vendor one", "/test-url-one", Seq(FreeVersion)),
      SoftwareVendorModel("test software vendor two", "/test-url-two", Seq(FreeTrial))
    )
  )

  val expectedFilteredSoftwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = "06/07/2022",
    vendors = Seq(
      SoftwareVendorModel("test software vendor two", "/test-url-two", Seq(FreeTrial))
    )
  )

  "softwareVendors" when {
    "the software vendor config file exists" must {
      "correctly retrieve and parse file" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(SoftwareChoicesService.softwareVendorsFileName)))
          .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

        service.softwareVendors mustBe expectedSoftwareVendors
      }

      "correctly filter vendors by name" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(SoftwareChoicesService.softwareVendorsFileName)))
          .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

        service.filterVendors(Some("two")) mustBe expectedFilteredSoftwareVendors
      }

      "not filter when no search term has been provided" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(SoftwareChoicesService.softwareVendorsFileName)))
          .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

        service.filterVendors(None) mustBe expectedSoftwareVendors
      }
    }
    "the software vendor config file does not exist" in new Setup {
      when(mockEnvironment.resourceAsStream(eqTo(SoftwareChoicesService.softwareVendorsFileName)))
        .thenReturn(None)

      intercept[InternalServerException](service.softwareVendors).message mustBe "[SoftwareChoicesService][jsonFile] - file not found"
    }
    "the software vendor config file contains invalid json" in new Setup {
      when(mockEnvironment.resourceAsStream(eqTo(SoftwareChoicesService.softwareVendorsFileName)))
        .thenReturn(Some(new FileInputStream("test/resources/test-invalid-software-vendors.json")))

      intercept[InternalServerException](service.softwareVendors).message
        .contains("[SoftwareChoicesService][softwareVendors] - Json parse failures") mustBe true
    }
  }

}
