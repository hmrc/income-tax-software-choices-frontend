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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services

import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.Environment
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.TestModels.{testVendorOne, testVendorThree, testVendorTwo}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.FreeVersion
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, SoftwareVendors}

import java.io.FileInputStream
import java.time.LocalDate

class SoftwareChoicesServiceSpec extends PlaySpec with BeforeAndAfterEach {

  private val testFileName: String = "test-software-vendors.json"
  private val validVendors = "test/resources/test-valid-software-vendors.json"
  private val invalidVendors = "test/resources/test-invalid-software-vendors.json"

  class Setup {
    val mockConfig: AppConfig = mock[AppConfig]
    val mockEnvironment: Environment = mock[Environment]

    lazy val service: SoftwareChoicesService = new SoftwareChoicesService(mockConfig, mockEnvironment)

    when(mockConfig.softwareChoicesVendorFileName) thenReturn testFileName
  }

  val unsortedSoftwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = LocalDate.of(2022, 12, 2),
    vendors = Seq(
      testVendorOne,
      testVendorTwo,
      testVendorThree,
    )
  )

  val expectedSoftwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = LocalDate.of(2022, 12, 2),
    vendors = Seq(
      testVendorOne,
      testVendorThree,
      testVendorTwo,
    )
  )

  val expectedFilteredByVendorNameSoftwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = LocalDate.of(2022, 12, 2),
    vendors = Seq(
      testVendorTwo
    )
  )

  val expectedFilteredByVendorFilterSoftwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = LocalDate.of(2022, 12, 2),
    vendors = Seq(
      testVendorOne,
      testVendorThree
    )
  )

  val expectedFilteredSoftwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = LocalDate.of(2022, 12, 2),
    vendors = Seq(
      testVendorThree
    )
  )

  "getAllInOne softwareVendors" when {
    "the software vendor config file exists" must {
      "correctly retrieve and parse file" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(testFileName)))
          .thenReturn(Some(new FileInputStream(validVendors)))

        service.softwareVendors mustBe unsortedSoftwareVendors
      }

      "correctly filter vendors by vendor filter" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(testFileName)))
          .thenReturn(Some(new FileInputStream(validVendors)))

        service.getAllInOneVendors(Seq(FreeVersion)) mustBe expectedFilteredByVendorFilterSoftwareVendors
      }

      "not filter when no search term has been provided" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(testFileName)))
          .thenReturn(Some(new FileInputStream(validVendors)))

        service.getAllInOneVendors(Seq()) mustBe expectedSoftwareVendors
      }
    }

    "the software vendor config file does not exist" in new Setup {
      when(mockEnvironment.resourceAsStream(eqTo(testFileName)))
        .thenReturn(None)

      intercept[InternalServerException](service.softwareVendors).message mustBe s"[SoftwareChoicesService][jsonFile] - $testFileName not found"
    }
    "the software vendor config file contains invalid json" in new Setup {
      when(mockEnvironment.resourceAsStream(eqTo(testFileName)))
        .thenReturn(Some(new FileInputStream(invalidVendors)))

      intercept[InternalServerException](service.softwareVendors).message
        .contains("[SoftwareChoicesService][softwareVendors] - Json parse failures") mustBe true
    }
  }

  "get software vendor" when {
    "fetching a software vendor which exists" should {
      "return that vendor" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(testFileName)))
          .thenReturn(Some(new FileInputStream(validVendors)))

        private val name = "test software vendor one"
        private val maybeVendorModel: Option[SoftwareVendorModel] = service.getSoftwareVendor(name)
        maybeVendorModel.isDefined mustBe true
        maybeVendorModel.get.name mustBe name
      }
    }
    "fetching a software vendor which does not exist" should {
      "return None" in new Setup {
        when(mockEnvironment.resourceAsStream(eqTo(testFileName)))
          .thenReturn(Some(new FileInputStream(validVendors)))

        private val name = "test software vendor one hundred"
        private val maybeVendorModel: Option[SoftwareVendorModel] = service.getSoftwareVendor(name)
        maybeVendorModel.isDefined mustBe false
      }
    }
  }

}
