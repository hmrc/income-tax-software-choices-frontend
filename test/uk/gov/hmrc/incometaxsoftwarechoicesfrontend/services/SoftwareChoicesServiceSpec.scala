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

  val mockDataService = mock[DataService]

  private def vendor(filters: Seq[VendorFilter]): SoftwareVendorModel = SoftwareVendorModel(
    name = filters.head.toString,
    email = None,
    phone = None,
    website = "",
    filters = filters
  )

  val service = new SoftwareChoicesService(
    mockDataService
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

      when(mockDataService.getSoftwareVendors()).thenReturn(
        allVendors
      )

      Seq(Agent, Individual).foreach { userType =>
        val result = service.getAllInOneVendors(Seq(userType))
        result.vendors.size mustBe 1
        result.vendors.head.name mustBe userType.toString
      }
    }

    "ignores question filters in other list" in {
      val allVendors = SoftwareVendors(
        lastUpdated = LocalDate.now,
        vendors = Seq(
          vendor(Seq(Agent, SoleTrader)),
          vendor(Seq(Agent))
        )
      )

      when(mockDataService.getSoftwareVendors()).thenReturn(
        allVendors
      )

      val filters = Seq(Agent, SoleTrader)
      val allInOne = service.getAllInOneVendors(filters)
      val other = service.getOtherVendors(filters)
      allInOne.vendors.size mustBe 1
      other.vendors.size mustBe 2
    }

    "Retains preferences filters" in {
      val allVendors = SoftwareVendors(
        lastUpdated = LocalDate.now,
        vendors = Seq(
          vendor(Seq(Agent, SoleTrader)),
          vendor(Seq(Agent))
        )
      )

      when(mockDataService.getSoftwareVendors()).thenReturn(
        allVendors
      )

      val filters = Seq(Agent, FreeVersion)
      val allInOne = service.getAllInOneVendors(filters)
      val other = service.getOtherVendors(filters)
      allInOne.vendors.size mustBe 0
      other.vendors.size mustBe 0
    }
  }
}
