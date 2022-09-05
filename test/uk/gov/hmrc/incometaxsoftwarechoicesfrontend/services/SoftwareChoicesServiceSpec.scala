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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.IncomeAndDeduction.{BlindPersonsAllowance, CapitalGainsTax}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{FreeTrial, FreeVersion}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, SoftwareVendors}

import java.io.FileInputStream

class SoftwareChoicesServiceSpec extends PlaySpec with BeforeAndAfterEach {

  class Setup {
    val mockEnvironment: Environment = mock[Environment]
    lazy val service: SoftwareChoicesService = new SoftwareChoicesService(mockEnvironment)
  }

  private val testVendorOne = SoftwareVendorModel(
    "test software vendor one",
    "/test-url-one",
    "test@software-vendor-name-one.com",
    "11111 111 111",
    "software-vendor-name-one.com",
    Seq(FreeVersion),
    Seq(BlindPersonsAllowance),
    accessibilityStatementLink = Some("software-vendor-accessibility.com")
  )

  private val testVendorTwo = SoftwareVendorModel(
    "test software vendor two",
    "/test-url-two",
    "test@software-vendor-name-two.com",
    "22222 222 222",
    "software-vendor-name-two.com",
    Seq(FreeTrial),
    Seq(CapitalGainsTax)
  )

  private val testVendorThree = SoftwareVendorModel(
    "test software vendor three",
    "/test-url-three",
    "test@software-vendor-name-three.com",
    "33333 333 333",
    "software-vendor-name-three.com",
    Seq(FreeTrial, FreeVersion),
    Seq(BlindPersonsAllowance, CapitalGainsTax)
  )

  val expectedSoftwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = "06/07/2022",
    vendors = Seq(
      testVendorOne,
      testVendorTwo,
      testVendorThree,
    )
  )

  val expectedFilteredByVendorNameSoftwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = "06/07/2022",
    vendors = Seq(
      testVendorTwo
    )
  )

  val expectedFilteredByVendorFilterSoftwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = "06/07/2022",
    vendors = Seq(
      testVendorTwo,
      testVendorThree
    )
  )

  val expectedFilteredSoftwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = "06/07/2022",
    vendors = Seq(
      testVendorThree
    )
  )

  "softwareVendors" when {
    "the software vendor config file exists" must {
      "correctly retrieve and parse file" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(SoftwareChoicesService.softwareVendorsFileName)))
          .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

        service.softwareVendors mustBe expectedSoftwareVendors
      }

      "correctly filter vendors by vendor name" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(SoftwareChoicesService.softwareVendorsFileName)))
          .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

        service.filterVendors(Some("two"), Seq()) mustBe expectedFilteredByVendorNameSoftwareVendors
      }

      "correctly filter vendors by vendor filter" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(SoftwareChoicesService.softwareVendorsFileName)))
          .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

        service.filterVendors(None, Seq(FreeTrial)) mustBe expectedFilteredByVendorFilterSoftwareVendors
      }

      "correctly filter vendors by vendor name and vendor filter" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(SoftwareChoicesService.softwareVendorsFileName)))
          .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

        service.filterVendors(Some("three"), Seq(FreeTrial)) mustBe expectedFilteredSoftwareVendors
      }

      "not filter when no search term has been provided" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(SoftwareChoicesService.softwareVendorsFileName)))
          .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

        service.filterVendors(None, Seq()) mustBe expectedSoftwareVendors
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

  "get software vendor" when {
    "fetching a software vendor which exists" should {
      "return that vendor" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(SoftwareChoicesService.softwareVendorsFileName)))
          .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

        private val name = "test software vendor one"
        private val maybeVendorModel: Option[SoftwareVendorModel] = service.getSoftwareVendor(name)
        maybeVendorModel.isDefined mustBe true
        maybeVendorModel.get.name mustBe name
      }
    }
    "fetching a software vendor which does not exist" should {
      "return None" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(SoftwareChoicesService.softwareVendorsFileName)))
          .thenReturn(Some(new FileInputStream("test/resources/test-valid-software-vendors.json")))

        private val name = "test software vendor one hundred"
        private val maybeVendorModel: Option[SoftwareVendorModel] = service.getSoftwareVendor(name)
        maybeVendorModel.isDefined mustBe false
      }
    }
  }

}
