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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.{RequireUserDataRefiner, SessionIdentifierAction}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ChoosingSoftwareView

import javax.inject.{Inject, Singleton}

@Singleton
class ChoosingSoftwareController @Inject()(view: ChoosingSoftwareView,
                                           identify: SessionIdentifierAction,
                                           requireData: RequireUserDataRefiner)
                                          (implicit mcc: MessagesControllerComponents) extends BaseFrontendController {

  def show(): Action[AnyContent] = (identify andThen requireData) { request =>
    given Request[AnyContent] = request
    Ok(view(
      postAction = routes.ChoosingSoftwareController.submit(),
      backLink = routes.CheckYourAnswersController.show().url
    ))
  }

  def submit(): Action[AnyContent] = (identify andThen requireData) { _ =>
    Redirect(routes.SearchSoftwareController.show())
  }

}

