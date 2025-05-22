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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.SoftwareChoicesService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.ProductDetailsPage

import java.net.URLDecoder
import javax.inject.{Inject, Singleton}

@Singleton
class ProductDetailsController @Inject()(mcc: MessagesControllerComponents,
                                         val appConfig: AppConfig,
                                         softwareChoicesService: SoftwareChoicesService,
                                         productDetailsPage: ProductDetailsPage) extends BaseFrontendController(mcc) with FeatureSwitching {

  def show(software: String): Action[AnyContent] = Action { implicit request =>
    val softwareName = URLDecoder.decode(software, "UTF-8")
    softwareChoicesService.getSoftwareVendor(softwareName) match {
      case None => throw new NotFoundException(ProductDetailsController.NotFound)
      case Some(softwareVendor) => Ok(productDetailsPage(
        softwareVendor,
        isEnabled(IncomeAndDeduction),
        isEnabled(ExtraPricingOptions),
        isEnabled(DisplayOverseasProperty)))
    }
  }
}

object ProductDetailsController {
  val NotProvided = "[ProductDetailsController][show] - Software vendor not provided"
  val NotFound = "[ProductDetailsController][show] - Software vendor not found"
}