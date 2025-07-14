/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers

import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod.SixthAprilToFifthApril
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.SoleTraderOrLandlord
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages._

class ZeroSoftwareResultsControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  def testUserFilters(answers: UserAnswers, filters: Seq[VendorFilter]): UserFilters = UserFilters(SessionId, Some(answers), filters)

  override def beforeEach(): Unit = {
    userFiltersRepository.collection.drop().toFuture()
    super.beforeEach()
  }

  s"GET ${routes.ZeroSoftwareResultsController.show().url}" should {
    s"return $OK" in {
      val result = SoftwareChoicesFrontend.getZeroSoftwareResults()

      result should have(
        httpStatus(OK),
        pageTitle(s"${messages("zero-results.heading")} - Find software that’s compatible with Making Tax Digital for Income Tax - GOV.UK"),
      )
    }
  }

  s"POST ${routes.ZeroSoftwareResultsController.submit().url}" should {
    s"return $SEE_OTHER and remove the filters from page answers except user type" in {
      val userAnswers = UserAnswers()
        .set(UserTypePage, SoleTraderOrLandlord).get
        .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
        .set(AdditionalIncomeSourcesPage, Seq(UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome,
          PrivatePensionIncome, ForeignDividends, ForeignInterest)).get
        .set(OtherItemsPage, Seq(PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans,
          MarriageAllowance, VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge)).get
        .set(AccountingPeriodPage, SixthAprilToFifthApril).get

      val filters = Seq(
        Individual,
        SoleTrader,
        UkProperty,
        OverseasProperty,
        UkInterest,
        ConstructionIndustryScheme,
        Employment,
        UkDividends,
        StatePensionIncome,
        PrivatePensionIncome,
        ForeignDividends,
        ForeignInterest,
        PaymentsIntoAPrivatePension,
        CharitableGiving,
        CapitalGainsTax,
        StudentLoans,
        MarriageAllowance,
        VoluntaryClass2NationalInsurance,
        HighIncomeChildBenefitCharge,
        StandardUpdatePeriods
      )
      await(userFiltersRepository.set(testUserFilters(userAnswers, filters)))

      // Check the vendor filters are set before the submit
      await(getById(SessionId)).get.finalFilters shouldBe filters

      val result = SoftwareChoicesFrontend.postZeroSoftwareResults()

      result should have(
        httpStatus(SEE_OTHER)
      )

      // Check the vendor filters are removed by the submit
      await(getById(SessionId)).get.finalFilters shouldBe Seq(Individual)

    }
  }

}
