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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models

import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class UserFilters(id: String,
                       answers: Option[UserAnswers] = None,
                       finalFilters: Seq[VendorFilter] = Seq.empty,
                       lastUpdated: Instant = Instant.now)

object UserFilters {

  val reads: Reads[UserFilters] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").read[String] and
        (__ \ "answers").readNullable[UserAnswers] and
        (__ \ "finalFilters").read[Seq[VendorFilter]] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      ) (UserFilters.apply _)
  }

  val writes: OWrites[UserFilters] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").write[String] and
        (__ \ "answers").writeNullable[UserAnswers] and
        (__ \ "finalFilters").write[Seq[VendorFilter]] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
      ) (ua => (ua.id, ua.answers, ua.finalFilters, ua.lastUpdated))
  }

  implicit val format: OFormat[UserFilters] = OFormat(reads, writes)
}


