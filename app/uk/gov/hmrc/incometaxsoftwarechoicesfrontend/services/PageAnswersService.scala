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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.Individual
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.queries.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PageAnswersService @Inject()(userFiltersRepository: UserFiltersRepository, implicit val ec: ExecutionContext) {

  private val userPages: Seq[QuestionPage[_]] = Seq(
    UserTypePage,
    BusinessIncomePage,
    AdditionalIncomeSourcesPage,
    OtherItemsPage,
    AccountingPeriodPage
  )

  def getPageAnswers[A](id: String, page: Gettable[A])(implicit rds: Reads[A]): Future[Option[A]] = {
    userFiltersRepository.get(id).map {
      case Some(uf) => uf.answers.flatMap(_.get(page))
      case None => None
    }
  }

  def getPageAnswers[A](userAnswers: Option[UserAnswers], page: Gettable[A])(implicit rds: Reads[A]): Option[A] = {
    userAnswers match {
      case Some(userAnswers) => userAnswers.get(page)
      case _ => None
    }
  }

  def setPageAnswers[A](userFilters: UserFilters, page: Settable[A], value: A)(implicit writes: Writes[A]): Future[Boolean] = {
    val newAnswers = userFilters.answers match {
      case Some(userAnswers) => userAnswers.set(page, value).toOption
      case None => UserAnswers().set(page, value).toOption
    }
    userFiltersRepository.set(userFilters.copy(answers = newAnswers))
  }

  def setPageAnswers[A](id: String, page: Settable[A], value: A)(implicit writes: Writes[A]): Future[Boolean] = {
    userFiltersRepository.get(id).flatMap {
      case Some(userFilters) =>
        val newAnswers = userFilters.answers match {
          case Some(userAnswers) => userAnswers.set(page, value).toOption
          case None => UserAnswers().set(page, value).toOption
        }
        userFiltersRepository.set(userFilters.copy(answers = newAnswers))
      case None =>
        userFiltersRepository.set(UserFilters(id, UserAnswers().set(page, value).toOption, Seq.empty))
    }
  }

  def saveFiltersFromAnswers(id: String): Future[Seq[VendorFilter]] = {
    userFiltersRepository.get(id).flatMap {
      case Some(userFilters) =>
        val vendorFilters = getFiltersFromAnswers(userFilters.answers)
        userFiltersRepository.set(userFilters.copy(finalFilters = vendorFilters)) map { _ =>
          vendorFilters
        }
      case None =>
        Future.successful(Seq.empty[VendorFilter])
    }
  }

  def getFiltersFromAnswers(userAnswers: Option[UserAnswers]): Seq[VendorFilter] = {
    userPages.flatMap(page => userAnswers.map(_.data).map(page.extractVendorFilters).getOrElse(Seq.empty))
  }

  def resetUserAnswers(id: String): Future[Boolean] = {
    userFiltersRepository.set(UserFilters(id, Some(UserAnswers()), Seq.empty))
  }

}

