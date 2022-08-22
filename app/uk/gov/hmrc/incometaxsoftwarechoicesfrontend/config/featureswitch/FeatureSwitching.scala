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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch

import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig

import javax.inject.{Inject, Singleton}

trait FeatureSwitching {

  def appConfig: AppConfig

  val FEATURE_SWITCH_ON: String = true.toString
  val FEATURE_SWITCH_OFF: String = false.toString

  def isEnabled(featureSwitch: FeatureSwitch): Boolean = {
    (sys.props.get(featureSwitch.name) orElse appConfig.config.getOptional[String](featureSwitch.name)) contains FEATURE_SWITCH_ON
  }

  def enable(featureSwitch: FeatureSwitch): Unit =
    sys.props += featureSwitch.name -> FEATURE_SWITCH_ON

  def disable(featureSwitch: FeatureSwitch): Unit =
    sys.props += featureSwitch.name -> FEATURE_SWITCH_OFF

}

@Singleton
class FeatureSwitchingImpl @Inject()(val appConfig: AppConfig) extends FeatureSwitching