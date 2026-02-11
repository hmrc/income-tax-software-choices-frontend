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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.{Available, Intended, NotApplicable}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilterGroups.userTypeFilters
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.requests.SessionDataRequest
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository

import java.time.LocalDate

class SoftwareChoicesServiceSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterEach {

  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val mockRequest: SessionDataRequest[_] = mock[SessionDataRequest[_]]

  private def vendor(productId: Int, filters: Seq[VendorFilter]): SoftwareVendorModel = SoftwareVendorModel(
    productId = productId,
    name = s"Vendor_$productId",
    email = None,
    phone = None,
    website = "",
    filters = filters.map(vf => vf -> Available).toMap
  )

  private val allVendors = SoftwareVendors(
    lastUpdated = LocalDate.now,
    vendors = userTypeFilters.flatMap { userType => Seq(
      vendor(1, Seq(userType, QuarterlyUpdates, TaxReturn, SoleTrader, StandardUpdatePeriods)),
      vendor(2, Seq(userType, QuarterlyUpdates, SoleTrader, StandardUpdatePeriods)),
      vendor(3, Seq(userType, QuarterlyUpdates, TaxReturn, SoleTrader, UkProperty, StandardUpdatePeriods)),
      vendor(4, Seq(userType, QuarterlyUpdates, TaxReturn, UkProperty, StandardUpdatePeriods)),
      vendor(5, Seq(userType, QuarterlyUpdates, TaxReturn, OverseasProperty, StandardUpdatePeriods)),
      vendor(6, Seq(userType, QuarterlyUpdates, TaxReturn, OverseasProperty, CalendarUpdatePeriods)),
    )}.toSeq ++ Seq(
      vendor(7, userTypeFilters.toSeq)
    )
  )

  private def intentVendor(productId: Int, name: String, filters: Map[VendorFilter, FeatureStatus]): SoftwareVendorModel = SoftwareVendorModel(
    productId = productId,
    name = name,
    email = None,
    phone = None,
    website = "",
    filters = filters
  )
 
