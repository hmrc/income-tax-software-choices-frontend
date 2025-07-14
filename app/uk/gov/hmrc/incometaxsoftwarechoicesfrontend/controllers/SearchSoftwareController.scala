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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.Agent
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{FiltersFormModel, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.UserTypePage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.{PageAnswersService, SoftwareChoicesService}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.viewmodels.SoftwareChoicesResultsViewModel
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

  def show(zeroResults: Boolean): Action[AnyContent] = Action.async { implicit request =>
    val sessionId = request.session.get("sessionId").getOrElse("")
    for {
      userFilters <- userFiltersRepository.get(sessionId)
      filters = userFilters.map(_.finalFilters).getOrElse(Seq.empty)
      userType <- pageAnswersService.getPageAnswers(sessionId, UserTypePage)
      model = SoftwareChoicesResultsViewModel(
        allInOneVendors = softwareChoicesService.getAllInOneVendors(filters = filters),
        otherVendors = softwareChoicesService.getOtherVendors(filters = filters),
        zeroResults = zeroResults,
        isAgent = userType.contains(Agent)
      )
    } yield {
      Ok(view(model, FiltersForm.form.fill(FiltersFormModel(filters = filters))))
    }
  }

  def search(zeroResults: Boolean): Action[AnyContent] = Action.async { implicit request =>
    val sessionId = request.session.get("sessionId").getOrElse("")
    val filters = FiltersForm.form.bindFromRequest().get
    for {
      userType <- pageAnswersService.getPageAnswers(sessionId, UserTypePage)
      _ <- update(filters)
    } yield {
      val model = SoftwareChoicesResultsViewModel(
        allInOneVendors = softwareChoicesService.getAllInOneVendors(filters.filters),
        otherVendors = softwareChoicesService.getOtherVendors(filters.filters),
        zeroResults = zeroResults,
        isAgent = userType.contains(Agent)
      )
      Ok(view(model, FiltersForm.form.fill(filters)))
    }
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

  private def view(model: SoftwareChoicesResultsViewModel, form: Form[FiltersFormModel])
                  (implicit request: Request[_]): Html = {

    searchSoftwarePage(
      viewModel = model,
      searchForm = form,
      postAction = routes.SearchSoftwareController.search(model.zeroResults),
      clearAction = routes.SearchSoftwareController.clear(model.zeroResults),
      backUrl = backLinkUrl(model.isAgent, model.zeroResults)
    )
  }

  def backLinkUrl(isAgent: Boolean, zeroResults: Boolean): String = {
    if (isAgent) {
      routes.UserTypeController.show().url
    } else if (zeroResults) {
      routes.ZeroSoftwareResultsController.show().url
    } else {
      routes.CheckYourAnswersController.show().url
    }
  }
}
