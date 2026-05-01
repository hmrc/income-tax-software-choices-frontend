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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.{RequireUserDataRefiner, SessionIdentifierAction}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.{Check, Find}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.Recognised
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.{PageAnswersService, SoftwareChoicesService}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.helpers.SummaryListBuilder
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject()(view: CheckYourAnswersView,
                                           softwareChoicesService: SoftwareChoicesService,
                                           identify: SessionIdentifierAction,
                                           requireData: RequireUserDataRefiner)
                                          (implicit ec: ExecutionContext,
                                           mcc: MessagesControllerComponents,
                                           pageAnswersService: PageAnswersService,
                                           val appConfig: AppConfig)
  extends BaseFrontendController with SummaryListBuilder {

  def show(): Action[AnyContent] = (identify andThen requireData) { implicit request =>
    given Request[AnyContent] = request

    Ok(view(
      summaryList = buildSummaryList(request.userFilters.answers),
      postAction = routes.CheckYourAnswersController.submit()
    ))
  }

  def submit(): Action[AnyContent] = (identify andThen requireData).async { implicit request =>
    given Request[AnyContent] = request
    for {
      vendorFilters <- pageAnswersService.saveFiltersFromAnswers(request.sessionId)
      vendors = softwareChoicesService.getVendorsWithIntent(vendorFilters)
    } yield {
      val softwareName = request.product.map(_.name).getOrElse("")
      val softwareType = request.product.map(_.softwareType)
      val foundVendor = vendors.find(v => softwareName == v.vendor.name)
      val isQuarterlyReady = foundVendor.flatMap(_.quarterlyReady)
      val isEoyReady = foundVendor.flatMap(_.eoyReady)
      (vendors.isEmpty, request.journey, softwareType, isQuarterlyReady, isEoyReady) match {
        case (true, _, _, _, _) =>
          Redirect(routes.ZeroSoftwareResultsController.show())
        case (false, None, _, _, _) =>
          Redirect(routes.ChoosingSoftwareController.show())
        case (false, Some(Find), _, _, _)  =>
          Redirect(routes.ChoosingSoftwareController.show())
        case (false, Some(Check), Some(Recognised), Some(true), Some(true)) =>
          Redirect(routes.FullyCompatibleController.show())
        case (false, Some(Check), Some(Recognised), Some(true), Some(false)) =>
          Redirect(routes.CheckYourAnswersController.show())
        case (false, Some(Check), Some(Recognised), Some(true), None) =>
          Redirect(routes.CheckYourAnswersController.show())
        case (false, Some(Check), Some(Recognised), None, None) =>
          Redirect(routes.CheckYourAnswersController.show())
        case (false, Some(Check), _, _, _) =>
          Redirect(routes.ChoosingSoftwareController.show())
      }
    }
  }

}
