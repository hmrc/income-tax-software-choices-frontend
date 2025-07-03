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

import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{FiltersFormModel, SoftwareVendors, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.{PageAnswersService, SoftwareChoicesService}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwarePage

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchSoftwareController @Inject()(mcc: MessagesControllerComponents,
                                         val appConfig: AppConfig,
                                         searchSoftwarePage: SearchSoftwarePage,
                                         softwareChoicesService: SoftwareChoicesService,
                                         pageAnswersService: PageAnswersService,
                                         userFiltersRepository: UserFiltersRepository,
                                         implicit val ec: ExecutionContext) extends BaseFrontendController(mcc) {

  def show(zeroResults: Boolean): Action[AnyContent] = Action { implicit request =>
    Ok(view(softwareChoicesService.getVendors(), FiltersForm.form, zeroResults))
   }

  def search(zeroResults: Boolean): Action[AnyContent] = Action.async { implicit request =>
    FiltersForm.form.bindFromRequest().fold(
      error => Future.successful(BadRequest(view(softwareChoicesService.getVendors(), error, zeroResults))),
      search => update(search) map { _ =>
        val vendors = softwareChoicesService.getVendors(search.searchTerm, search.filters)
        Ok(view(vendors, FiltersForm.form.fill(search), zeroResults))
      }
    )
  }

  def clear(zeroResults: Boolean): Action[AnyContent] = Action.async { implicit request =>
    update(FiltersFormModel()) map { _ =>
      Redirect(routes.SearchSoftwareController.show(zeroResults))
    }
  }

  private def update(search: FiltersFormModel)(implicit request: Request[_]): Future[Boolean] = {
    val sessionId = request.session.get("sessionId").getOrElse("")
    for {
      userFilters <- userFiltersRepository.get(sessionId)
      result <- userFilters match {
        case Some(userFilters) => userFiltersRepository.set(userFilters.copy(finalFilters = search.filters))
        case None => userFiltersRepository.set(UserFilters(sessionId, None, search.filters))
      }
    } yield {
      result
    }
  }

  private def view(vendors: SoftwareVendors, form: Form[FiltersFormModel], zeroResults: Boolean)
                  (implicit request: Request[_]): Html = {

    searchSoftwarePage(
      softwareVendors = vendors,
      searchForm = form,
      postAction = routes.SearchSoftwareController.search(zeroResults),
      clearAction = routes.SearchSoftwareController.clear(zeroResults),
      backUrl = backLinkUrl(zeroResults),
      zeroResults = zeroResults
    )
  }

  private def backLinkUrl(zeroResults: Boolean): String = {
   if (zeroResults) routes.ZeroSoftwareResultsController.show().url
   else routes.CheckYourAnswersController.show().url
  }

}
