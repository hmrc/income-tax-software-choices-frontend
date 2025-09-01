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

import scala.jdk.CollectionConverters.CollectionHasAsScala

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

  def summaryListRow(key: String, values: String): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)

      val summaryListRows = body.select(".govuk-summary-list__row")
      val summaryListRowsKeys = body.select(".govuk-summary-list__row").select(".govuk-summary-list__key")

      val matchKeyCondition = summaryListRowsKeys.asScala.exists(_.text.equals(key))

      val matchValueCondition = summaryListRows.asScala.find(_.select(".govuk-summary-list__key").text()
        .contains(key)).exists(_.select(".govuk-summary-list__value").text().equals(values))

      HavePropertyMatchResult(
        matches = matchKeyCondition && matchValueCondition,
        propertyName = "summaryList",
        expectedValue = values,
        actualValue = summaryListRows.asScala.find(_.select(".govuk-summary-list__key").text().contains(key))
          .map(_.select(".govuk-summary-list__value").text()).getOrElse("")
      )
    }

  def checkboxSelected(id: String, value: Option[String]): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)
      val checkbox = body.select(s"input[id=$id]")
      val checkedAttr = "checked"

      def checkboxValue(checkboxId: String) =
        if (checkboxId.isEmpty) ""
        else body.select(s"input[id=$checkboxId]").attr("value")

      val matchCondition: Boolean = value match {
        case Some(expectedOption) =>
          val checkboxId = checkbox.select(s"input[checked]").attr("id")
          checkboxValue(checkboxId) == expectedOption
        case None => !checkbox.hasAttr(checkedAttr)
      }


      HavePropertyMatchResult(
        matches = matchCondition,
        propertyName = "checkbox",
        expectedValue = value.fold("")(identity),
        actualValue = {
          val selected = checkbox.select("input[checked]")
          selected.size() match {
            case 0 =>
              "no checkbox is selected"
            case _ =>
              val checkboxId = selected.attr("id")
              s"""The "${checkboxValue(checkboxId)}" selected"""
          }
        }
      )
    }

  def radioButtonSelected(id: String, selectedRadioButton: Option[String]): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)
      val radios = body.select(s"input[id=$id]")
      val checkedAttr = "checked"

      val matchCondition = selectedRadioButton match {
        case Some(expectedOption) => radios.select("input[checked]").attr("value") == expectedOption
        case None => !radios.hasAttr(checkedAttr)
      }

      HavePropertyMatchResult(
        matches = matchCondition,
        propertyName = "accounting-period",
        expectedValue = selectedRadioButton.fold("")(identity),
        actualValue = {
          val selected = radios.select("input[checked]")
          selected.size() match {
            case 0 =>
              "no radio button is selected"
            case 1 =>
              val actualKey = selected.attr("value")
              s"""The "$actualKey" selected"""
          }
        }
      )
    }
}
