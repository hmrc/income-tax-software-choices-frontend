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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers

import play.api.i18n.Lang
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.GlossaryPage

import javax.inject.{Inject, Singleton}

@Singleton
class GlossaryController @Inject()(mcc: MessagesControllerComponents,
                                   val appConfig: AppConfig,
                                   glossaryPage: GlossaryPage
                                  ) extends BaseFrontendController(mcc) {

  private val English: Lang = Lang("en")

  private val glossaryPrefix = "glossary.contents"
  private val glossaryPrefixLabels = glossaryPrefix.split("\\.").length
  private val glossaryMaxLabelsWithoutLinks = 7

  private val allMessageKeys = getMessageKeySet

  private val langToOrderedListOfMessagePairsMap = mcc.langs.availables.map(lang => lang -> getMessagesPairs(lang)).toMap
  if (!langToOrderedListOfMessagePairsMap.keys.toList.contains(English)) throw new GlossaryException(langToOrderedListOfMessagePairsMap.keys.toList)

  def show(ajax: Boolean): Action[AnyContent] = Action { implicit request =>

    val requestLanguage = request.lang(mcc.messagesApi)
    // NB This is actually handled by Play - if you ask for, say, French, it will give you the default language, which will be English.
    // So we *know* the request language will be present in our map
    val glossaryList = langToOrderedListOfMessagePairsMap(requestLanguage)
    Ok(glossaryPage(glossaryList, glossaryMaxLabelsWithoutLinks))
  }

  private def getMessagesPairs(lang: Lang) = {
    val messages = messagesApi.preferred(Seq(lang))
    allMessageKeys
      .groupBy(k => messages.messages(s"$k.key").substring(0, 1))
      .map { case (initial, messageKeys) => initial ->
        messageKeys.map(sss => messages.messages(s"$sss.key") -> messages.messages(s"$sss.value") )
          .toList
          .sortBy(_._1)
      }
      .toList
      .sortBy(_._1)
  }

  private def getMessageKeySet = mcc
    .messagesApi
    .messages
    .values
    .flatMap(m => m.keys)
    .filter(_.startsWith(s"$glossaryPrefix."))
    .map(s => s.split("\\.").take(glossaryPrefixLabels + 1).mkString(".")).toSet

}

class GlossaryException(l: List[Lang]) extends RuntimeException("No English glossary found, only: " + l.map(ll=>ll.code).mkString(","))
