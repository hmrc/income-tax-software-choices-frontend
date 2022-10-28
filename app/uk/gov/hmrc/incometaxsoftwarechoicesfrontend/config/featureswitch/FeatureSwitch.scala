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

import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.prefix

sealed trait FeatureSwitch {
  val key: String
  lazy val name: String = s"$prefix.$key"
  val displayText: String
}

object FeatureSwitch {

  val prefix = "feature-switch"

  val switches: Set[FeatureSwitch] = Set(
    BetaFeatures,
    IncomeAndDeduction,
    ExtraPricingOptions,
    DisplayOverseasProperty
  )

  def get(str: String): Option[FeatureSwitch] = switches find (_.key == str)

  case object BetaFeatures extends FeatureSwitch {
    override val key = "enable-beta-features"
    override val displayText = "Beta Features"
  }

  case object IncomeAndDeduction extends FeatureSwitch {
    override val key = "enable-income-and-deduction"
    override val displayText = "Income and Deduction"
  }

  case object ExtraPricingOptions extends FeatureSwitch {
    override val key = "enable-extra-pricing-options"
    override val displayText = "Extra Pricing Options"
  }

  case object DisplayOverseasProperty extends FeatureSwitch {
    override val key = "enable-overseasproperty-option"
    override val displayText = "Overseas Property Option"
  }

}
