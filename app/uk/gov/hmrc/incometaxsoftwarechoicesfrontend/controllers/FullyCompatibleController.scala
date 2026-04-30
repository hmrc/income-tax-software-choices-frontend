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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.FullyCompatibleView

import javax.inject.{Inject, Singleton}

@Singleton
class FullyCompatibleController @Inject()(view: FullyCompatibleView,
                                          identify: SessionIdentifierAction,
                                          requireData: RequireUserDataRefiner)
                                         (implicit mcc: MessagesControllerComponents) extends BaseFrontendController {

  def show(): Action[AnyContent] = (identify andThen requireData) { request =>
    given Request[AnyContent] = request

    (request.softwareId, request.softwareName) match {
      case (Some(softwareId), Some(softwareName)) => {
        Ok(view(
          continueURL = routes.ProductDetailsController.show(softwareId.toString).url,
          backLink = routes.CheckYourAnswersController.show().url,
          chosenSoftware = softwareName,
        ))
      }
      case _ => InternalServerError("[FullyCompatibleController][show] - Could not find software name and software product ID in answers]")
    }

  }
}