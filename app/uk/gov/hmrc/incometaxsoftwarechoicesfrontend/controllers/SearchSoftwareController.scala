/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{FiltersFormModel, SoftwareVendors}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.SoftwareChoicesService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.{SearchSoftwarePage, SoftwareVendorsTemplate}

import javax.inject.{Inject, Singleton}

@Singleton
class SearchSoftwareController @Inject()(mcc: MessagesControllerComponents,
                                         searchSoftwarePage: SearchSoftwarePage,
                                         softwareVendorsTemplate: SoftwareVendorsTemplate,
                                         softwareChoicesService: SoftwareChoicesService) extends BaseFrontendController(mcc) {

  val show: Action[AnyContent] = Action { implicit request =>
    val vendors: SoftwareVendors = softwareChoicesService.softwareVendors
    Ok(view(vendors))
  }

  val search: Action[AnyContent] = Action { implicit request =>
    FiltersForm.form.bindFromRequest().fold(
      error => {
        val vendors: SoftwareVendors = softwareChoicesService.softwareVendors
        BadRequest(view(vendors, error))
      },
      search => {
        val vendors = softwareChoicesService.filterVendors(search.searchTerm)
        Ok(view(vendors, FiltersForm.form.fill(search)))
      }
    )
  }

  val ajaxSearch: Action[AnyContent] = Action { implicit request =>
    FiltersForm.form.bindFromRequest().fold(
      error => {
        val vendors: SoftwareVendors = softwareChoicesService.softwareVendors
        BadRequest(view(vendors, error))
      },
      search => {
        val vendors = softwareChoicesService.filterVendors(search.searchTerm)
        Ok(softwareVendorsTemplate(vendors))
      }
    )
  }

  private def view(vendors: SoftwareVendors, form: Form[FiltersFormModel] = FiltersForm.form)
                  (implicit request: Request[_]): Html =
    searchSoftwarePage(vendors, form, routes.SearchSoftwareController.search)

}
