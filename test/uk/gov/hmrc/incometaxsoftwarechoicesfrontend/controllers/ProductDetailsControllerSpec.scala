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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers

import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.mocks.MockSessionIdentifierAction
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.AccountingPeriod.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.Check
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.Recognised
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareProduct, UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.SoleTraderOrLandlord
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.{PageAnswersService, SoftwareChoicesService}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.{NotFoundView, ProductDetailsPersonalisedView, ProductDetailsView}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository

import scala.concurrent.Future

class ProductDetailsControllerSpec extends ControllerBaseSpec
  with MockSessionIdentifierAction
  with BeforeAndAfterEach {

  private val productDetailsView = app.injector.instanceOf[ProductDetailsView]
  private val personalisedView = app.injector.instanceOf[ProductDetailsPersonalisedView]
  private val notFoundView = app.injector.instanceOf[NotFoundView]
  private val softwareChoicesService = app.injector.instanceOf[SoftwareChoicesService]
  private val pageAnswersService = app.injector.instanceOf[PageAnswersService]
  val mockUserFiltersRepo = mock[UserFiltersRepository]

  private val recognisedProduct = SoftwareProduct(3, "Vendor 03", Recognised)
  private val fullUserAnswers: UserAnswers = UserAnswers()
    .set(HowYouFindSoftwarePage, Check).get
    .set(EnterSoftwareNamePage, recognisedProduct).get
    .set(UserTypePage, SoleTraderOrLandlord).get
    .set(BusinessIncomePage, Seq(SoleTrader, UkProperty, OverseasProperty)).get
    .set(AdditionalIncomeSourcesPage, Seq(UkInterest, ConstructionIndustryScheme, Employment, UkDividends, StatePensionIncome,
      PrivatePensionIncome, ForeignDividends, ForeignInterest)).get
    .set(OtherItemsPage, Seq(PaymentsIntoAPrivatePension, CharitableGiving, CapitalGainsTax, StudentLoans,
      MarriageAllowance, VoluntaryClass2NationalInsurance, HighIncomeChildBenefitCharge)).get
    .set(AccountingPeriodPage, FirstAprilToThirtyFirstMarch).get
  private val userFilterWithFullAnswersForPage = UserFilters(sessionId, Some(fullUserAnswers), Seq.empty)


  "Show" when {
    "there are no user answers" should {
      when(mockUserFiltersRepo.get(any())).thenReturn(Future.successful(None))

      "a valid param has been passed" should {
        "return OK status with the product details page" in withController { controller =>
          val result = controller.show("101")(fakeRequest)

          status(result) shouldBe Status.OK
        }
      }
      "an invalid param has been passed" when {
        "return NotFound status with the Not Found view" in withController { controller =>
          val result = controller.show("vendor1")(fakeRequest)

          status(result) shouldBe Status.NOT_FOUND
        }
      }
      "a non existent vendor id has been passed" when {
        "return NotFound status with the Not Found view" in withController { controller =>
          val result = controller.show("2")(fakeRequest)

          status(result) shouldBe Status.NOT_FOUND
        }
      }
    }
    "there are User answers for Product and Journey" should {
      when(mockUserFiltersRepo.get(any())).thenReturn(Future.successful(Some(userFilterWithFullAnswersForPage)))

      "a valid param has been passed" should {
        "return OK status with the product details page" in withController { controller =>
          val result = controller.show("101")(fakeRequest)

          status(result) shouldBe Status.OK
        }
      }
      "an invalid param has been passed" when {
        "return NotFound status with the Not Found view" in withController { controller =>
          val result = controller.show("vendor1")(fakeRequest)

          status(result) shouldBe Status.NOT_FOUND
        }
      }
      "a non existent vendor id has been passed" when {
        "return NotFound status with the Not Found view" in withController { controller =>
          val result = controller.show("2")(fakeRequest)

          status(result) shouldBe Status.NOT_FOUND
        }
      }
    }
  }

  private def withController(testCode: ProductDetailsController => Any): Unit = {
    val controller = new ProductDetailsController(
      softwareChoicesService,
      mockUserFiltersRepo,
      pageAnswersService,
      fakeSessionIdentifierAction,
      productDetailsView,
      personalisedView,
      notFoundView
    )(mcc, appConfig, ec)

    testCode(controller)
  }

}
