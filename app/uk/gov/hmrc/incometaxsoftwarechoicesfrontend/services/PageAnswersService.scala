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

import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.queries._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PageAnswersService @Inject()(userFiltersRepository: UserFiltersRepository, implicit val ec: ExecutionContext) {

  private val userPages = Seq(
    BusinessIncomePage,
    AdditionalIncomeSourcesPage,
    OtherItemsPage
  )

  def getPageAnswers[A](id: String, page: Gettable[A])(implicit rds: Reads[A]): Future[Option[A]] = {
    userFiltersRepository.get(id).map {
      case Some(uf) => uf.answers.flatMap(_.get(page))
      case None => None
    }
  }

  def setPageAnswers[A](id: String, page: Settable[A], value: A)(implicit writes: Writes[A]): Future[Boolean] = {
    userFiltersRepository.get(id).flatMap {
      case Some(uf) => {
        val newAnswers = uf.answers.flatMap(_.set(page, value).toOption)
        userFiltersRepository.set(uf.copy(answers = newAnswers))
      }
      case None => {
        userFiltersRepository.set(UserFilters(id, UserAnswers().set(page, value).toOption, Seq.empty))
      }
    }
  }

  def saveFiltersFromAnswers(id: String): Future[Seq[VendorFilter]] = {
    userFiltersRepository.get(id).map {
      case Some(uf) => {
        val vf = userPages.flatMap(page => uf.answers.map(_.get(page).getOrElse(Seq.empty)).getOrElse(Seq.empty))
        userFiltersRepository.set(uf.copy(finalFilters = vf))
        vf
      }
      case None => Seq.empty
    }
  }

}

