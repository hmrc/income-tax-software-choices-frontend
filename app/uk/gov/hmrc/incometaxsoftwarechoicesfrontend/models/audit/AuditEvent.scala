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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.audit

import play.api.libs.json.{JsObject, Json, OWrites}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.*

sealed trait AuditEvent {
  val auditType: String
}

case class SearchResultsEvent(userAnswers: JsObject,
                              filtersApplied: Seq[String],
                              vendorsList: Seq[String],
                              vendorsListCount: Int
                             ) extends AuditEvent {
  override val auditType = "SoftwareChoicesSearchResults"
}

object SearchResultsEvent {

  def apply(userFilters: UserFilters,
            vendorsList: Seq[String]): SearchResultsEvent = {

    val userAnswers = userFilters.answers.getOrElse(UserAnswers())
    val userAnswersFormatted = Json.obj(
      "userType" -> userAnswers.get(UserTypePage).map(_.auditDescription),
      "businessIncome" -> userAnswers.get(BusinessIncomePage).getOrElse(Seq.empty).map(_.auditDescription),
      "additionalIncome" -> userAnswers.get(AdditionalIncomeSourcesPage).getOrElse(Seq.empty).map(_.auditDescription),
      "otherItems" -> userAnswers.get(OtherItemsPage).getOrElse(Seq.empty).map(_.auditDescription),
      "accountingPeriod" -> userAnswers.get(AccountingPeriodPage).map(_.auditDescription).getOrElse("")
    )
    
    SearchResultsEvent(
      userAnswersFormatted,
      userFilters.finalFilters.map(_.auditDescription),
      vendorsList,
      vendorsList.size
    )

  }

  implicit val writes: OWrites[SearchResultsEvent] = Json.writes[SearchResultsEvent]
}
