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

import play.api.libs.json.{JsString, Reads, Writes, __}

sealed trait AccountingPeriod {
  def key: String
}

object AccountingPeriod {

  case object SixthAprilToFifthApril extends AccountingPeriod {
    val key: String = "sixth-april-to-fifth-april"
  }

  case object FirstAprilToThirtyFirstMarch extends AccountingPeriod {
    val key: String = "first-april-to-thirty-first-march"
  }

  case object OtherAccountingPeriod extends AccountingPeriod {
    val key: String = "other"
  }

  private val periodKey: Map[String, AccountingPeriod] = Seq(
    SixthAprilToFifthApril,
    FirstAprilToThirtyFirstMarch,
    OtherAccountingPeriod)
    .map(value => value.key -> value).toMap

  implicit val reads: Reads[AccountingPeriod] = __.read[String] map periodKey

  implicit val writes: Writes[AccountingPeriod] = Writes(value => JsString(value.key))
}

