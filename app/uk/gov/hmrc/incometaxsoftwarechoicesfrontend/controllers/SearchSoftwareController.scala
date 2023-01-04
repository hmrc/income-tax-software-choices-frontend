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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{FiltersFormModel, SoftwareVendors}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.SoftwareChoicesService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.SearchSoftwarePage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.templates.{SoftwareVendorsTemplate, SoftwareVendorsTemplateAlpha}

import javax.inject.{Inject, Singleton}

@Singleton
class SearchSoftwareController @Inject()(mcc: MessagesControllerComponents,
                                         val appConfig: AppConfig,
                                         searchSoftwarePage: SearchSoftwarePage,
                                         softwareVendorsTemplateAlpha: SoftwareVendorsTemplateAlpha,
                                         softwareVendorsTemplate: SoftwareVendorsTemplate,
                                         softwareChoicesService: SoftwareChoicesService) extends BaseFrontendController(mcc) with FeatureSwitching {

  val show: Action[AnyContent] = Action { implicit request => Ok(view(softwareChoicesService.getVendors(), ajax = false, FiltersForm.form)) }

  def search(ajax: Boolean): Action[AnyContent] = Action { implicit request =>
    FiltersForm.form.bindFromRequest().fold(
      error => BadRequest(view(softwareChoicesService.getVendors(), ajax, error)),
      search => {
        val vendors = softwareChoicesService.getVendors(search.searchTerm, search.filters)
        Ok(view(vendors, ajax, FiltersForm.form.fill(search)))
      }
    )
  }

  private def view(vendors: SoftwareVendors, ajax: Boolean, form: Form[FiltersFormModel])
                  (implicit request: Request[_]): Html = {

    val betaEnabled: Boolean = isEnabled(BetaFeatures)
    val extraPricingOptionsEnabled: Boolean = isEnabled(ExtraPricingOptions)
    val overseasPropertyEnabled: Boolean = isEnabled(DisplayOverseasProperty)

    (ajax, isEnabled(BetaFeatures)) match {
      case (true, true) => softwareVendorsTemplate(vendors)
      case (true, false) => softwareVendorsTemplateAlpha(vendors, extraPricingOptionsEnabled, overseasPropertyEnabled)
      case _ =>
        searchSoftwarePage(
          vendors,
          form,
          routes.SearchSoftwareController.search(ajax),
          betaEnabled,
          extraPricingOptionsEnabled,
          overseasPropertyEnabled
        )
    }
  }

}
