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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.{Available, Intended}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilterGroups.userTypeFilters
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.*

import java.time.LocalDate

class SoftwareChoicesServiceSpec extends PlaySpec with BeforeAndAfterEach {

  private def vendor(filters: Seq[VendorFilter]): SoftwareVendorModel = SoftwareVendorModel(
    name = filters.head.toString,
    email = None,
    phone = None,
    website = "",
    filters = filters.map(vf => vf -> Available).toMap
  )

  private val allVendors = SoftwareVendors(
    lastUpdated = LocalDate.now,
    vendors = userTypeFilters.flatMap { userType => Seq(
      vendor(Seq(userType, QuarterlyUpdates, TaxReturn, SoleTrader, StandardUpdatePeriods)),
      vendor(Seq(userType, QuarterlyUpdates, SoleTrader, StandardUpdatePeriods)),
      vendor(Seq(userType, QuarterlyUpdates, TaxReturn, SoleTrader, UkProperty, StandardUpdatePeriods)),
      vendor(Seq(userType, QuarterlyUpdates, TaxReturn, UkProperty, StandardUpdatePeriods)),
      vendor(Seq(userType, QuarterlyUpdates, TaxReturn, OverseasProperty, StandardUpdatePeriods)),
      vendor(Seq(userType, QuarterlyUpdates, TaxReturn, OverseasProperty, CalendarUpdatePeriods)),
    )}.toSeq ++ Seq(
      vendor(userTypeFilters.toSeq)
    )
  )

  private def intentVendor(name: String, filters: Map[VendorFilter, FeatureStatus]): SoftwareVendorModel = SoftwareVendorModel(
    name = name,
    email = None,
    phone = None,
    website = "",
    filters = filters
  )
 
