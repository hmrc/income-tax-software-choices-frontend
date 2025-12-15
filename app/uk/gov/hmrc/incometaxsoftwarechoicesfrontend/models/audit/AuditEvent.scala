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

    val businessIncome = {
      userAnswers.get(BusinessIncomePage).map(answers =>
        Json.obj("businessIncome" -> answers.map(_.auditDescription))).getOrElse(Json.obj())
    }
    val additionalIncome = {
      userAnswers.get(AdditionalIncomeSourcesPage).map(answers =>
        Json.obj("additionalIncome" -> answers.map(_.auditDescription))).getOrElse(Json.obj())
    }
    val otherItems = {
      userAnswers.get(OtherItemsPage).map(answers =>
        Json.obj("otherItems" -> answers.map(_.auditDescription))).getOrElse(Json.obj())
    }
    val accountingPeriod = {
      userAnswers.get(AccountingPeriodPage).map(answer =>
        Json.obj("accountingPeriod" -> answer.auditDescription)).getOrElse(Json.obj())
    }

    val userAnswersFormatted = {
      Json.obj("userType" -> userAnswers.get(UserTypePage).map(_.auditDescription)) ++
        businessIncome ++
        additionalIncome ++
        otherItems ++
        accountingPeriod
    }

    SearchResultsEvent(
      userAnswersFormatted,
      userFilters.finalFilters.map(_.auditDescription),
      vendorsList,
      vendorsList.size
    )

  }

  implicit val writes: OWrites[SearchResultsEvent] = Json.writes[SearchResultsEvent]
}
