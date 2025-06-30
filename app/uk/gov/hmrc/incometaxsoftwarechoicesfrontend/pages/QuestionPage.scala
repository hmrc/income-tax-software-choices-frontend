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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages

import play.api.libs.json.{JsObject, Reads}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.queries.{Gettable, Settable}

import scala.language.implicitConversions

trait QuestionPage[A] extends Gettable[A] with Settable[A] {

  implicit def toString(page: QuestionPage[A]): String = page.toString

  def reads: Reads[A]

  def toVendorFilter(value: A): Seq[VendorFilter] = Seq.empty

  def extractVendorFilters(json: JsObject): Seq[VendorFilter] = {
    Reads.optionNoError(Reads.at(path)(reads)).reads(json).getOrElse(None) match {
      case Some(value) => toVendorFilter(value)
      case None => Seq.empty
    }
  }

}