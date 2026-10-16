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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.assets

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.io.Source
import scala.util.Using

class ApplicationStylesheetSpec extends AnyWordSpec with Matchers {

  private val stylesheet = Using.resource(Source.fromResource("stylesheets/application.css"))(_.mkString)

  "The compiled application stylesheet" should {
    "use 50:50 widths below the tablet breakpoint and thirds above it" in {
      val mobileWidths =
        """(?s)\.govuk-\\!-width-two-thirds-override\s*\{\s*width:\s*50%;\s*\}\s*\.govuk-\\!-width-one-third-override\s*\{\s*width:\s*50%;\s*\}""".r
      val tabletWidths =
        """(?s)@media\s*\(min-width:\s*40\.0625em\)\s*\{\s*\.govuk-\\!-width-two-thirds-override\s*\{\s*width:\s*66\.666667%;\s*\}\s*\.govuk-\\!-width-one-third-override\s*\{\s*width:\s*33\.333333%;\s*\}\s*\}""".r

      mobileWidths.findFirstIn(stylesheet) shouldBe defined
      tabletWidths.findFirstIn(stylesheet) shouldBe defined
    }
  }
}
