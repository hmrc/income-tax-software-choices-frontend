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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.mappings

import play.api.data.Forms.of
import play.api.data.format.Formatter
import play.api.data.{FormError, Mapping}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.{Agent, SoleTraderOrLandlord}

object UserTypeMapping {

  def userTypeMapping(error: String): Mapping[UserType] = {
    of(userTypeFormatter(error))
  }

  private def userTypeFormatter(error: String): Formatter[UserType] = {
    new Formatter[UserType] {
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], UserType] = {
        data.get(key) match {
          case Some(SoleTraderOrLandlord.key) => Right(SoleTraderOrLandlord)
          case Some(Agent.key) => Right(Agent)
          case _ => Left(Seq(FormError(key, error)))
        }
      }

      override def unbind(key: String, value: UserType): Map[String, String] = {
        Map(key -> value.key)
      }
    }
  }
}
