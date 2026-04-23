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

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.Environment
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.TestModels.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.{FutureVendor, Spreadsheet}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareProduct, SoftwareVendors}

import java.io.FileInputStream
import java.time.LocalDate

class DataServiceSpec extends PlaySpec {
  private val testFileName: String = "test-software-vendors.json"
  private val validVendors = "test/resources/test-valid-software-vendors.json"
  private val invalidVendors = "test/resources/test-invalid-software-vendors.json"
  private val productionVendors = "conf/software-vendors.json"

  private val testOtherSoftwareFileName: String = "test-other-software.json"
  private val validOtherSoftware = "test/resources/test-other-software.json"
  private val invalidOtherSoftware = "test/resources/test-invalid-other-software.json"
  private val productionOtherSoftware = "conf/other-software.json"

  class Setup {
    val mockConfig: AppConfig = mock[AppConfig]
    val mockEnvironment: Environment = mock[Environment]

    lazy val service: DataService = new DataService(
      mockConfig,
      mockEnvironment
    )

    when(mockConfig.softwareChoicesVendorFileName) thenReturn testFileName
    when(mockConfig.otherSoftwareFileName) thenReturn testOtherSoftwareFileName
    when(mockEnvironment.resourceAsStream(eqTo(testOtherSoftwareFileName))).thenReturn(Some(new FileInputStream(validOtherSoftware)))
    when(mockEnvironment.resourceAsStream(eqTo(testFileName))).thenReturn(Some(new FileInputStream(validVendors)))
  }

  val unsortedSoftwareVendors: SoftwareVendors = SoftwareVendors(
    lastUpdated = LocalDate.of(2022, 12, 2),
    vendors = Seq(
      testVendorOne,
      testVendorTwo,
      testVendorThree,
      testVendorFour,
      testVendorFive
    )
  )

  val validOtherSoftwareProducts: Seq[SoftwareProduct] = Seq(
    SoftwareProduct(1001, "Microsoft Excel", Spreadsheet),
    SoftwareProduct(1002, "Google Sheets", Spreadsheet),
    SoftwareProduct(2001, "Maybe Vendor", FutureVendor)
    )

  "getSoftwareVendors" when {

    "the software vendor production config file exists and is valid" in new Setup {
      when(mockEnvironment.resourceAsStream(eqTo(testFileName)))
        .thenReturn(Some(new FileInputStream(productionVendors)))

      noException shouldBe thrownBy(service.getSoftwareVendors())
    }

    "the software vendor config file exists and is valid" in new Setup {
      when(mockEnvironment.resourceAsStream(eqTo(testFileName)))
        .thenReturn(Some(new FileInputStream(validVendors)))

      service.getSoftwareVendors() mustBe unsortedSoftwareVendors
    }

    "the software vendor config file does not exist" in new Setup {
      when(mockEnvironment.resourceAsStream(eqTo(testFileName)))
        .thenReturn(None)

      intercept[InternalServerException](service.getSoftwareVendors()).message mustBe s"[DataService][jsonFile] - $testFileName not found"
    }

    "the software vendor config file contains invalid json" in new Setup {
      when(mockEnvironment.resourceAsStream(eqTo(testFileName)))
        .thenReturn(Some(new FileInputStream(invalidVendors)))

      intercept[InternalServerException](service.getSoftwareVendors()).message
        .contains("[DataService][softwareVendors] - Json parse failures") mustBe true
    }
  }

  "getOtherSoftware" when {

    "the other software production config file exists and is valid" in new Setup {
      when(mockEnvironment.resourceAsStream(eqTo(testOtherSoftwareFileName)))
        .thenReturn(Some(new FileInputStream(productionOtherSoftware)))

      noException shouldBe thrownBy(service.getOtherSoftware())
    }

    "the other software config file exists and is valid" in new Setup {
      when(mockEnvironment.resourceAsStream(eqTo(testOtherSoftwareFileName)))
        .thenReturn(Some(new FileInputStream(validOtherSoftware)))

      service.getOtherSoftware() mustBe validOtherSoftwareProducts
    }

    "the other software config file does not exist" in new Setup {
      when(mockEnvironment.resourceAsStream(eqTo(testOtherSoftwareFileName)))
        .thenReturn(None)

      intercept[InternalServerException](service.getOtherSoftware()).message mustBe s"[DataService][jsonFile] - $testOtherSoftwareFileName not found"
    }

    "the other software config file contains invalid json" in new Setup {
      when(mockEnvironment.resourceAsStream(eqTo(testOtherSoftwareFileName)))
        .thenReturn(Some(new FileInputStream(invalidOtherSoftware)))

      intercept[InternalServerException](service.getOtherSoftware()).message
        .contains("[DataService][otherSoftware] - Json parse failures") mustBe true
    }
  }
}
