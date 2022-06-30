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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models

import play.api.libs.json.{Reads, __}

sealed trait Filter {
  val key: String
}

object Filter {

  case object FreeVersion extends Filter {
    override val key: String = "free-version"
  }

  case object FreeTrail extends Filter {
    override val key: String = "free-trail"
  }

  case object Individual extends Filter {
    override val key: String = "individual"
  }

  case object Agent extends Filter {
    override val key: String = "agent"
  }

  case object MicrosoftWindows extends Filter {
    override val key: String = "microsoft-windows"
  }

  case object MacOS extends Filter {
    override val key: String = "mac-os"
  }

  case object Android extends Filter {
    override val key: String = "android"
  }

  case object AppleIOS extends Filter {
    override val key: String = "apple-ios"
  }

  case object BrowserBased extends Filter {
    override val key: String = "browser-based"
  }

  case object ApplicationBased extends Filter {
    override val key: String = "application-based"
  }

  case object Visual extends Filter {
    override val key: String = "visual"
  }

  case object Hearing extends Filter {
    override val key: String = "hearing"
  }

  case object Motor extends Filter {
    override val key: String = "motor"
  }

  case object Cognitive extends Filter {
    override val key: String = "cognitive"
  }

  val filterKeyToFilter: Map[String, Filter] = Map(
    FreeVersion.key -> FreeVersion,
    FreeTrail.key -> FreeTrail,
    Individual.key -> Individual,
    Agent.key -> Agent,
    MicrosoftWindows.key -> MicrosoftWindows,
    MacOS.key -> MacOS,
    Android.key -> Android,
    AppleIOS.key -> AppleIOS,
    BrowserBased.key -> BrowserBased,
    ApplicationBased.key -> ApplicationBased,
    Visual.key -> Visual,
    Hearing.key -> Hearing,
    Motor.key -> Motor,
    Cognitive.key -> Cognitive
  )

  implicit val reads: Reads[Filter] = __.read[String] map filterKeyToFilter

}
