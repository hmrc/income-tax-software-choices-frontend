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

import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RecoveryIdService @Inject()(userFiltersRepository: UserFiltersRepository, implicit val ec: ExecutionContext) {

  def getRecoveryId(id: String): Future[Option[String]] = {
    userFiltersRepository.get(id).map {
      case Some(userFilters) if(userFilters.recoveryId.isDefined) => userFilters.recoveryId
      case Some(userFilters) => {
        val recoveryId = java.util.UUID.randomUUID().toString
        userFiltersRepository.set(userFilters.copy(recoveryId = Some(recoveryId)))
        Some(recoveryId)
      }
      case None => None
    }
  }
  
  def recoverSession(newSessionId: String, recoveryId: String): Future[Boolean] = {
    userFiltersRepository.getRecovery(recoveryId).flatMap {
      case Some(userFilters) => userFiltersRepository.set(userFilters.copy(id = newSessionId))
      case None => Future.successful(false)
    }
  }


}

