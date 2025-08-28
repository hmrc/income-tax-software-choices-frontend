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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers

import play.api.libs.json.{JsValue, Reads, Writes}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.QuestionPage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository

import scala.concurrent.Future

trait DatabaseHelper extends ComponentSpecBase {

  lazy val userFiltersRepository: UserFiltersRepository = app.injector.instanceOf[UserFiltersRepository]

  def getById(id: String): Future[Option[UserFilters]] = userFiltersRepository.get(id)

  def getPageData(id: String, page: String): Option[JsValue] = await(getById(id)).flatMap(_.answers).flatMap(_.data.value.get(page))

  def getAllPageData(id: String): Iterable[JsValue] = await(getById(id)).get.answers.get.data.values

  def getFinalFilters(id: String): Seq[VendorFilter] = await(getById(id)).get.finalFilters

  def setupAnswers(id: String, userAnswers: Option[UserAnswers]): Unit = {
    await(userFiltersRepository.set(UserFilters(id = id, answers = userAnswers)))
  }

  def setPageData[A](id: String, page: QuestionPage[A], data: A)(implicit writes: Writes[A]): Unit = {
    setupAnswers(
      id = id,
      userAnswers = UserAnswers().set(page, data).toOption
    )
  }

  def getPageData[A](id: String, page: QuestionPage[A])(implicit reads: Reads[A]): Option[A] = {
    await(getById(id)).flatMap(_.answers.flatMap(_.get(page)))
  }

}
