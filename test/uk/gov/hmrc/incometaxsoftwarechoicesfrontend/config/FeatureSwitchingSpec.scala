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

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Environment, Mode}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.BetaFeatures
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.{FeatureSwitch, FeatureSwitchingImpl}

class FeatureSwitchingSpec extends PlaySpec with BeforeAndAfterEach with GuiceOneAppPerSuite {

  class Setup(betaFeatures: Boolean = false) {

    def config: Map[String, String] = Map(
      "feature-switch.enable-beta-features" -> betaFeatures.toString,
      "metrics.enabled" -> "false"
    )

    val featureSwitching: FeatureSwitchingImpl = new GuiceApplicationBuilder()
      .in(Environment.simple(mode = Mode.Dev))
      .configure(config)
      .build
      .injector
      .instanceOf[FeatureSwitchingImpl]

  }


  override def beforeEach(): Unit = {
    super.beforeEach()
    FeatureSwitch.switches foreach { switch =>
      sys.props -= switch.name
    }
  }

  "BetaFeatures" should {
    "return true if BetaFeatures feature switch is enabled in sys.props" in new Setup {
      featureSwitching.enable(BetaFeatures)
      featureSwitching.isEnabled(BetaFeatures) mustBe true
    }
    "return false if BetaFeatures feature switch is disabled in sys.props" in new Setup {
      featureSwitching.disable(BetaFeatures)
      featureSwitching.isEnabled(BetaFeatures) mustBe false
    }

    "return false if BetaFeatures feature switch is not in sys.props but is set to off in config" in new Setup(betaFeatures = false) {
      featureSwitching.isEnabled(BetaFeatures) mustBe false
    }

    "return true if BetaFeatures feature switch is not in sys.props but is set to on in config" in new Setup(betaFeatures = true) {
      featureSwitching.isEnabled(BetaFeatures) mustBe true
    }
  }
}
