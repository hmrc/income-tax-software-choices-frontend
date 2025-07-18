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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models

import play.api.libs.json._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.queries._

import scala.util.{Failure, Success, Try}

final case class UserAnswers(data: JsObject = Json.obj()) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.map(UserAnswers(_))
  }
}

object UserAnswers {

  val reads: Reads[UserAnswers] =
    (__ \ "data").read[JsObject].map(UserAnswers(_))

  val writes: OWrites[UserAnswers] =
    (__ \ "data").write[JsObject].contramap((f: UserAnswers) => f.data)

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)
}
