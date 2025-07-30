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

  val allVendors = SoftwareVendors(
    lastUpdated = LocalDate.now,
    vendors = Seq(
      vendor(Seq(Agent)),
      vendor(Seq(Agent, SoleTrader)),
      vendor(Seq(Individual)),
      vendor(Seq(Individual, SoleTrader)),
    )
  )

  val mockDataService = mock[DataService]

  when(mockDataService.getSoftwareVendors()).thenReturn(
    allVendors
  )

  val service = new SoftwareChoicesService(
    mockDataService
  )

  "getAllInOneVendors" should {
    "exclude vendors that are not for requested user type" in {
      Seq(Agent, Individual).foreach { userType =>
        val result = service.getAllInOneVendors(Seq(userType))
        result.vendors.size mustBe 2
        result.vendors.head.name mustBe userType.toString
      }
    }

    "not ignore question filters" in {
      val result = service.getAllInOneVendors(Seq(Agent, SoleTrader))
      result.vendors.size mustBe 1
    }

    "retain preferences filters" in {
      val result = service.getAllInOneVendors(Seq(Agent, FreeVersion))
      result.vendors.size mustBe 0
    }
  }

  "getOtherVendors" should {
    "exclude vendors that are not for requested user type" in {
      Seq(Agent, Individual).foreach { userType =>
        val isAgent = userType == Agent
        val result = service.getOtherVendors(Seq(userType), isAgent)
        val expected = if (isAgent) 2 else 0
        result.vendors.size mustBe expected
        if (expected != 0) {
          result.vendors.head.name mustBe userType.toString
        }
      }
    }

    "ignore question filters and excludes all-in-one vendors" in {
      Seq(false, true).foreach { isAgent =>
        val result = service.getOtherVendors(Seq(Agent, SoleTrader), isAgent)
        val expected = if (isAgent) 2 else 1
        result.vendors.size mustBe expected
      }
    }

    "retain preferences filters" in {
      Seq(false, true).foreach { isAgent =>
        val result = service.getOtherVendors(Seq(Agent, FreeVersion), isAgent)
        result.vendors.size mustBe 0
      }
    }
  }
}
