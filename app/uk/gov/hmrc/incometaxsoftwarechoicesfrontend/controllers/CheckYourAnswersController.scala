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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.{Recognised, Unrecognised}
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
      val foundVendor = vendors.find(v => request.softwareName.contains(v.vendor.name))
      println(Console.RED + foundVendor + Console.RESET)
      // If not compliant condition is correct, remove foundVendor.isDefined from below, and just keep guard clauses?
      (vendors.isEmpty, request.journey, request.softwareType, foundVendor.isDefined) match {
        case (true, _, _, _) =>
          println(Console.BLUE + "zero results" + Console.RESET)
          Redirect(routes.ZeroSoftwareResultsController.show())
        case (false, None, _, _) =>
          println(Console.BLUE + "no journey, choosing software" + Console.RESET)
          Redirect(routes.ChoosingSoftwareController.show())
        case (false, Some(Find), _, _)  =>
          println(Console.BLUE + "find journey, choosing software" + Console.RESET)
          Redirect(routes.ChoosingSoftwareController.show())
        case (false, Some(Check), Some(Unrecognised), _)  =>
          // Or the following??
          // case (false, Some(Check), _, false)  =>
          println(Console.BLUE + "check journey, not compliant" + Console.RESET)
          Redirect(routes.CheckYourAnswersController.show())
        case (false, Some(Check), Some(Recognised), true) if foundVendor.get.eoyReady.contains(true) =>
          println(Console.BLUE + "check journey, fully compliant" + Console.RESET)
          Redirect(routes.CheckYourAnswersController.show())
        case (false, Some(Check), Some(Recognised), true) if foundVendor.get.eoyReady.contains(false) =>
          println(Console.BLUE + "check journey, partially compliant" + Console.RESET)
          Redirect(routes.CheckYourAnswersController.show())
        case (false, Some(Check), Some(Recognised), true) if foundVendor.get.eoyReady.isEmpty =>
          println(Console.BLUE + "check journey, quarterly only" + Console.RESET)
          Redirect(routes.CheckYourAnswersController.show())
        case (false, Some(Check), _, _) =>
          println(Console.BLUE + "check journey, choosing software" + Console.RESET)
          Redirect(routes.ChoosingSoftwareController.show())
      }
    }
  }

}
