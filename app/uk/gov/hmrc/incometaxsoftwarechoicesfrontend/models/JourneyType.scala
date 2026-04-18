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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models
import play.api.libs.json.{JsString, Reads, Writes, __}

enum JourneyType(val key: String) {
  case Find  extends JourneyType("find")
  case Check extends JourneyType("check")
  case ViewAll extends JourneyType("view-all")
}

object JourneyType {

  private val keyToJourney = JourneyType.values.map(value => value.key -> value).toMap

  implicit val reads: Reads[JourneyType] = __.read[String] map keyToJourney
  
  implicit val writes: Writes[JourneyType] = Writes(journeyType => JsString(journeyType.key))
}
