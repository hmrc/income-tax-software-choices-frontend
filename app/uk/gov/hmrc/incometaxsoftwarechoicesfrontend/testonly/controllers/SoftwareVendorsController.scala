/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.testonly.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.data.Form
import play.api.data.Forms.{boolean, optional, text, tuple}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.BaseFrontendController
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.SoftwareChoicesService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.testonly.{SoftwareVendorsCsv, SoftwareVendorsOverview}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.testonly.views.html.SoftwareVendorsView

import javax.inject.Inject

class SoftwareVendorsController @Inject()(softwareChoicesService: SoftwareChoicesService,
                                         softwareVendorsView: SoftwareVendorsView)
                                        (implicit mcc: MessagesControllerComponents) extends BaseFrontendController {

  private val filtersForm = Form(tuple(
    "q" -> optional(text).transform[String](_.getOrElse(""), value => Option.when(value.nonEmpty)(value)),
    "feature" -> optional(text.verifying("Choose a feature from the list", VendorFilter.filterKeyToFilter.contains)),
    "developmentOnly" -> boolean
  ))

  lazy val show: Action[AnyContent] = response(asCsv = false)
  lazy val download: Action[AnyContent] = response(asCsv = true)

  private def response(asCsv: Boolean): Action[AnyContent] = Action { implicit request =>
    val vendors = softwareChoicesService.softwareVendors
    filtersForm.bind(request.queryString.map { case (key, values) => key -> values.headOption.getOrElse("") }).fold(
      errors => BadRequest(softwareVendorsView(SoftwareVendorsOverview(vendors), errors)),
      { case (search, feature, developmentOnly) =>
        val overview = SoftwareVendorsOverview(vendors, search, feature.flatMap(VendorFilter.filterKeyToFilter.get), developmentOnly)
        if (asCsv) {
          val scope = if (search.nonEmpty || feature.nonEmpty || developmentOnly) "filtered" else "all"
          Ok(SoftwareVendorsCsv.render(overview))
            .as("text/csv; charset=utf-8")
            .withHeaders(CONTENT_DISPOSITION ->
              s"""attachment; filename="software-commitments-$scope-${java.time.LocalDate.now()}.csv"""")
        } else {
          Ok(softwareVendorsView(overview, filtersForm.fill((search, feature, developmentOnly))))
        }
      }
    )
  }
}
