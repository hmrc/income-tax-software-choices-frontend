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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.SessionIdentifierAction
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.{RecoveryIdService, SoftwareChoicesService}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.{NotFoundView, ProductDetailsView}

import java.net.URLDecoder
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductDetailsController @Inject()(softwareChoicesService: SoftwareChoicesService,
                                         recoveryIdService: RecoveryIdService,
                                         identify: SessionIdentifierAction,
                                         productDetailsPage: ProductDetailsView,
                                         notFoundView: NotFoundView)
                                        (implicit ec: ExecutionContext,
                                         mcc: MessagesControllerComponents) extends BaseFrontendController {

  def show(software: String): Action[AnyContent] = identify.async { request =>
    given Request[AnyContent] = request
    val softwareName = URLDecoder.decode(software, "UTF-8")
    softwareChoicesService.getSoftwareVendor(softwareName) match {
      case None => Future.successful(NotFound(notFoundView(routes.ProductDetailsController.show(software).url)))
      case Some(softwareVendor) => {
        recoveryIdService.getRecoveryId(request.sessionId).map(recoveryId =>
          Ok(productDetailsPage(softwareVendor, recoveryId))
        )
      }
    }
  }
}