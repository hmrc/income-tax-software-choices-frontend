/*
 * Copyright 2026 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.*

object JourneyTypeMapping {

  def journeyTypeMapping(error: String): Mapping[JourneyType] =
    of(journeyTypeFormatter(error))

  private def journeyTypeFormatter(error: String): Formatter[JourneyType] =
    new Formatter[JourneyType] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], JourneyType] =
        data.get(key) match {
          case Some(Find.key) => Right(Find)
          case Some(ViewAll.key) => Right(ViewAll)
          case Some(Check.key) => Right(Check)
          case _ => Left(Seq(FormError(key, error)))
        }

      override def unbind(key: String, value: JourneyType): Map[String, String] =
        Map(key -> value.key)
    }
}