  val mockDataService = mock[DataService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockDataService.getSoftwareVendors()).thenReturn(
      allVendors
    )
  }

  val service = new SoftwareChoicesService(
    mockDataService
  )

  "getAllInOneVendors" should {
    "exclude vendors that are not for Individual and have both Quarterly Submissions and Tax returns" in {
      val result = service.getAllInOneVendors(Seq(Individual, QuarterlyUpdates, TaxReturn))
      result.vendors.size mustBe 5
    }

    "not ignore question filters" in {
      val result = service.getAllInOneVendors(Seq(Agent, SoleTrader))
      result.vendors.size mustBe 3
    }

    "retain preferences filters" in {
      val result = service.getAllInOneVendors(Seq(Agent, FreeVersion))
      result.vendors.size mustBe 0
    }
  }

  "getOtherVendors" should {
    "exclude vendors that are not for requested user type" in {
      Seq(Individual, Agent).foreach { userType =>
        val result = service.getOtherVendors(Seq(userType, SoleTrader, StandardUpdatePeriods), true)
        result.vendors.size mustBe 3
        result.vendors.head.name mustBe userType.toString
      }
    }

    "returns all vendors so mandatory filters are covered" in {
      Seq(Individual, Agent).foreach { userType =>
        val result = service.getOtherVendors(Seq(userType, SoleTrader, UkProperty, StandardUpdatePeriods), true)
        result.vendors.size mustBe 4
        result.vendors.head.name mustBe userType.toString
      }
    }

    "ignore question filters and excludes all-in-one vendors" in {
      Seq(false, true).foreach { isAgent =>
        val result = service.getOtherVendors(Seq(Agent, SoleTrader, StandardUpdatePeriods), isAgent)
        val expected = if (isAgent) 3 else 0
        result.vendors.size mustBe expected
      }
    }

    "ignores accounting period and mandatory income sources for agent" in {
      val result = service.getOtherVendors(Seq(Agent), true)
      result.vendors.size mustBe 7
    }

    "retain preferences filters" in {
      Seq(false, true).foreach { isAgent =>
        val result = service.getOtherVendors(Seq(Agent, SoleTrader, StandardUpdatePeriods, FreeVersion), isAgent)
        result.vendors.size mustBe 0
      }
    }

    "does not show duplicates" in {
      val result = service.getOtherVendors(Seq(Agent, SoleTrader, UkProperty, StandardUpdatePeriods), true)
      result.vendors.size mustBe 4
    }

    "returns all vendors when no user types are specified" in {
      val result = service.getOtherVendors(Seq(SoleTrader, UkProperty, StandardUpdatePeriods), true)
      result.vendors.size mustBe 13
    }

    "returns vendors that have all user type filters" in {
      val result = service.getOtherVendors(userTypeFilters.toSeq, true)
      result.vendors.size mustBe 1
    }
  }
  
  "getVendorsWithIntent" should {
    "return 1 vendors if everything available and selected" in {
      when(mockDataService.getSoftwareVendors()).thenReturn(
        SoftwareVendors(
          lastUpdated = LocalDate.now,
          vendors = Seq(
            intentVendor("Vendor 01", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor("Vendor 02", Map(
              Individual->Available, Agent->Available, QuarterlyUpdates->Available, TaxReturn->Available,
              SoleTrader->Available, UkProperty->Available, OverseasProperty->Available,
              UkInterest->Available, ConstructionIndustryScheme->Available, Employment->Available, UkDividends->Available,
              StatePensionIncome->Available, PrivatePensionIncome->Available, ForeignDividends->Available, ForeignInterest->Available,
              PaymentsIntoAPrivatePension->Available, CharitableGiving->Available, CapitalGainsTax->Available, StudentLoans->Available,
              MarriageAllowance->Available, VoluntaryClass2NationalInsurance->Available, HighIncomeChildBenefitCharge->Available,
              StandardUpdatePeriods->Available, CalendarUpdatePeriods->Available,
              FreeVersion->Available, Bridging->Available, Vat->Available,
              Visual->Available, Hearing->Available, Motor->Available, Cognitive->Available))
          )
        )
      )
      val result = service.getVendorsWithIntent(
        Seq(Individual, Agent, QuarterlyUpdates, TaxReturn, SoleTrader, UkProperty, OverseasProperty,
          UkInterest, ConstructionIndustryScheme, Employment, UkDividends,
          StatePensionIncome, PrivatePensionIncome, ForeignDividends, ForeignInterest,
          PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans,
          MarriageAllowance, VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge,
          StandardUpdatePeriods, CalendarUpdatePeriods,
          FreeVersion, Bridging, Vat,
          Visual, Hearing, Motor, Cognitive
        ))
      result.size mustBe 1
      result(0).vendor.name mustBe "Vendor 02"
      result(0).quarterlyReady mustBe Some(true)
      result(0).eoyReady mustBe Some(true)
    }
    
    "return vendors with Ready and In Development statuses" in {
      when(mockDataService.getSoftwareVendors()).thenReturn(
        SoftwareVendors(
          lastUpdated = LocalDate.now,
          vendors = Seq(
            intentVendor("Vendor 01", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor("Vendor 02", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Intended, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor("Vendor 03", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Intended, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available))
          )
        )
      )
      val result = service.getVendorsWithIntent(Seq(Individual, TaxReturn, UkProperty, StudentLoans, StandardUpdatePeriods, Visual))
      result.size mustBe 3
      result(0).vendor.name mustBe "Vendor 01"
      result(0).quarterlyReady mustBe Some(true)
      result(0).eoyReady mustBe Some(true)
      result(1).vendor.name mustBe "Vendor 02"
      result(1).quarterlyReady mustBe Some(true)
      result(1).eoyReady mustBe Some(false)
      result(2).vendor.name mustBe "Vendor 03"
      result(2).quarterlyReady mustBe Some(true)
      result(2).eoyReady mustBe Some(false)
    }
    
    "return vendors with Ready and Not included statuses" in {
      when(mockDataService.getSoftwareVendors()).thenReturn(
        SoftwareVendors(
          lastUpdated = LocalDate.now,
          vendors = Seq(
            intentVendor("Vendor 01", Map(Individual->Available, QuarterlyUpdates->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available))
          )
        )
      )
      val result = service.getVendorsWithIntent(Seq(Individual, TaxReturn, UkProperty, StandardUpdatePeriods, Visual))
      result.size mustBe 1
      result(0).vendor.name mustBe "Vendor 01"
      result(0).quarterlyReady mustBe Some(true)
      result(0).eoyReady mustBe None

    }
    
    "return empty Sequence when mandatory filters not done or are intended" in {
      when(mockDataService.getSoftwareVendors()).thenReturn(
        SoftwareVendors(
          lastUpdated = LocalDate.now,
          vendors = Seq(
            intentVendor("Vendor 01", Map(Individual->Intended, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor("Vendor 02", Map(QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor("Vendor 03", Map(Individual->Available, QuarterlyUpdates->Intended, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor("Vendor 04", Map(Individual->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor("Vendor 05", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Intended, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor("Vendor 06", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor("Vendor 07", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Intended, Visual->Available)),
            intentVendor("Vendor 08", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, Visual->Available)),
            intentVendor("Vendor 09", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Intended)),
            intentVendor("Vendor 10", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available))
          )
        )
      )
      val result = service.getVendorsWithIntent(Seq(Individual, UkProperty, StandardUpdatePeriods, Visual))
      result mustBe Seq.empty
    }
  }
}
