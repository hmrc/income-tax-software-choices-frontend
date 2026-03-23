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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.{RequireUserDataRefiner, SessionIdentifierAction}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.CheckSoftwareForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.SoftwareChoicesService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.helpers.SelectBuilder
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.CheckSoftwareView

import javax.inject.{Inject, Singleton}

@Singleton
class CheckSoftwareController @Inject()(view: CheckSoftwareView,
                                        identify: SessionIdentifierAction,
                                        requireData: RequireUserDataRefiner,
                                        softwareChoicesService: SoftwareChoicesService
                                       )(implicit mcc: MessagesControllerComponents) extends BaseFrontendController with SelectBuilder {

  def show(): Action[AnyContent] = (identify andThen requireData) { request =>
    given Request[AnyContent] = request

    val vendors = softwareChoicesService.softwareVendors.vendors
    Ok(view(
      checkSoftwareForm = CheckSoftwareForm.form,
      selectOptions = buildSelects(vendors),
      postAction = routes.CheckSoftwareController.submit(),
      backLink = routes.UserTypeController.show().url
    ))
  }


  def submit(): Action[AnyContent] = (identify andThen requireData) { request =>
    given Request[AnyContent] = request

    val vendors = softwareChoicesService.softwareVendors.vendors
    CheckSoftwareForm.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(view(
          checkSoftwareForm = formWithErrors,
          selectOptions = buildSelects(vendors),
          postAction = routes.CheckSoftwareController.submit(),
          backLink = routes.UserTypeController.show().url
        ))
      },
      vendorName => {
        if (otherOptions.contains(vendorName)) {
          Redirect(routes.NotApprovedController.show())
        } else {
          Redirect(routes.SearchSoftwareController.show())
        }
      }
    )
  }

  private val otherOptions = Seq(
    "Google Sheets",
    "Microsoft Excel"
  )
  

}

