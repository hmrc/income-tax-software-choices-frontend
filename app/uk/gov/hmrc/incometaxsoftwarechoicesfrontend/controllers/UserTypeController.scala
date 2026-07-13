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

import play.api.mvc.*
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.{AppConfig, SCInconsistentDataException}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.{RequireUserDataRefiner, SessionIdentifierAction}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.UserTypeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.UserTypeForm.userTypeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.{Check, Find, ViewAll}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.{FutureVendor, Spreadsheet, Unrecognised}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.{Agent, SoleTraderOrLandlord}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{JourneyType, SoftwareProduct, UserAnswers, UserType}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.{EnterSoftwareNamePage, HowYouFindSoftwarePage, UserTypePage}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories.UserFiltersRepository
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.PageAnswersService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.UserTypeView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UserTypeController @Inject()(view: UserTypeView,
                                   pageAnswersService: PageAnswersService,
                                   userFiltersRepository: UserFiltersRepository,
                                   identify: SessionIdentifierAction,
                                   requireData: RequireUserDataRefiner,
                                   val appConfig: AppConfig)
                                  (implicit ec: ExecutionContext,
                                   mcc: MessagesControllerComponents) extends BaseFrontendController {

  def show(editMode: Boolean = false): Action[AnyContent] = (identify andThen requireData).async { request =>
    given Request[AnyContent] = request
    for {
      userFilters <- userFiltersRepository.get(request.sessionId)
      answers = userFilters.flatMap(_.answers)
      userTypeAnswer = pageAnswersService.getPageAnswers(answers, UserTypePage)
      product = pageAnswersService.getPageAnswers(answers, EnterSoftwareNamePage)
    } yield {
      Ok(view(
        userTypeForm = UserTypeForm.userTypeForm.fill(userTypeAnswer),
        postAction = routes.UserTypeController.submit(editMode),
        backUrl = backUrl(answers, editMode),
        softwareName = getSoftwareName(product)
      ))
    }
  }

  def submit(editMode: Boolean = false): Action[AnyContent] = (identify andThen requireData).async { request =>
    given Request[AnyContent] = request

    val journey = pageAnswersService.getPageAnswers(request.sessionId, HowYouFindSoftwarePage)

    userTypeForm.bindFromRequest().fold(
      formWithErrors => {
        for {
          userFilters <- userFiltersRepository.get(request.sessionId)
          answers = userFilters.flatMap(_.answers)
          product = pageAnswersService.getPageAnswers(answers, EnterSoftwareNamePage)
        } yield {
          BadRequest(view(
            userTypeForm = formWithErrors,
            postAction = routes.UserTypeController.submit(editMode),
            backUrl = backUrl(answers, editMode),
            softwareName = getSoftwareName(product)
          ))
        }
      },
      userType => journey.flatMap {
        case Some(Find) | Some(Check) =>
          pageAnswersService.setPageAnswers(request.sessionId, UserTypePage, userType).map {
            case true =>
              if (editMode) Redirect(routes.CheckYourAnswersController.show())
              else Redirect(routes.BusinessIncomeController.show())
            case false => throw new InternalServerException("[UserTypeController][submit] - Could not save user type for find or check journey")
          }
        case Some(ViewAll) =>
          for {
            setPageAnswers <- pageAnswersService.setPageAnswers(request.sessionId, UserTypePage, userType)
            saveFiltersFromAnswers <- pageAnswersService.saveFiltersFromAnswers(request.sessionId)
          } yield {
            if (setPageAnswers && saveFiltersFromAnswers.nonEmpty) {
              Redirect(routes.SearchSoftwareController.show())
            } else {
              throw new InternalServerException("[UserTypeController][submit] - Could not save user type for view all journey")
            }
          }
        case _ =>
          throw new SCInconsistentDataException("[UserTypeController][submit] - No journey type")
      }
    )
  }

  private def backUrl(answers: Option[UserAnswers], editMode: Boolean): String = {
    val journeyOpt = pageAnswersService.getPageAnswers(answers, HowYouFindSoftwarePage)
    val productType = pageAnswersService.getPageAnswers(answers, EnterSoftwareNamePage).map(_.softwareType)

    (editMode, journeyOpt) match {
      case (true, _) => routes.CheckYourAnswersController.show().url
      case (false, Some(Check)) =>
        productType match {
          case Some(Spreadsheet) => routes.NeedAdditionalSoftwareController.show().url
          case Some(FutureVendor) => routes.SoftwareInDevelopmentController.show().url
          case Some(Unrecognised) => routes.NoSoftwareListedController.show().url
          case _ => routes.EnterSoftwareNameController.show().url
        }
      case (false, _) => routes.HowYouFindSoftwareController.show().url
    }
  }

}
