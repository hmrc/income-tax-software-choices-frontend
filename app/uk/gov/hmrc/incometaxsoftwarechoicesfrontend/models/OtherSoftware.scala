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

import play.api.libs.json.{Format, JsString, Json, Reads, Writes, __}

case class OtherSoftware(productId: Int,
                         name: String,
                         softwareType: SoftwareType)

object OtherSoftware {
  implicit val format: Format[OtherSoftware] = Json.format[OtherSoftware]
}

case class OtherSoftwareList(software: Seq[OtherSoftware])

object OtherSoftwareList {
  implicit val reads: Reads[OtherSoftwareList] = Json.reads[OtherSoftwareList]
}

enum SoftwareType(val key: String) {
  case Spreadsheet  extends SoftwareType("spreadsheet")
  case FutureVendor extends SoftwareType("future-vendor")
  case Recognised extends SoftwareType("recognised")
  case Unrecognised extends SoftwareType("unrecognised")
}

object SoftwareType {
  private val keyToSoftwareType = SoftwareType.values.map(value => value.key -> value).toMap

  implicit val reads: Reads[SoftwareType] = __.read[String] map keyToSoftwareType

  implicit val writes: Writes[SoftwareType] = Writes(softwareType => JsString(softwareType.key))
}
