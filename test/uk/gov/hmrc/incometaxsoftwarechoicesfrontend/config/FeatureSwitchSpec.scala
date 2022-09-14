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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config

import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.{BetaFeatures, IncomeAndDeduction}

class FeatureSwitchSpec extends PlaySpec {

  "FeatureSwitch.get" when {
    "the beta features feature switch exists" should {
      "return the feature switch" in {
        FeatureSwitch.get("enable-beta-features") mustBe Some(BetaFeatures)
      }
    }
    "the income and deduction feature switch exists" should {
      "return the feature switch" in {
        FeatureSwitch.get("enable-income-and-deduction") mustBe Some(IncomeAndDeduction)
      }
    }
  }
  "the requested feature switch does not exists" should {
    "return none" in {
      FeatureSwitch.get("does-not-exist") mustBe None
    }
  }

}
