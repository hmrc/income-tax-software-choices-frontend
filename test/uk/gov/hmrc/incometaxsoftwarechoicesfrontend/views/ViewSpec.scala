/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views

import org.jsoup.nodes.Element
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig

import scala.jdk.CollectionConverters._

trait ViewSpec extends AnyWordSpecLike with Matchers with GuiceOneAppPerSuite {

  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit lazy val mockMessages: Messages = messagesApi.preferred(FakeRequest())

  implicit val request: Request[_] = FakeRequest()

  implicit class CustomSelectors(element: Element) {

    lazy val getTechnicalHelpLink: String = element.getElementsByClass("hmrc-report-technical-issue").attr("href")

    lazy val getTechnicalHelpLinkText: String = element.getElementsByClass("hmrc-report-technical-issue").text

    def selectHead(selector: String): Element = element.select(selector).asScala.headOption match {
      case Some(element) => element
      case None => fail(s"No elements returned for selector: $selector")
    }

    def selectSeq(selector: String): Seq[Element] = element.select(selector).asScala.toSeq

    def selectOptionally(selector: String): Option[Element] = element.select(selector).asScala.headOption

    def selectNth(selector: String, nth: Int): Element = selectHead(s"$selector:nth-of-type($nth)")

    def selectNthOptionally(selector: String, nth: Int): Option[Element] = selectOptionally(s"$selector:nth-of-type($nth)")

    def mainContent: Element = element.selectHead("main")

  }
}
