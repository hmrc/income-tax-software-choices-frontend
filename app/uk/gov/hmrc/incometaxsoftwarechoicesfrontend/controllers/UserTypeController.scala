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
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.CheckJourney
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.actions.{RequireUserDataRefiner, SessionIdentifierAction}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.UserTypeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.UserTypeForm.userTypeForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.JourneyType.{Check, Find, ViewAll}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.SoftwareType.{FutureVendor, Recognised, Spreadsheet, Unrecognised}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{JourneyType, UserType}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserType.{Agent, SoleTraderOrLandlord}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.requests.SessionRequest
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.{EnterSoftwareNamePage, HowYouFindSoftwarePage, UserTypePage}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services.PageAnswersService
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.UserTypeView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserTypeController @Inject()(view: UserTypeView,
                                   pageAnswersService: PageAnswersService,
                                   identify: SessionIdentifierAction,
                                   requireData: RequireUserDataRefiner,
                                   val appConfig: AppConfig)
                                  (implicit ec: ExecutionContext,
                                   mcc: MessagesControllerComponents) extends BaseFrontendController with FeatureSwitching {

  def show(editMode: Boolean = false): Action[AnyContent] = identifyAndRequireData.async { request =>
    given Request[AnyContent] = request
    for {
      pageAnswers <- pageAnswersService.getPageAnswers(request.sessionId, UserTypePage)
    } yield {
      Ok(view(
        userTypeForm = UserTypeForm.userTypeForm.fill(pageAnswers),
        postAction = routes.UserTypeController.submit(editMode),
        backUrl = backUrl(editMode, request)
      ))
    }
  }

  def submit(editMode: Boolean = false): Action[AnyContent] = identifyAndRequireData.async { request =>
    given Request[AnyContent] = request

    val journey = pageAnswersService.getPageAnswers(request.sessionId, HowYouFindSoftwarePage)

    userTypeForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(
          BadRequest(view(
            userTypeForm = formWithErrors,
            postAction = routes.UserTypeController.submit(editMode),
            backUrl = backUrl(editMode, request)
          ))
        )
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
          case None if userType == SoleTraderOrLandlord =>
            pageAnswersService.setPageAnswers(request.sessionId, UserTypePage, userType).map {
              case true => Redirect(routes.BusinessIncomeController.show())
              case false => throw new InternalServerException("[UserTypeController][submit] - Could not save sole trader or landlord user type")
            }
          case None if userType == Agent =>
            for {
              resetUserAnswers <- pageAnswersService.resetUserAnswers(request.sessionId)
              setPageAnswers <- pageAnswersService.setPageAnswers(request.sessionId, UserTypePage, userType)
              saveFiltersFromAnswers <- pageAnswersService.saveFiltersFromAnswers(request.sessionId)
            } yield {
              if (resetUserAnswers && setPageAnswers && saveFiltersFromAnswers.nonEmpty) {
                Redirect(routes.SearchSoftwareController.show())
              } else {
                throw new InternalServerException("[UserTypeController][submit] - Could not save agent user type")
              }
            }
        }
    )
  }

  private def identifyAndRequireData: ActionBuilder[SessionRequest, AnyContent] = {
    if (isEnabled(CheckJourney)) identify andThen requireData
    else identify
  }

  def backUrl(editMode: Boolean = false, request: SessionRequest[AnyContent]): String = {
    for {
      journey <- pageAnswersService.getPageAnswers(request.sessionId, HowYouFindSoftwarePage)
      product <- pageAnswersService.getPageAnswers(request.sessionId, EnterSoftwareNamePage)
    } yield {
      if (editMode) routes.CheckYourAnswersController.show().url
      else
        journey match {
          case Some(Find) | Some(ViewAll) => routes.HowYouFindSoftwareController.show().url
          case Some(Check) =>
            product.getOrElse(throw new InternalServerException("[UserTypeController][backUrl] - Check journey with no answer from HowYouFindSoftwarePage")).softwareType match {
              case Recognised => routes.EnterSoftwareNameController.show().url
              case Spreadsheet => routes.NeedAdditionalSoftwareController.show().url
              case FutureVendor => routes.SoftwareInDevelopmentController.show().url
              case Unrecognised => routes.NoSoftwareListedController.show().url
            }
          case None => appConfig.guidance
        }
    }
    
  }
}
