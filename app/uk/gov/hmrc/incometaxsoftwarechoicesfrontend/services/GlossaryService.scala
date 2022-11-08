/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.Logging
import play.api.i18n.{Lang, Langs, Messages, MessagesApi}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.GlossaryFormModel
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.GlossaryService._
import uk.gov.hmrc.play.language.LanguageUtils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

@Singleton
class GlossaryService @Inject()(messagesApi: MessagesApi, langs: Langs, languageUtils: LanguageUtils) extends Logging {

  private val allMessageKeys = getMessageKeySet
  private val langToOrderedListOfMessagePairsMap = langs.availables.map(lang => lang -> getMessagesPairs(lang)).toMap
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  if (!langToOrderedListOfMessagePairsMap.keys.toList.contains(English)) throw new GlossaryException(langToOrderedListOfMessagePairsMap.keys.toList)

  def getGlossaryList(implicit requestLanguage: Lang): List[(String, List[(String, String)])] = {
    langToOrderedListOfMessagePairsMap(requestLanguage)
  }

  def getFilteredGlossaryList(glossaryFormModel: GlossaryFormModel)(implicit requestLanguage: Lang): List[(String, List[(String, String)])] = {
    matchSearchTerm(glossaryFormModel.searchTerm)(getGlossaryList)
  }

  def getLastChangedString(implicit lang: Lang, messages: Messages): String = {
    val lastChangedMessage = messagesApi("glossary.last-changed")
    val lastChangedDate: LocalDate = LocalDate.parse(lastChangedMessage, formatter)

    languageUtils.Dates.formatDate(lastChangedDate)
  }

  private def getMessageKeySet: Set[String] = {
    messagesApi
      .messages
      .values
      .flatMap(m => m.keys)
      .filter(_.startsWith(s"$glossaryPrefix."))
      .map(s => s.split("\\.").take(glossaryPrefixLabels + 1).mkString("."))
      .toSet
  }

  private def getMessagesPairs(lang: Lang): List[(String, List[(String, String)])] = {
    val messages = messagesApi.preferred(Seq(lang))
    allMessageKeys
      .groupBy(k => messages(s"$k.key").substring(0, 1))
      .map { case (initial, messageKeys) => initial ->
        messageKeys.map(sss => messages(s"$sss.key") -> messages(s"$sss.value"))
          .toList
          .sortBy(_._1)
      }
      .toList
      .sortBy(_._1)
  }

}

object GlossaryService {

  private val English: Lang = Lang("en")

  private val glossaryPrefix = "glossary.contents"
  private val glossaryPrefixLabels = glossaryPrefix.split("\\.").length

  private[services] def matchSearchTerm(maybeSearchTerm: Option[String])
                                       (glossaryList: List[(String, List[(String, String)])]): List[(String, List[(String, String)])] = {

    val searchTermWords = maybeSearchTerm.map(_.toLowerCase().split("\\s+").toSeq).getOrElse(Seq.empty)

    glossaryList map { case (key, list) =>
      val newList = list.filter { case (l, r) =>
        searchTermWords.forall((l + r).replaceAll("<.*?>", "").toLowerCase().contains(_))
      }
      (key, newList)
    } filter (_._2.nonEmpty)

  }

}

class GlossaryException(l: List[Lang]) extends RuntimeException("No English glossary found, only: " + l.map(ll => ll.code).mkString(","))
