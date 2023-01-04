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
  private val langToOrderedListOfMessagePairsMap = langs.availables.map(lang => lang -> getAllMessagesPairsUnsorted(lang)).toMap
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  if (!langToOrderedListOfMessagePairsMap.keys.toList.contains(English)) throw new GlossaryException(langToOrderedListOfMessagePairsMap.keys.toList)
  private val defaultGlossarySettings = GlossaryFormModel(sortOrder = Some("asc"))

  def getGlossaryContent(glossaryFormModel: GlossaryFormModel = defaultGlossarySettings)(implicit requestLanguage: Lang): GlossaryContent =
    searchAndSort(glossaryFormModel)(langToOrderedListOfMessagePairsMap(requestLanguage))

  private def searchAndSort(glossaryFormModel: GlossaryFormModel) =
    matchSearchTerm(glossaryFormModel.searchTerm) _ andThen sortSearchResults(glossaryFormModel.sortOrder)

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

  private def getAllMessagesPairsUnsorted(lang: Lang): GlossaryContent = {
    val messages = messagesApi.preferred(Seq(lang))
    allMessageKeys
      .groupBy(k => messages(s"$k.key").substring(0, 1))
      .map { case (initial, messageKeys) => initial ->
        messageKeys.map(sss => messages(s"$sss.key") -> messages(s"$sss.value"))
          .toList
      }
      .toList
  }

}

object GlossaryService {

  type GlossaryContent = List[(String, List[(String, String)])]
  private val English: Lang = Lang("en")

  private val glossaryPrefix = "glossary.contents"
  private val glossaryPrefixLabels = glossaryPrefix.split("\\.").length

  private[services] def matchSearchTerm(maybeSearchTerm: Option[String])
                                       (glossaryContent: GlossaryContent): GlossaryContent = {

    val searchTermWords = maybeSearchTerm.map(_.toLowerCase().split("\\s+").toSeq).getOrElse(Seq.empty)

    glossaryContent map { case (key, list) =>
      val newList = list.filter { case (l, r) =>
        searchTermWords.forall((l + r).replaceAll("<.*?>", "").toLowerCase().contains(_))
      }
      (key, newList)
    } filter (_._2.nonEmpty)

  }

  private[services] def sortSearchResults(maybeSortOrder: Option[String])(glossaryContent: GlossaryContent): GlossaryContent = maybeSortOrder match {
    case Some("asc") => glossaryContent.sortWith(sortAsc).map { case (initialKey, entries) => (initialKey, entries.sortWith(sortAsc)) }
    case Some("desc") => glossaryContent.sortWith(sortDesc).map { case (initialKey, entries) => (initialKey, entries.sortWith(sortDesc)) }
    case _ => glossaryContent
  }

  private[services] def sortAsc(key1: (String, Any), key2: (String, Any)): Boolean = key1._1 < key2._1

  private[services] def sortDesc(key1: (String, Any), key2: (String, Any)): Boolean = key1._1 > key2._1

}

class GlossaryException(l: List[Lang]) extends RuntimeException("No English glossary found, only: " + l.map(ll => ll.code).mkString(","))
