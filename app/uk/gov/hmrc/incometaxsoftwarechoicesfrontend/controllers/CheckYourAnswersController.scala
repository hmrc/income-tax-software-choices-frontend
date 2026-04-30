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
      //TODO - Remove print lines in yield when new controllers are in place
      println(Console.GREEN + vendors.map(_.vendor.name) + Console.RESET)
      println(Console.YELLOW + request.softwareName + Console.RESET)
      val foundVendor = vendors.find(v => request.softwareName == Some(v.vendor.name))
      println(Console.RED + foundVendor + Console.RESET)
      val isQuarterlyReady = foundVendor.flatMap(_.quarterlyReady)
      println(Console.RED + isQuarterlyReady + Console.RESET)
      val isEoyReady = foundVendor.flatMap(_.eoyReady)
      println(Console.RED + isEoyReady + Console.RESET)
      (vendors.isEmpty, request.journey, request.softwareType, isQuarterlyReady, isEoyReady) match {
        case (true, _, _, _, _) =>
          println(Console.BLUE + "zero results" + Console.RESET)
          Redirect(routes.ZeroSoftwareResultsController.show())
        case (false, None, _, _, _) =>
          println(Console.BLUE + "no journey, choosing software" + Console.RESET)
          Redirect(routes.ChoosingSoftwareController.show())
        case (false, Some(Find), _, _, _)  =>
          println(Console.BLUE + "find journey, choosing software" + Console.RESET)
          Redirect(routes.ChoosingSoftwareController.show())
        case (false, Some(Check), Some(Recognised), Some(true), Some(true)) =>
          println(Console.BLUE + "check journey, fully compatible" + Console.RESET)
          Redirect(routes.FullyCompatibleController.show())
        case (false, Some(Check), Some(Recognised), Some(true), Some(false)) =>
          println(Console.BLUE + "check journey, partially compatible" + Console.RESET)
          Redirect(routes.CheckYourAnswersController.show())
        case (false, Some(Check), Some(Recognised), Some(true), None) =>
          println(Console.BLUE + "check journey, quarterly only" + Console.RESET)
          Redirect(routes.CheckYourAnswersController.show())
        case (false, Some(Check), Some(Recognised), None, None) =>
          println(Console.BLUE + "check journey, not compatible" + Console.RESET)
          Redirect(routes.CheckYourAnswersController.show())
        case (false, Some(Check), _, _, _) =>
          println(Console.BLUE + "check journey, choosing software" + Console.RESET)
          Redirect(routes.ChoosingSoftwareController.show())
      }
    }
  }

}
