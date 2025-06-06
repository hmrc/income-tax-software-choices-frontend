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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers

import org.jsoup.Jsoup
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import play.api.libs.ws.WSResponse

trait CustomMatchers {
  def httpStatus(expectedValue: Int): HavePropertyMatcher[WSResponse, Int] =
    (response: WSResponse) => HavePropertyMatchResult(
      response.status == expectedValue,
      "httpStatus",
      expectedValue,
      response.status
    )

  def pageTitle(expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)

      HavePropertyMatchResult(
        body.title == expectedValue,
        "pageTitle",
        expectedValue,
        body.title
      )
    }

  def redirectURI(expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val redirectLocation: Option[String] = response.header("Location")

      val matchCondition = redirectLocation.exists(_.contains(expectedValue))
      HavePropertyMatchResult(
        matchCondition,
        "redirectURI",
        expectedValue,
        redirectLocation.getOrElse("")
      )
    }
}
