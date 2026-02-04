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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.ExplicitAudits
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.{RequireUserDataRefiner, SessionIdentifierAction}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.Agent
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.requests.SessionDataRequest
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.UserTypePage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.viewmodels.SoftwareChoicesResultsViewModel
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwareView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchSoftwareController @Inject()(searchSoftwareView: SearchSoftwareView,
                                         softwareChoicesService: SoftwareChoicesService,
                                         pageAnswersService: PageAnswersService,
                                         userFiltersRepository: UserFiltersRepository,
                                         identify: SessionIdentifierAction,
                                         requireData: RequireUserDataRefiner,
                                         auditService: AuditService)
                                        (implicit ec: ExecutionContext,
                                         val appConfig: AppConfig,
                                         mcc: MessagesControllerComponents) extends BaseFrontendController with FeatureSwitching {

  def show(): Action[AnyContent] = (identify andThen requireData) { implicit request =>
    given Request[AnyContent] = request

    val finalFilters = request.userFilters.finalFilters
    val isAgent = pageAnswersService.getPageAnswers(request.userFilters.answers, UserTypePage).contains(Agent)
    val model = SoftwareChoicesResultsViewModel(
      vendorsWithIntent = softwareChoicesService.getVendorsWithIntent(finalFilters),
      isAgent = isAgent
    )

    if (isEnabled(ExplicitAudits)) auditService.auditSearchResults(request.userFilters, model.vendorsWithIntent.map(_.vendor.name))
    Ok(view(model = model, form = FiltersForm.form.fill(FiltersFormModel(filters = finalFilters))))

  }

  def search(): Action[AnyContent] = (identify andThen requireData).async { implicit request =>
    given Request[AnyContent] = request
    val filters = FiltersForm.form.bindFromRequest().get
    for {
      updatedUserFilters <- update(filters)(request).flatMap(_ => userFiltersRepository.get(request.sessionId))
    } yield {
      val isAgent = pageAnswersService.getPageAnswers(request.userFilters.answers, UserTypePage).contains(Agent)
      val userFilters = updatedUserFilters.getOrElse(UserFilters(request.sessionId, None, filters.filters))
      val model = SoftwareChoicesResultsViewModel(
        vendorsWithIntent = softwareChoicesService.getVendorsWithIntent(userFilters.finalFilters),
        isAgent = isAgent
      )

      if (isEnabled(ExplicitAudits)) auditService.auditSearchResults(userFilters, model.vendorsWithIntent.map(_.vendor.name))
      Ok(view(model, FiltersForm.form.fill(filters)))
    }
  }

  def clear(): Action[AnyContent] = (identify andThen requireData).async { request =>
    given Request[AnyContent] = request
    update(FiltersFormModel())(request) map { _ =>
      Redirect(routes.SearchSoftwareController.show())
    }
  }

  private def update(search: FiltersFormModel)(implicit request: SessionDataRequest[_]): Future[Boolean] = {
    val filtersFromAnswers = pageAnswersService
      .getFiltersFromAnswers(request.userFilters.answers)
      .filterNot(_ == VendorFilter.Agent)

    userFiltersRepository.set(request.userFilters.copy(finalFilters = filtersFromAnswers ++ search.filters))
  }

  private def view(model: SoftwareChoicesResultsViewModel, form: Form[FiltersFormModel])
                  (implicit request: Request[_]): Html = {

    searchSoftwareView(
      viewModel = model,
      searchForm = form,
      postAction = routes.SearchSoftwareController.search(),
      clearAction = routes.SearchSoftwareController.clear(),
      backUrl = backLinkUrl(model.isAgent)
    )
  }

  def backLinkUrl(isAgent: Boolean): String = {
    if (isAgent) routes.UserTypeController.show().url
    else routes.ChoosingSoftwareController.show().url
  }

}