  val mockDataService = mock[DataService]
  val mockUserFiltersRepo = mock[UserFiltersRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockDataService.getSoftwareVendors()).thenReturn(
      allVendors
    )
    when(mockRequest.userFilters).thenReturn(
      UserFilters(
        id = "test-id",
        answers = None,
        finalFilters = Seq.empty,
        randomVendorOrder = (for (x <- 1 to 20) yield x).toList // doesn't want to random for tests!
      )
    )
  }

  val service = new SoftwareChoicesService(
    mockDataService,
    mockUserFiltersRepo
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

    "randomise the order of vendors when no previous order set" in {
      val result1 = service.getAllInOneVendors(Seq(Individual, QuarterlyUpdates, TaxReturn))
      result1.vendors.map(_.productId) mustBe List(1,3,4,5,6)

      when(mockRequest.userFilters).thenReturn(
        UserFilters(
          id = "test-id",
          answers = None,
          finalFilters = Seq.empty,
          randomVendorOrder = List.empty // should generate a new random order
        )
      )

      val result2 = service.getAllInOneVendors(Seq(Individual, QuarterlyUpdates, TaxReturn))
      result2.vendors.map(_.productId) == result1.vendors.map(_.productId) mustBe false

      result2.vendors.map(_.productId).toSet mustBe result1.vendors.map(_.productId).toSet
    }

    "retain randomised order when filters change" in {
      val result1 = service.getAllInOneVendors(Seq(Individual, QuarterlyUpdates, TaxReturn))
      result1.vendors.map(_.productId) mustBe List(1,3,4,5,6)

      val result5 = service.getAllInOneVendors(Seq(Individual, QuarterlyUpdates, TaxReturn, UkProperty))
      result5.vendors.map(_.productId) mustBe List(3,4)

      val result3 = service.getAllInOneVendors(Seq(Individual, QuarterlyUpdates, TaxReturn))
      result3.vendors.map(_.productId) mustBe List(1,3,4,5,6)
    }
  }

  "getVendorsWithIntent" should {
    "return 1 vendors if everything available and selected" in {
      when(mockDataService.getSoftwareVendors()).thenReturn(
        SoftwareVendors(
          lastUpdated = LocalDate.now,
          vendors = Seq(
            intentVendor(1, "Vendor 01", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor(2, "Vendor 02", Map(
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
        ))(appConfig)
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
            intentVendor(1, "Vendor 01", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor(2, "Vendor 02", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Intended, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor(3, "Vendor 03", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Intended, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available))
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
            intentVendor(1, "Vendor 01", Map(Individual->Available, QuarterlyUpdates->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available))
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
            intentVendor(1, "Vendor 01", Map(Individual->Intended, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor(2, "Vendor 02", Map(QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor(3, "Vendor 03", Map(Individual->Available, QuarterlyUpdates->Intended, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor(4, "Vendor 04", Map(Individual->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor(5, "Vendor 05", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Intended, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor(6, "Vendor 06", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available)),
            intentVendor(7, "Vendor 07", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Intended, Visual->Available)),
            intentVendor(8, "Vendor 08", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, Visual->Available)),
            intentVendor(9, "Vendor 09", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Intended)),
            intentVendor(10, "Vendor 10", Map(Individual->Available, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available)),
            intentVendor(11, "Vendor 11", Map(Individual->NotApplicable, QuarterlyUpdates->Available, TaxReturn->Available, UkProperty->Available, StudentLoans->Available, StandardUpdatePeriods->Available, Visual->Available))
          )
        )
      )
      val result = service.getVendorsWithIntent(Seq(Individual, UkProperty, StandardUpdatePeriods, Visual))
      result mustBe Seq.empty
    }

    "return 1 vendor if Partner Income is also selected" in {
      when(mockDataService.getSoftwareVendors()).thenReturn(
        SoftwareVendors(
          lastUpdated = LocalDate.now,
          vendors = Seq(
            intentVendor(1, "Vendor 01", Map(Individual -> Available, QuarterlyUpdates -> Available, TaxReturn -> Available, UkProperty -> Available, StudentLoans -> Available, StandardUpdatePeriods -> Available, Visual -> Available)),
            intentVendor(2, "Vendor 02", Map(
              Individual -> Available, Agent -> Available, QuarterlyUpdates -> Available, TaxReturn -> Available,
              SoleTrader -> Available, UkProperty -> Available, OverseasProperty -> Available,
              UkInterest -> Available, ConstructionIndustryScheme -> Available, Employment -> Available, UkDividends -> Available,
              StatePensionIncome -> Available, PrivatePensionIncome -> Available, ForeignDividends -> Available, ForeignInterest -> Available,
              PaymentsIntoAPrivatePension -> Available, CharitableGiving -> Available, CapitalGainsTax -> Available, StudentLoans -> Available,
              MarriageAllowance -> Available, VoluntaryClass2NationalInsurance -> Available, HighIncomeChildBenefitCharge -> Available,
              StandardUpdatePeriods -> Available, CalendarUpdatePeriods -> Available,
              FreeVersion -> Available, Bridging -> Available, Vat -> Available,
              Visual -> Available, Hearing -> Available, Motor -> Available, Cognitive -> Available)),
            intentVendor(3, "Vendor 03", Map(
              Individual -> Available, Agent -> Available, QuarterlyUpdates -> Available, TaxReturn -> Available,
              SoleTrader -> Available, UkProperty -> Available, OverseasProperty -> Available,
              UkInterest -> Available, ConstructionIndustryScheme -> Available, Employment -> Available, UkDividends -> Available,
              StatePensionIncome -> Available, PrivatePensionIncome -> Available, ForeignDividends -> Available, ForeignInterest -> Available,
              PaymentsIntoAPrivatePension -> Available, CharitableGiving -> Available, CapitalGainsTax -> Available, StudentLoans -> Available,
              MarriageAllowance -> Available, VoluntaryClass2NationalInsurance -> Available, HighIncomeChildBenefitCharge -> Available,
              StandardUpdatePeriods -> Available, CalendarUpdatePeriods -> Available,
              PartnerIncome -> Available, TrustIncome -> Available, FosterCarer -> Available, AveragingAdjustment -> Available,
              FreeVersion -> Available, Bridging -> Available, Vat -> Available,
              Visual -> Available, Hearing -> Available, Motor -> Available, Cognitive -> Available))
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
          PartnerIncome, 
          FreeVersion, Bridging, Vat,
          Visual, Hearing, Motor, Cognitive
        ))
      result.size mustBe 1
      result(0).vendor.name mustBe "Vendor 03"
      result(0).quarterlyReady mustBe Some(true)
      result(0).eoyReady mustBe Some(true)
    }

    "return 1 vendor if Foster Carer is also selected" in {
      when(mockDataService.getSoftwareVendors()).thenReturn(
        SoftwareVendors(
          lastUpdated = LocalDate.now,
          vendors = Seq(
            intentVendor(1, "Vendor 01", Map(Individual -> Available, QuarterlyUpdates -> Available, TaxReturn -> Available, UkProperty -> Available, StudentLoans -> Available, StandardUpdatePeriods -> Available, Visual -> Available)),
            intentVendor(2, "Vendor 02", Map(
              Individual -> Available, Agent -> Available, QuarterlyUpdates -> Available, TaxReturn -> Available,
              SoleTrader -> Available, UkProperty -> Available, OverseasProperty -> Available,
              UkInterest -> Available, ConstructionIndustryScheme -> Available, Employment -> Available, UkDividends -> Available,
              StatePensionIncome -> Available, PrivatePensionIncome -> Available, ForeignDividends -> Available, ForeignInterest -> Available,
              PaymentsIntoAPrivatePension -> Available, CharitableGiving -> Available, CapitalGainsTax -> Available, StudentLoans -> Available,
              MarriageAllowance -> Available, VoluntaryClass2NationalInsurance -> Available, HighIncomeChildBenefitCharge -> Available,
              StandardUpdatePeriods -> Available, CalendarUpdatePeriods -> Available,
              FreeVersion -> Available, Bridging -> Available, Vat -> Available,
              Visual -> Available, Hearing -> Available, Motor -> Available, Cognitive -> Available)),
            intentVendor(3, "Vendor 03", Map(
              Individual -> Available, Agent -> Available, QuarterlyUpdates -> Available, TaxReturn -> Available,
              SoleTrader -> Available, UkProperty -> Available, OverseasProperty -> Available,
              UkInterest -> Available, ConstructionIndustryScheme -> Available, Employment -> Available, UkDividends -> Available,
              StatePensionIncome -> Available, PrivatePensionIncome -> Available, ForeignDividends -> Available, ForeignInterest -> Available,
              PaymentsIntoAPrivatePension -> Available, CharitableGiving -> Available, CapitalGainsTax -> Available, StudentLoans -> Available,
              MarriageAllowance -> Available, VoluntaryClass2NationalInsurance -> Available, HighIncomeChildBenefitCharge -> Available,
              StandardUpdatePeriods -> Available, CalendarUpdatePeriods -> Available,
              PartnerIncome -> Available, TrustIncome -> Available, FosterCarer -> Available, AveragingAdjustment -> Available,
              FreeVersion -> Available, Bridging -> Available, Vat -> Available,
              Visual -> Available, Hearing -> Available, Motor -> Available, Cognitive -> Available))
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
          FosterCarer,
          FreeVersion, Bridging, Vat,
          Visual, Hearing, Motor, Cognitive
        ))
      result.size mustBe 1
      result(0).vendor.name mustBe "Vendor 03"
      result(0).quarterlyReady mustBe Some(true)
      result(0).eoyReady mustBe Some(true)
    }

    "return 1 vendor if Trustee Income is also selected" in {
      when(mockDataService.getSoftwareVendors()).thenReturn(
        SoftwareVendors(
          lastUpdated = LocalDate.now,
          vendors = Seq(
            intentVendor(1, "Vendor 01", Map(Individual -> Available, QuarterlyUpdates -> Available, TaxReturn -> Available, UkProperty -> Available, StudentLoans -> Available, StandardUpdatePeriods -> Available, Visual -> Available)),
            intentVendor(2, "Vendor 02", Map(
              Individual -> Available, Agent -> Available, QuarterlyUpdates -> Available, TaxReturn -> Available,
              SoleTrader -> Available, UkProperty -> Available, OverseasProperty -> Available,
              UkInterest -> Available, ConstructionIndustryScheme -> Available, Employment -> Available, UkDividends -> Available,
              StatePensionIncome -> Available, PrivatePensionIncome -> Available, ForeignDividends -> Available, ForeignInterest -> Available,
              PaymentsIntoAPrivatePension -> Available, CharitableGiving -> Available, CapitalGainsTax -> Available, StudentLoans -> Available,
              MarriageAllowance -> Available, VoluntaryClass2NationalInsurance -> Available, HighIncomeChildBenefitCharge -> Available,
              StandardUpdatePeriods -> Available, CalendarUpdatePeriods -> Available,
              FreeVersion -> Available, Bridging -> Available, Vat -> Available,
              Visual -> Available, Hearing -> Available, Motor -> Available, Cognitive -> Available)),
            intentVendor(3, "Vendor 03", Map(
              Individual -> Available, Agent -> Available, QuarterlyUpdates -> Available, TaxReturn -> Available,
              SoleTrader -> Available, UkProperty -> Available, OverseasProperty -> Available,
              UkInterest -> Available, ConstructionIndustryScheme -> Available, Employment -> Available, UkDividends -> Available,
              StatePensionIncome -> Available, PrivatePensionIncome -> Available, ForeignDividends -> Available, ForeignInterest -> Available,
              PaymentsIntoAPrivatePension -> Available, CharitableGiving -> Available, CapitalGainsTax -> Available, StudentLoans -> Available,
              MarriageAllowance -> Available, VoluntaryClass2NationalInsurance -> Available, HighIncomeChildBenefitCharge -> Available,
              StandardUpdatePeriods -> Available, CalendarUpdatePeriods -> Available,
              PartnerIncome -> Available, TrustIncome -> Available, FosterCarer -> Available, AveragingAdjustment -> Available,
              FreeVersion -> Available, Bridging -> Available, Vat -> Available,
              Visual -> Available, Hearing -> Available, Motor -> Available, Cognitive -> Available))
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
          TrustIncome,
          FreeVersion, Bridging, Vat,
          Visual, Hearing, Motor, Cognitive
        ))
      result.size mustBe 1
      result(0).vendor.name mustBe "Vendor 03"
      result(0).quarterlyReady mustBe Some(true)
      result(0).eoyReady mustBe Some(true)
    }

    "return 2 vendors if Averaging Adjustment is selected" in {
      when(mockDataService.getSoftwareVendors()).thenReturn(
        SoftwareVendors(
          lastUpdated = LocalDate.now,
          vendors = Seq(
            intentVendor(1, "Vendor 01", Map(Individual -> Available, QuarterlyUpdates -> Available, TaxReturn -> Available, UkProperty -> Available, StudentLoans -> Available, StandardUpdatePeriods -> Available, Visual -> Available)),
            intentVendor(2, "Vendor 02", Map(
              Individual -> Available, Agent -> Available, QuarterlyUpdates -> Available, TaxReturn -> Available,
              SoleTrader -> Available, UkProperty -> Available, OverseasProperty -> Available,
              UkInterest -> Available, ConstructionIndustryScheme -> Available, Employment -> Available, UkDividends -> Available,
              StatePensionIncome -> Available, PrivatePensionIncome -> Available, ForeignDividends -> Available, ForeignInterest -> Available,
              PaymentsIntoAPrivatePension -> Available, CharitableGiving -> Available, CapitalGainsTax -> Available, StudentLoans -> Available,
              MarriageAllowance -> Available, VoluntaryClass2NationalInsurance -> Available, HighIncomeChildBenefitCharge -> Available,
              StandardUpdatePeriods -> Available, CalendarUpdatePeriods -> Available,
              AveragingAdjustment -> Available,
              FreeVersion -> Available, Bridging -> Available, Vat -> Available,
              Visual -> Available, Hearing -> Available, Motor -> Available, Cognitive -> Available)),
            intentVendor(3, "Vendor 03", Map(
              Individual -> Available, Agent -> Available, QuarterlyUpdates -> Available, TaxReturn -> Available,
              SoleTrader -> Available, UkProperty -> Available, OverseasProperty -> Available,
              UkInterest -> Available, ConstructionIndustryScheme -> Available, Employment -> Available, UkDividends -> Available,
              StatePensionIncome -> Available, PrivatePensionIncome -> Available, ForeignDividends -> Available, ForeignInterest -> Available,
              PaymentsIntoAPrivatePension -> Available, CharitableGiving -> Available, CapitalGainsTax -> Available, StudentLoans -> Available,
              MarriageAllowance -> Available, VoluntaryClass2NationalInsurance -> Available, HighIncomeChildBenefitCharge -> Available,
              StandardUpdatePeriods -> Available, CalendarUpdatePeriods -> Available,
              PartnerIncome -> Available, TrustIncome -> Available, FosterCarer -> Available, AveragingAdjustment -> Available,
              FreeVersion -> Available, Bridging -> Available, Vat -> Available,
              Visual -> Available, Hearing -> Available, Motor -> Available, Cognitive -> Available))
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
          AveragingAdjustment,
          FreeVersion, Bridging, Vat,
          Visual, Hearing, Motor, Cognitive
        ))
      result.size mustBe 2
      result(0).vendor.name mustBe "Vendor 02"
      result(0).quarterlyReady mustBe Some(true)
      result(0).eoyReady mustBe Some(true)
    }
  }
}
