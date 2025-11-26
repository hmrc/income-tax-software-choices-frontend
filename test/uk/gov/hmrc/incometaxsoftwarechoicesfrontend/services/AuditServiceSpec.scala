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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{doNothing, verify}
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.audit.AuditEvent
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext.global

class AuditServiceSpec extends PlaySpec {

  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val service = new AuditService(mockAuditConnector)(global)

  val hc = HeaderCarrier()

  case class TestAuditEvent(detail: Map[String, String]) extends AuditEvent {
    override val auditType: String = "TestAuditEvent"
  }

  object TestAuditEvent {
    implicit val format: OFormat[TestAuditEvent] = Json.format[TestAuditEvent]
  }

  "AuditService" must {
    "send explicit audit" in {
      val testEvent = TestAuditEvent(Map("key" -> "value"))

      doNothing().when(mockAuditConnector).sendExplicitAudit(any(), any())(any(), any(), any())

      service.audit(testEvent)(hc, TestAuditEvent.format)

      verify(mockAuditConnector).sendExplicitAudit(
        eqTo("TestAuditEvent"),
        eqTo(testEvent)
      )(any(), any(), any())

    }
  }

}
