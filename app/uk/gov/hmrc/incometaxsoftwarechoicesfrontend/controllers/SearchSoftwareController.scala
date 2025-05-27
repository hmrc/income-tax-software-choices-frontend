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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{FiltersFormModel, SoftwareVendors, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.SoftwareChoicesService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwarePage

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchSoftwareController @Inject()(mcc: MessagesControllerComponents,
                                         val appConfig: AppConfig,
                                         searchSoftwarePage: SearchSoftwarePage,
                                         softwareChoicesService: SoftwareChoicesService,
                                         userFiltersRepository: UserFiltersRepository,
                                         implicit val ec: ExecutionContext) extends BaseFrontendController(mcc) with FeatureSwitching {

  val show: Action[AnyContent] = Action { implicit request => Ok(view(softwareChoicesService.getVendors(), FiltersForm.form)) }

  def search: Action[AnyContent] = Action.async { implicit request =>
    FiltersForm.form.bindFromRequest().fold(
      error => Future.successful(BadRequest(view(softwareChoicesService.getVendors(), error))),
      search => update(search) map { _ =>
        val vendors = softwareChoicesService.getVendors(search.searchTerm, search.filters)
        Ok(view(vendors, FiltersForm.form.fill(search)))
      }
    )
  }

  def clear: Action[AnyContent] = Action.async { implicit request =>
    update(FiltersFormModel()) map { _ =>
      Redirect(routes.SearchSoftwareController.show)
    }
  }

  private def update(search: FiltersFormModel)(implicit request: Request[_]): Future[Boolean] = {
    val session = request.session
    val sessionId = session.get("sessionId").getOrElse("")
    for {
      userFilters <- userFiltersRepository.get(sessionId)
      result <- userFilters match {
        case Some(userFilters) => userFiltersRepository.set(userFilters.copy(finalFilters = search.filters))
        case None => userFiltersRepository.set(UserFilters(sessionId, search.filters))
      }
    } yield {
      result
    }
  }

  private def view(vendors: SoftwareVendors, form: Form[FiltersFormModel])
                  (implicit request: Request[_]): Html = {

    val betaEnabled: Boolean = isEnabled(BetaFeatures)
    val extraPricingOptionsEnabled: Boolean = isEnabled(ExtraPricingOptions)
    val overseasPropertyEnabled: Boolean = isEnabled(DisplayOverseasProperty)

    searchSoftwarePage(
      vendors,
      form,
      routes.SearchSoftwareController.search,
      routes.SearchSoftwareController.clear,
      betaEnabled,
      extraPricingOptionsEnabled,
      overseasPropertyEnabled
    )
  }

}
