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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config

import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.MessagesMatcher

import scala.io.Source

class MessagesSpec extends PlaySpec with MessagesMatcher {
  override val excludedKeys: Set[String] = Set(
    "product-details.income-and-deduction-types.construction-industry-scheme",
    "product-details.income-and-deduction-types.lloyds-underwriters",
    "product-details.income-and-deduction-types.marriage-allowance",
    "product-details.income-and-deduction-types.heading",
    "product-details.income-and-deduction-types.minister-of-religion",
    "product-details.income-and-deduction-types.capital-gains-tax",
    "product-details.income-and-deduction-types.partner-income",
    "product-details.income-and-deduction-types.complex-partnerships",
    "product-details.income-and-deduction-types.state-pension",
    "product-details.income-and-deduction-types.member-of-parliament",
    "product-details.income-and-deduction-types.paye",
    "product-details.income-and-deduction-types.investments",
    "product-details.income-and-deduction-types.residence-and-remittance",
    "product-details.income-and-deduction-types.uk-dividends",
    "product-details.income-and-deduction-types.sa-additional-income",
    "product-details.income-and-deduction-types.property-business",
    "product-details.income-and-deduction-types.married-allowance",
    "product-details.income-and-deduction-types.gift-aid",
    "product-details.income-and-deduction-types.blind-persons-allowance",
    "product-details.income-and-deduction-types.simple-partnerships",
    "product-details.income-and-deduction-types.student-loans",
    "product-details.income-and-deduction-types.pensions",
    "product-details.income-and-deduction-types.pension-contributions",
    "product-details.income-and-deduction-types.uk-interest",
    "product-details.income-and-deduction-types.employment",
    "product-details.income-and-deduction-types.foreign-income",
    "product-details.income-and-deduction-types.self-employment",
    "product-details.income-and-deduction-types.high-income-child-benefit",
    "product-details.income-and-deduction-types.number-covered",
    "product-details.explanation-link.text",
    "product-details.explanation-link.link"
  )

  private val messageKeysEnglish: List[String] = getMessageKeys("messages").toList
  private lazy val messageKeySetEnglish = messageKeysEnglish.toSet

  private val messageKeysWelsh: List[String] = getMessageKeys("messages.cy").toList
  private lazy val messageKeySetWelsh = messageKeysWelsh.toSet

  "Messages present in Welsh (conf/messages.cy)" should {
    "also have an English translation (conf/messages)" in {
      messageKeySetWelsh must allBeIn(messageKeySetEnglish)
    }

    "not contain duplicate keys" in {
      messageKeysWelsh must containUniqueKeys
    }

    "contain only permitted characters" in {
      messageKeysWelsh must containOnlyPermittedCharacters
    }
  }

  "Messages present in English (conf/messages)" should {
    "also have a Welsh translation (conf/messages.cy)" in {
      messageKeySetEnglish must allBeIn(messageKeySetWelsh)
    }

    "not contain duplicate keys" in {
      messageKeysEnglish must containUniqueKeys
    }

    "contain only permitted characters" in {
      messageKeysEnglish must containOnlyPermittedCharacters
    }
  }

  private def getMessageKeys(fileName: String) = {
    Source.fromResource(fileName)
      .getLines()
      .map(_.trim)
      .filter(!_.startsWith("#"))
      .filter(_.nonEmpty)
      .map(_.split(' ').head)
  }
}
