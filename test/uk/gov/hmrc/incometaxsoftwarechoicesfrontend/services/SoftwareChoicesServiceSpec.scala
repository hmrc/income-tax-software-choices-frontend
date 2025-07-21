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

import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, SoftwareVendors, VendorFilter}

import java.time.LocalDate

class SoftwareChoicesServiceSpec extends PlaySpec with BeforeAndAfterEach {

  private def vendor(filters: Seq[VendorFilter]): SoftwareVendorModel = SoftwareVendorModel(
    name = filters.head.toString,
    email = None,
    phone = None,
    website = "",
    filters = filters
  )

  "Correctly filters software vendors" should {
    "exclude vendors that are not for requested user type" in {
      val allVendors = SoftwareVendors(
        lastUpdated = LocalDate.now,
        vendors = Seq(
          vendor(Seq(Agent)),
          vendor(Seq(Individual))
        )
      )

      val mockDataService = mock[DataService]

      when(mockDataService.getSoftwareVendors()).thenReturn(
        allVendors
      )

      val service = new SoftwareChoicesService(
        mockDataService
      )

      Seq(Agent, Individual).foreach { userType =>
        val result = service.getAllInOneVendors(Seq(userType))
        result.vendors.size mustBe 1
        result.vendors.head.name mustBe userType.toString
      }
    }

    "ignores question filters in other list" should {
      val allVendors = SoftwareVendors(
        lastUpdated = LocalDate.now,
        vendors = Seq(
          vendor(Seq(Agent, SoleTrader)),
          vendor(Seq(Agent))
        )
      )

      val mockDataService = mock[DataService]

      when(mockDataService.getSoftwareVendors()).thenReturn(
        allVendors
      )

      val service = new SoftwareChoicesService(
        mockDataService
      )

      val filters = Seq(Agent, SoleTrader)

      "allInOneSoftware" in {
        val allInOne = service.getAllInOneVendors(filters)
        allInOne.vendors.size mustBe 1
      }

      "otherSoftware" in {
        val other = service.getOtherVendors(filters)
        other.vendors.size mustBe 2
      }
    }

    "Retains preferences filters" should {
      val allVendors = SoftwareVendors(
        lastUpdated = LocalDate.now,
        vendors = Seq(
          vendor(Seq(Agent, SoleTrader)),
          vendor(Seq(Agent))
        )
      )

      val mockDataService = mock[DataService]

      when(mockDataService.getSoftwareVendors()).thenReturn(
        allVendors
      )

      val service = new SoftwareChoicesService(
        mockDataService
      )

      val filters = Seq(Agent, FreeVersion)

      "allInOneSoftware" in {
        val allInOne = service.getAllInOneVendors(filters)
        allInOne.vendors.size mustBe 0
      }

      "otherSoftware" in {
        val other = service.getOtherVendors(filters)
        other.vendors.size mustBe 0
      }
    }
  }
}
