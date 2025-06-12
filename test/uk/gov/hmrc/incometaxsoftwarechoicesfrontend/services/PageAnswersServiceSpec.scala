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
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.{ForeignIncome, OverseasProperty}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, SoftwareVendors, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository

import scala.concurrent.{ExecutionContext, Future}

class PageAnswersServiceSpec extends PlaySpec with BeforeAndAfterEach {

  private val pageId: String = "my-page"
  private val sessionId: String = "sessionId"
  private val emptyUserFilter = UserFilters(sessionId, Map.empty, Seq.empty)
  private val userFilterNoAnswerForPage = UserFilters(sessionId, Map(pageId -> Seq.empty), Seq.empty)
  private val userFilterWithAnswerForPage = UserFilters(sessionId, Map(pageId -> Seq(OverseasProperty)), Seq.empty)

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

        await(service.getPageAnswers(sessionId, pageId)) mustBe None
      }

      "return None if user filters exists for this session but the page does not" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(Some(emptyUserFilter)))

        await(service.getPageAnswers(sessionId, pageId)) mustBe None
      }
   }
    "the page has been visited previously" must {
      "return an empty list if no user filters have been selected (None of the above)" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(Some(userFilterNoAnswerForPage)))

        await(service.getPageAnswers(sessionId, pageId)) mustBe Some(Seq.empty)
      }

      "return a list of answers if user filters exists for this session and the page has answers" in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(Some(userFilterWithAnswerForPage)))

        await(service.getPageAnswers(sessionId, pageId)) mustBe Some(Seq(OverseasProperty))
      }
   }
  }

  "setPageAnswers" when {
    "the userFilters exist" must {
      "return true to show the answers have been saved"  in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(Some(userFilterWithAnswerForPage)))
        when(mockUserFiltersRepository.set(any()))
          .thenReturn(Future.successful(true))

        await(service.setPageAnswers(sessionId, pageId, Seq(ForeignIncome, OverseasProperty))) mustBe true
      }
    }
    "the userFilters do not exist" must {
      "return false to show the answers have not been saved"  in new Setup {
        when(mockUserFiltersRepository.get(eqTo(sessionId)))
          .thenReturn(Future.successful(None))

        await(service.setPageAnswers(sessionId, pageId, Seq(ForeignIncome, OverseasProperty))) mustBe false
      }
    }
  }

}
