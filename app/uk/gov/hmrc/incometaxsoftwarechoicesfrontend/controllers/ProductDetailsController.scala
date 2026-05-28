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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.SessionIdentifierAction
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.Check
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.Recognised
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.requests.SessionRequest
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{SoftwareVendorModel, UserAnswers, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.{PageAnswersService, SoftwareChoicesService}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.{EnterSoftwareNamePage, HowYouFindSoftwarePage}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.{NotFoundView, ProductDetailsPersonalisedView, ProductDetailsView}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ProductDetailsController @Inject()(softwareChoicesService: SoftwareChoicesService,
                                         userFiltersRepository: UserFiltersRepository,
                                         pageAnswersService: PageAnswersService,
                                         identify: SessionIdentifierAction,
                                         productDetailsView: ProductDetailsView,
                                         personalisedView: ProductDetailsPersonalisedView,
                                         notFoundView: NotFoundView)
                                        (implicit mcc: MessagesControllerComponents, appConfig: AppConfig, val executionContext: ExecutionContext) extends BaseFrontendController {

  def show(productId: String): Action[AnyContent] = identify.async { implicit request =>
    given Request[AnyContent] = request

    val vendorOpt = productId.toIntOption.flatMap(softwareChoicesService.getSoftwareVendor)

    userFiltersRepository.get(request.sessionId) map {
      case Some(userFilters) =>
        vendorOpt match {
          case Some(softwareVendor) => Ok(personalisedView(softwareVendor, backLink(userFilters.answers, userFilters.finalFilters, softwareVendor)))
          case _ => NotFound(notFoundView(routes.ProductDetailsController.show(productId).url))
        }
      case None =>
        vendorOpt match {
          case Some(softwareVendor) => Ok(productDetailsView(softwareVendor))
          case _ => NotFound(notFoundView(routes.ProductDetailsController.show(productId).url))
        }
    }
  }

  private def backLink(answers: Option[UserAnswers], filters: Seq[VendorFilter], softwareVendor: SoftwareVendorModel)
                      (implicit appConfig: AppConfig, request: SessionRequest[_]): String = {
    val journey     = pageAnswersService.getPageAnswers(answers, HowYouFindSoftwarePage)
    val productId   = pageAnswersService.getPageAnswers(answers, EnterSoftwareNamePage).map(_.productId).getOrElse(-1)
    val productType = pageAnswersService.getPageAnswers(answers, EnterSoftwareNamePage).map(_.softwareType)

    val isDifferentProduct  = productId != softwareVendor.productId
    val vendorOpt           = softwareChoicesService.getSoftwareVendor(productId)
    val isQuarterlyReady    = vendorOpt.map(_.isQuarterlyReady(filters))
    val isEoyReady          = vendorOpt.flatMap(_.isEoyReady(filters))

    (journey, isDifferentProduct, productType, isQuarterlyReady, isEoyReady) match {
      case (Some(Check), true,  _,                _,          _)            => routes.SearchSoftwareController.show().url
      case (Some(Check), false, Some(Recognised), Some(true), Some(true))   => routes.FullyCompatibleController.show().url
      case (Some(Check), false, Some(Recognised), Some(true), Some(false))  => routes.PartiallyCompatibleController.show().url
      case (Some(Check), false, Some(Recognised), Some(true), None)         => routes.QuarterlyOnlyController.show().url
      case (Some(Check), false, Some(Recognised), Some(false), None)        => routes.NotCompatibleController.show().url
      case _                                                                => routes.SearchSoftwareController.show().url
    }
  }
}
