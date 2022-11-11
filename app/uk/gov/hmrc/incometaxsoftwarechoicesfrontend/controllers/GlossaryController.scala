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

import play.api.data.Form
import play.api.http.HeaderNames
import play.api.i18n.Lang
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.GlossaryController.{glossaryMaxLabelsWithoutLinks, referringProvider}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.GlossaryForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.GlossaryFormModel
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.GlossaryService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.GlossaryService.GlossaryContent
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.{GlossaryPage, GlossaryPageList}

import java.net.{URL, URLDecoder}
import javax.inject.{Inject, Singleton}

@Singleton
class GlossaryController @Inject()(mcc: MessagesControllerComponents,
                                   val appConfig: AppConfig,
                                   glossaryPage: GlossaryPage,
                                   glossaryPageList: GlossaryPageList,
                                   glossaryService: GlossaryService) extends BaseFrontendController(mcc) {

  private val glossaryForm: Form[GlossaryFormModel] = GlossaryForm.form

  def show: Action[AnyContent] = Action { implicit request =>
    implicit val requestLanguage: Lang = request.lang(messagesApi)
    val glossaryContent: GlossaryContent = glossaryService.getGlossaryContent()

    Ok(view(ajax = false, glossaryContent = glossaryContent))
  }

  def search(ajax: Boolean): Action[AnyContent] = Action { implicit request =>
    implicit val requestLanguage: Lang = request.lang(messagesApi)

    // form can't produce an error, either we get value or have unreachable code
    val model: GlossaryFormModel = glossaryForm.bindFromRequest().value.get
    Ok(view(ajax, glossaryForm.fill(model), glossaryService.getGlossaryContent(model)))
  }

  private def view(ajax: Boolean,
                   form: Form[GlossaryFormModel] = glossaryForm,
                   glossaryContent: GlossaryContent)(implicit lang: Lang, request: Request[_]) = {
    if (ajax) {
      glossaryPageList(
        glossaryContent,
        glossaryMaxLabelsWithoutLinks,
        searched = true
      )
    } else {
      glossaryPage(
        glossaryContent,
        glossaryMaxLabelsWithoutLinks,
        glossaryService.getLastChangedString,
        referringProvider,
        form,
        routes.GlossaryController.search(ajax = false)
      )
    }
  }

}

object GlossaryController extends HeaderNames {
  val glossaryMaxLabelsWithoutLinks = 7

  def referringProvider(implicit request: Request[_]): Option[String] = {
    request.headers.get(REFERER).flatMap(r => {
      val url = new URL(r)
      val path = URLDecoder.decode(url.getPath, "UTF-8")
      val softwareProvider = path.split("/").reverse.head
      val url1 = uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.routes.ProductDetailsController.show(softwareProvider).url
      val loopbackCheck = url1 == path
      if (loopbackCheck)
        Some(softwareProvider)
      else
        None
    })
  }
}