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

import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitch.IntentFeature

class FeatureSwitchingSpec extends PlaySpec with FeatureSwitching with BeforeAndAfterEach with GuiceOneAppPerSuite {

  val mockConfig: Configuration = mock[Configuration]
  override val appConfig: AppConfig = mock[AppConfig]
  when(appConfig.config).thenReturn(mockConfig)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConfig)
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

  "Intent" should {
    "return true if Intent feature switch is enabled in sys.props" in {
      enable(IntentFeature)
      isEnabled(IntentFeature) mustBe true
    }
    "return false if Intent feature switch is disabled in sys.props" in {
      disable(IntentFeature)
      isEnabled(IntentFeature) mustBe false
    }

    "return false if Intent feature switch does not exist" in {
      when(mockConfig.getOptional[String]("feature-switch.intent")).thenReturn(None)
      isEnabled(IntentFeature) mustBe false
    }

    "return false if Intent feature switch is not in sys.props but is set to off in config" in {
      when(mockConfig.getOptional[String]("feature-switch.intent")).thenReturn(Some(FEATURE_SWITCH_OFF))
      isEnabled(IntentFeature) mustBe false
    }

    "return true if Intent feature switch is not in sys.props but is set to on in config" in {
      when(mockConfig.getOptional[String]("feature-switch.intent")).thenReturn(Some(FEATURE_SWITCH_ON))
      isEnabled(IntentFeature) mustBe true
    }
  }
}
