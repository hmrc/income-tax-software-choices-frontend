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
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.SessionId
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.{ComponentSpecBase, DatabaseHelper}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.Check
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.{FutureVendor, Recognised, Spreadsheet}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{JourneyType, SoftwareProduct, UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.DataService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.PageContentBase

class EnterSoftwareNameControllerISpec extends ComponentSpecBase with BeforeAndAfterEach with DatabaseHelper {

  def testUserFilters(answers: UserAnswers): UserFilters = UserFilters(SessionId, Some(answers))

  private val firstOtherSpreadsheetProduct = SoftwareProduct(1001, "Microsoft Excel", Spreadsheet)


  override def beforeEach(): Unit = {
    await(userFiltersRepository.collection.drop().toFuture())
    super.beforeEach()
  }

  private val dataService: DataService = app.injector.instanceOf[DataService]
  private val recognisedProducts = dataService.getSoftwareVendors().vendors.map(v => SoftwareProduct(v.productId, v.name, Recognised))

  private val futureProducts = dataService.getOtherSoftware().filter(_.softwareType.eq(FutureVendor))
  private val spreadsheetProducts = dataService.getOtherSoftware().filter(_.softwareType.eq(Spreadsheet))

  s"GET ${routes.EnterSoftwareNameController.show()}" when {
    "there is nothing saved in the database for this user" should {
      "redirect to the service index" in {
        val res = SoftwareChoicesFrontend.getEnterSoftwareName()

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }

    "there are pre-existing page answers but not for this page" should {
      "display the page" in {
        val userAnswers = UserAnswers().set(HowYouFindSoftwarePage, Check).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getEnterSoftwareName()

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("enter-software-name.heading")} - ${PageContentBase.title} - GOV.UK"),
          autocompleteSelected("")

        )
      }
    }

    "there are pre-existing page answers including for this page" should {
      "display the page with pre-selection" in {
        val userAnswers = UserAnswers().set(HowYouFindSoftwarePage, Check).get
                                        .set(EnterSoftwareNamePage, firstOtherSpreadsheetProduct).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.getEnterSoftwareName()

        res should have(
          httpStatus(OK),
          pageTitle(s"${messages("enter-software-name.heading")} - ${PageContentBase.title} - GOV.UK"),
          autocompleteSelected(firstOtherSpreadsheetProduct.name)
        )
      }
    }

  }
  s"POST ${routes.EnterSoftwareNameController.submit()}" when {
    "there is nothing saved in the database for this user" should {
      "redirect to the service index" in {
        val res = SoftwareChoicesFrontend.postEnterSoftwareName(None)

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.IndexController.index.url)
        )
      }
    }

    "user selects Recognised product" must {
      s"return $SEE_OTHER and save page answer" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get

        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.postEnterSoftwareName(Some(recognisedProducts.head.productId))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.UserTypeController.show().url)
        )

        getPageData(SessionId, EnterSoftwareNamePage).map(_.name) shouldBe Some(recognisedProducts.head.name)

      }
    }

    "user selects Spreadsheet product" must {
      s"return $SEE_OTHER and save page answer" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get

        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.postEnterSoftwareName(Some(spreadsheetProducts.head.productId))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.NeedAdditionalSoftwareController.show().url)
        )

        getPageData(SessionId, EnterSoftwareNamePage).map(_.name) shouldBe Some(spreadsheetProducts.head.name)

      }
    }

    "user selects Future Vendor product" must {
      s"return $SEE_OTHER and save page answer" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get

        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.postEnterSoftwareName(Some(futureProducts.head.productId))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SoftwareInDevelopmentController.show().url)
        )

        getPageData(SessionId, EnterSoftwareNamePage).map(_.name) shouldBe Some(futureProducts.head.name)

      }
    }

    "user selects Unrecognised product" must {
      s"return $SEE_OTHER and save page answer" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get

        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.postEnterSoftwareName(Some(-1))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.NoSoftwareListedController.show().url)
        )

        getPageData(SessionId, EnterSoftwareNamePage).map(_.name) shouldBe Some("Unknown")

      }
    }

    "user has preexisting UserAnswers but updates software choice" must {
      s"return $SEE_OTHER and update page answer" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get
          .set(EnterSoftwareNamePage, recognisedProducts.head).get
        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.postEnterSoftwareName(Some(spreadsheetProducts.head.productId))

        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.NeedAdditionalSoftwareController.show().url)
        )

        getPageData(SessionId, EnterSoftwareNamePage).map(_.name) shouldBe Some(spreadsheetProducts.head.name)

      }
    }

    "return BAD_REQUEST" when {
      "no answer is given" in {
        val userAnswers = UserAnswers()
          .set(HowYouFindSoftwarePage, Check).get

        await(userFiltersRepository.set(testUserFilters(userAnswers)))

        val res = SoftwareChoicesFrontend.postEnterSoftwareName(None)

        res should have(
          httpStatus(BAD_REQUEST)
        )
        getPageData(SessionId, EnterSoftwareNamePage.toString).size shouldBe 0
      }
    }
  }


}
