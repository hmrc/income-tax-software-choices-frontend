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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.IntentFeature
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.{RequireUserDataRefiner, SessionIdentifierAction}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.Agent
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.requests.SessionDataRequest
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{FiltersFormModel, UserFilters, VendorFilter}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.UserTypePage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.{PageAnswersService, SoftwareChoicesService}
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
                                         requireData: RequireUserDataRefiner)
                                        (implicit ec: ExecutionContext,
                                         val appConfig: AppConfig,
                                         mcc: MessagesControllerComponents) extends BaseFrontendController with FeatureSwitching {

  def show(zeroResults: Boolean): Action[AnyContent] = (identify andThen requireData) { request =>
    given Request[AnyContent] = request

    val finalFilters = request.userFilters.finalFilters
    val isAgent = pageAnswersService.getPageAnswers(request.userFilters, UserTypePage).eq(Agent)

    Ok(view(
      model = SoftwareChoicesResultsViewModel(
        allInOneVendors = softwareChoicesService.getAllInOneVendors(filters = finalFilters),
        otherVendors = softwareChoicesService.getOtherVendors(filters = finalFilters, isAgent || zeroResults),
        vendorsWithIntent = if (isEnabled(IntentFeature)) softwareChoicesService.getVendorsWithIntent(filters = finalFilters) else Seq.empty,
        zeroResults = zeroResults,
        isAgent = isAgent
      ),
      form = FiltersForm.form.fill(FiltersFormModel(filters = finalFilters))
    ))
  }

  def search(zeroResults: Boolean): Action[AnyContent] = (identify andThen requireData).async { request =>
    given Request[AnyContent] = request
    val filters = FiltersForm.form.bindFromRequest().get
    for {
      userFilters <- update(filters)(request).map(_ => request.userFilters)
    } yield {
      val isAgent = pageAnswersService.getPageAnswers(request.userFilters, UserTypePage).eq(Agent)
      val model = SoftwareChoicesResultsViewModel(
        allInOneVendors = softwareChoicesService.getAllInOneVendors(userFilters.finalFilters),
        otherVendors = softwareChoicesService.getOtherVendors(userFilters.finalFilters, isAgent || zeroResults),
        vendorsWithIntent = if (isEnabled(IntentFeature)) softwareChoicesService.getVendorsWithIntent(userFilters.finalFilters) else Seq.empty,
        zeroResults = zeroResults,
        isAgent = isAgent
      )
      Ok(view(model, FiltersForm.form.fill(filters)))
    }
  }
  

  def clear(zeroResults: Boolean): Action[AnyContent] = (identify andThen requireData).async { request =>
    given Request[AnyContent] = request

    update(FiltersFormModel())(request) map { _ =>
      Redirect(routes.SearchSoftwareController.show(zeroResults))
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
      postAction = routes.SearchSoftwareController.search(model.zeroResults),
      clearAction = routes.SearchSoftwareController.clear(model.zeroResults),
      backUrl = backLinkUrl(model.isAgent, model.zeroResults)
    )
  }

  def backLinkUrl(isAgent: Boolean, zeroResults: Boolean): String = {
    (isAgent, isEnabled(IntentFeature), zeroResults) match {
      case (true, _, _) => routes.UserTypeController.show().url
      case (false, true, _) => routes.ChoosingSoftwareController.show().url
      case (false, false, true) => routes.ZeroSoftwareResultsController.show().url
      case (false, false, false) => routes.CheckYourAnswersController.show().url
    }
  }
}
