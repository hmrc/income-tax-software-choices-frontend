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
import play.api.mvc.PathBindable

enum TimeoutType(val key: String) {
  case Timeout extends TimeoutType("timeout")
  case Manual  extends TimeoutType("manual")
  case Auto    extends TimeoutType("auto")
}

object TimeoutType {
  private val keyToType = TimeoutType.values.map(value => value.key -> value).toMap

  implicit val reads: Reads[TimeoutType] = __.read[String] map keyToType

  implicit val writes: Writes[TimeoutType] = Writes(timeoutType => JsString(timeoutType.key))

  implicit val pathBindable: PathBindable[TimeoutType] = new PathBindable[TimeoutType] {
    override def bind(key: String, value: String): Either[String, TimeoutType] =
      keyToType.get(value).toRight(s"Invalid timeout type: $value")

    override def unbind(key: String, value: TimeoutType): String =
      value.key
  }

}