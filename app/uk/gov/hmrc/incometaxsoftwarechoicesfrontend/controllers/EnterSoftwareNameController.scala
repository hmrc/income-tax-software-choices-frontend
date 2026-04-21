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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.EnterSoftwareNameForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.OtherSoftware
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.EnterSoftwareNamePage
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.{DataService, PageAnswersService}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.helpers.SelectBuilder
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.EnterSoftwareNameView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnterSoftwareNameController @Inject()(view: EnterSoftwareNameView,
                                            identify: SessionIdentifierAction,
                                            requireData: RequireUserDataRefiner,
                                            pageAnswersService: PageAnswersService,
                                            dataService: DataService
                                       )(implicit mcc: MessagesControllerComponents,
                                         ec: ExecutionContext) extends BaseFrontendController with SelectBuilder {

  private val recognisedProducts = dataService.getSoftwareVendors().vendors.map(v => OtherSoftware(v.productId, v.name, Recognised))
  private val futureProducts = dataService.getOtherSoftware().filter(_.softwareType.eq(FutureVendor))
  private val spreadsheetProducts = dataService.getOtherSoftware().filter(_.softwareType.eq(Spreadsheet))
  private val allProducts = recognisedProducts ++ futureProducts ++ spreadsheetProducts

  def show(): Action[AnyContent] = (identify andThen requireData) { request =>
    given Request[AnyContent] = request

    val pageAnswers = pageAnswersService.getPageAnswers(request.userFilters.answers, EnterSoftwareNamePage)

    Ok(view(
      enterSoftwareNameForm = EnterSoftwareNameForm.form.fill(pageAnswers.map(_.productId)),
      selectOptions = buildSelects(allProducts),
      postAction = routes.EnterSoftwareNameController.submit(),
      notListedLink = routes.NoSoftwareListedController.show().url,
      backLink = routes.HowYouFindSoftwareController.show().url
    ))
  }

  def submit(): Action[AnyContent] = (identify andThen requireData).async { request =>
    given Request[AnyContent] = request

    EnterSoftwareNameForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(view(
          enterSoftwareNameForm = formWithErrors,
          selectOptions = buildSelects(allProducts),
          postAction = routes.EnterSoftwareNameController.submit(),
          notListedLink = routes.NoSoftwareListedController.show().url,
          backLink = routes.HowYouFindSoftwareController.show().url
        )))
      },
      productId => {
        val selectedProduct  = allProducts.find(_.productId == productId).getOrElse(OtherSoftware(productId, "Unknown", Unrecognised))

        pageAnswersService.setPageAnswers(request.sessionId, EnterSoftwareNamePage, selectedProduct).map {
          case true => redirect(selectedProduct)
          case false => InternalServerError("[EnterSoftwareNameController][submit] - Could not save product]")
        }
      }
    )
  }

  private def redirect(selectedProduct: OtherSoftware) = {
    selectedProduct match {
      case product if product.softwareType == Recognised => Redirect(routes.UserTypeController.show())
      case product if product.softwareType == FutureVendor => Redirect(routes.EnterSoftwareNameController.show())
      case product if product.softwareType == Spreadsheet => Redirect(routes.NeedAdditionalSoftwareController.show())
      case _ => Redirect(routes.NoSoftwareListedController.show())
    }
  }

}

