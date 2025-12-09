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

sealed trait UserType {
  def key: String
  val auditDescription: String
}

object UserType {

  case object SoleTraderOrLandlord extends UserType {
    val key: String = "sole-trader-or-landlord"
    override val auditDescription: String = "soleTraderOrLandlord"
  }

  case object Agent extends UserType {
    val key: String = "agent"
    override val auditDescription: String = "agent"
  }

  private val userKey: Map[String, UserType] = Seq(
    SoleTraderOrLandlord,
    Agent)
    .map(value => value.key -> value).toMap

  implicit val reads: Reads[UserType] = __.read[String] map userKey

  implicit val writes: Writes[UserType] = Writes(value => JsString(value.key))
}
