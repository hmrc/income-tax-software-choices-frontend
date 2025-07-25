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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.{PageAnswersService, SoftwareChoicesService}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.helpers.SummaryListBuilder
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.CheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject()(view: CheckYourAnswersView,
                                           softwareChoicesService: SoftwareChoicesService,
                                           userFiltersRepository: UserFiltersRepository)
                                          (implicit val ec: ExecutionContext,
                                           mcc: MessagesControllerComponents,
                                           pageAnswersService: PageAnswersService) extends BaseFrontendController(mcc) with SummaryListBuilder {

  def show(): Action[AnyContent] = Action.async { implicit request =>
    val sessionId = request.session.get("sessionId").getOrElse("")
    for {
      userFilters <- userFiltersRepository.get(sessionId)
      summaryList = buildSummaryList(userFilters.flatMap(_.answers))
    } yield {
      Ok(view(
        summaryList = summaryList,
        postAction = routes.CheckYourAnswersController.submit(),
        backLink = routes.AccountingPeriodController.show().url
      ))
    }
  }

  def submit(): Action[AnyContent] = Action.async { implicit request =>
    val sessionId = request.session.get("sessionId").getOrElse("")
    for {
      vendorFilters <- pageAnswersService.saveFiltersFromAnswers(sessionId)
      vendors = softwareChoicesService.getAllInOneVendors(vendorFilters).vendors
    } yield {
      if (vendors.isEmpty) {
        Redirect(routes.ZeroSoftwareResultsController.show())
      } else {
        Redirect(routes.SearchSoftwareController.show())
      }
    }
  }

}
