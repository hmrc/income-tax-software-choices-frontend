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

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsPath, Reads}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod.FirstAprilToThirtyFirstMarch
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.SoleTraderOrLandlord
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters, UserType, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository

import scala.concurrent.{ExecutionContext, Future}

class PageAnswersServiceSpec extends PlaySpec with BeforeAndAfterEach {

  private case object DummyPage extends QuestionPage[String] {
    override def toString: String = "dummy"
    override def path: JsPath = JsPath \ toString
    override def toVendorFilter(value: String): Seq[VendorFilter] = Seq.empty
    override def reads: Reads[String] = implicitly
  }

  private val sessionId: String = "sessionId"
  private val dummyUserAnswers: UserAnswers = UserAnswers().set(DummyPage, "Test").get
  private val vendorFilterUserAnswers: UserAnswers = UserAnswers().set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
  private val mixedUserAnswers: UserAnswers = UserAnswers()
    .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
    .set(DummyPage, "Test").get
  private val agentMixedUserAnswers: UserAnswers = UserAnswers()
    .set(UserTypePage, UserType.Agent).get
    .set(BusinessIncomePage, Seq(SoleTrader)).get
    .set(OtherItemsPage, Seq(CharitableGiving)).get
  private val fullUserAnswers: UserAnswers = UserAnswers()
    .set(UserTypePage, SoleTraderOrLandlord).get
    .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
    .set(AdditionalIncomeSourcesPage, Seq(UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome,
      PrivatePensionIncome, ForeignDividends, ForeignInterest)).get
    .set(OtherItemsPage, Seq(PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans,
      MarriageAllowance, VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge)).get
    .set(AccountingPeriodPage, FirstAprilToThirtyFirstMarch).get
  private val emptyUserFilter = UserFilters(sessionId, None, Seq.empty)
  private val userFilterWithAnswerForPage = UserFilters(sessionId, Some(dummyUserAnswers), Seq.empty)
  private val userFilterWithVendorFilterAnswerForPage = UserFilters(sessionId, Some(vendorFilterUserAnswers), Seq.empty)
  private val userFilterWithMixedAnswersForPage = UserFilters(sessionId, Some(mixedUserAnswers), Seq.empty)
  private val userFilterWithFullAnswersForPage = UserFilters(sessionId, Some(fullUserAnswers), Seq.empty)
  private val userFilterWithFullFinalFilters = UserFilters(sessionId, Some(fullUserAnswers),
    finalFilters = Seq(Individual, SoleTrader, UkProperty, OverseasProperty, UkInterest,
      ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome, PrivatePensionIncome, ForeignDividends,
      ForeignInterest, PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans, MarriageAllowance,
      VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge, CalendarUpdatePeriods))
  private val agentUserFilter = UserFilters(sessionId, Some(agentMixedUserAnswers), Seq(Agent, SoleTrader, CharitableGiving))

  class Setup {
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
    val mockUserFiltersRepository: UserFiltersRepository = mock[UserFiltersRepository]

    lazy val service: PageAnswersService = new PageAnswersService(mockUserFiltersRepository, ec)
  }

  "getPageAnswers" when {
    "the page has not been visited previously" must {
      "return None if no user filters exists for this session" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(None))

        await(service.getPageAnswers(sessionId, DummyPage)) mustBe None
      }

      "return None if user filters exists for this session but the page does not" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(Some(emptyUserFilter)))

        await(service.getPageAnswers(sessionId, DummyPage)) mustBe None
      }
    }
    "the page has been visited previously" must {
      "return answers if user filters exists for this session and the page has answers" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(Some(userFilterWithAnswerForPage)))

        await(service.getPageAnswers(sessionId, DummyPage)) mustBe Some("Test")
      }
    }
  }

  "setPageAnswers" when {
    "the userFilters exist" must {
      "return true to show the answers have been saved" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(Some(userFilterWithAnswerForPage)))
        when(mockUserFiltersRepository.set(any()))
          .thenReturn(Future.successful(true))

        await(service.setPageAnswers(sessionId, DummyPage, "Another Test String")) mustBe true
      }
    }
    "the userFilters do not exist" must {
      "return true to show the answers have been saved once the user filters created" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(None))
        when(mockUserFiltersRepository.set(any()))
          .thenReturn(Future.successful(true))

        await(service.setPageAnswers(sessionId, DummyPage, "Third Test String")) mustBe true
      }
    }
  }

  "saveFiltersFromAnswers" when {
    "the userFilters exist" must {
      "return correct sequence of vendors for the answers that have been saved" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(Some(userFilterWithVendorFilterAnswerForPage)))
        when(mockUserFiltersRepository.set(any()))
          .thenReturn(Future.successful(true))

        await(service.saveFiltersFromAnswers(sessionId)) mustBe Seq(SoleTrader, UkProperty, OverseasProperty)
      }
      "return correct sequence of vendors when all filter have been selected" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(Some(userFilterWithFullAnswersForPage)))
        when(mockUserFiltersRepository.set(any()))
          .thenReturn(Future.successful(true))

        await(service.saveFiltersFromAnswers(sessionId)) mustBe Seq(Individual, SoleTrader, UkProperty, OverseasProperty, UkInterest,
          ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome, PrivatePensionIncome, ForeignDividends,
          ForeignInterest, PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans, MarriageAllowance,
          VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge, CalendarUpdatePeriods)
      }
      "return correct sequence of vendors for the answers that have been saved ignoring not VendorFilter questions" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(Some(userFilterWithMixedAnswersForPage)))
        when(mockUserFiltersRepository.set(any()))
          .thenReturn(Future.successful(true))

        await(service.saveFiltersFromAnswers(sessionId)) mustBe Seq(SoleTrader, UkProperty, OverseasProperty)
      }
    }
    "the userFilters do not exist" must {
      "return and empty sequence to show there no filters saved" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(None))
        when(mockUserFiltersRepository.set(any()))
          .thenReturn(Future.successful(true))

        await(service.saveFiltersFromAnswers(sessionId)) mustBe Seq.empty
      }
    }

  }

  "removePageFilters" when {
    "the userFilters exists" must {
      "return true to show the filters have been removed except Individual" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(Some(userFilterWithFullFinalFilters)))
        when(mockUserFiltersRepository.set(userFilterWithFullFinalFilters.copy(finalFilters = Seq(Individual))))
          .thenReturn(Future.successful(true))

        await(service.removePageFilters(sessionId)) mustBe true
      }
    }
    "the userFilters do not exist" must {
      "return false to show no filters were removed" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(None))

        await(service.removePageFilters(sessionId)) mustBe false
      }
    }
  }

}
