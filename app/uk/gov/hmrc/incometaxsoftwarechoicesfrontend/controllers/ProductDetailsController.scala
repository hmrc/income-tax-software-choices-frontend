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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.SoftwareChoicesService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.{ProductDetailsView, NotFoundView}

import java.net.URLDecoder
import javax.inject.{Inject, Singleton}

@Singleton
class ProductDetailsController @Inject()(softwareChoicesService: SoftwareChoicesService,
                                         productDetailsPage: ProductDetailsView,
                                         notFoundView: NotFoundView)
                                        (implicit mcc: MessagesControllerComponents) extends BaseFrontendController {

  def show(software: String): Action[AnyContent] = Action { request =>
    given Request[AnyContent] = request
    val softwareName = URLDecoder.decode(software, "UTF-8")
    softwareChoicesService.getSoftwareVendor(softwareName) match {
      case None => NotFound(notFoundView(routes.ProductDetailsController.show(software).url))
      case Some(softwareVendor) => Ok(productDetailsPage(softwareVendor))
    }
  }
}