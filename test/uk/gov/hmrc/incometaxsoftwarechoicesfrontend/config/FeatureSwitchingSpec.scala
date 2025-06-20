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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Environment, Mode}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch._

class FeatureSwitchingSpec extends PlaySpec with FeatureSwitching with BeforeAndAfterEach with GuiceOneAppPerSuite {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  class Setup() {

    def config: Map[String, String] = Map(
      "metrics.enabled" -> "false"
    )

    val featureSwitching: FeatureSwitchingImpl = new GuiceApplicationBuilder()
      .in(Environment.simple(mode = Mode.Dev))
      .configure(config)
      .build()
      .injector
      .instanceOf[FeatureSwitchingImpl]
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    FeatureSwitch.switches foreach { switch =>
      sys.props -= switch.name
    }
  }

  "FeatureSwitching constants" should {
    "be true and false" in {
      FEATURE_SWITCH_ON mustBe "true"
      FEATURE_SWITCH_OFF mustBe "false"
    }
  }
}
