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

import play.api.libs.json.{Reads, __}

sealed trait FeatureStatus {
  val key: String
}

object FeatureStatus {
  case object Available extends FeatureStatus {
    override val key: String = "Available"
  }

  case object Intended extends FeatureStatus {
    override val key: String = "Intended"
  }

  implicit val reads: Reads[FeatureStatus] = __.read[String] map[FeatureStatus] {
    case "Available" => Available
    case "Intended" => Intended
  }

}
