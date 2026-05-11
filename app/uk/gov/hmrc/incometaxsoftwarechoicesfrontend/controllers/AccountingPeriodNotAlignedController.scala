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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.AccountingPeriodNotAlignedView
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class AccountingPeriodNotAlignedController @Inject()(view: AccountingPeriodNotAlignedView,
                                                     identify: SessionIdentifierAction,
                                                     requireData: RequireUserDataRefiner)
                                                    (implicit mcc: MessagesControllerComponents) extends BaseFrontendController {


  def show(editMode: Boolean): Action[AnyContent] = (identify andThen requireData) { request =>
    given Request[AnyContent] = request

    request.product match {
      case Some(product) =>
        val softwareName: Option[String] = product.softwareType match {
          case SoftwareType.Recognised => Some(product.name)
          case _                       => None
        }

        Ok(view(
          postAction = routes.AccountingPeriodNotAlignedController.submit(editMode),
          backLink = routes.AccountingPeriodController.show(editMode).url,
          softwareName = softwareName
        ))
      case None => InternalServerError("[AccountingPeriodNotAlignedController][show] - Could not find software product in answers")
    }
  }

  def submit(editMode: Boolean): Action[AnyContent] = (identify andThen requireData) { _ =>
    Redirect(routes.CheckYourAnswersController.show())
  }

}
