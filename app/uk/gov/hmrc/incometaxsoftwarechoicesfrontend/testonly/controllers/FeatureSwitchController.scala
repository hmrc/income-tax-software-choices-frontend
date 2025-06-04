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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.testonly.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.switches
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.{FeatureSwitch, FeatureSwitching}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers.BaseFrontendController
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.testonly.views.html.FeatureSwitchSettings

import javax.inject.Inject
import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext

class FeatureSwitchController @Inject()(mcc: MessagesControllerComponents, featureSwitchSettings: FeatureSwitchSettings)
                                       (implicit val ec: ExecutionContext,
                                        val appConfig: AppConfig) extends BaseFrontendController(mcc) with FeatureSwitching {

  private def view(switchNames: Map[FeatureSwitch, Boolean])(implicit request: Request[_]): Html =
    featureSwitchSettings(
      switchNames = switchNames,
      routes.FeatureSwitchController.submit
    )

  lazy val show: Action[AnyContent] = Action { implicit request =>
    val featureSwitches = ListMap(switches.toSeq sortBy (_.displayText) map (switch => switch -> isEnabled(switch)): _*)
    Ok(view(featureSwitches))
  }

  lazy val submit: Action[AnyContent] = Action { implicit req =>
    val submittedData: Set[String] = req.body.asFormUrlEncoded match {
      case None => Set.empty
      case Some(data) => data.getOrElse(FeatureSwitch.prefix, Seq.empty).toSet
    }

    val frontendFeatureSwitches = submittedData flatMap FeatureSwitch.get

    switches.foreach(fs =>
      if (frontendFeatureSwitches.contains(fs)) enable(fs)
      else disable(fs)
    )

    Redirect(routes.FeatureSwitchController.show)
  }

}
