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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers

import org.scalatest.GivenWhenThen
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.FiltersForm
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.baseURI
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FiltersFormModel

trait ComponentSpecBase extends AnyWordSpec
  with GivenWhenThen
  with Matchers
  with CustomMatchers
  with ScalaFutures
  with IntegrationPatience
  with GuiceOneServerPerSuite
  with FeatureSwitching {

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  private val wsClient = app.injector.instanceOf[WSClient]
  private val baseUrl = s"http://localhost:$port"

  def config: Map[String, Any] = Map(
    "metrics.enabled" -> false
  )

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(config)
      .build()

  implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  object SoftwareChoicesFrontend {
    def startPage(): WSResponse = get("/")

    def productDetails(name: String): WSResponse = get(s"/product-details?software=$name")

    def submitSearch(search: FiltersFormModel): WSResponse = post("/")(
      FiltersForm.form.fill(search).data.map { case (k, v) => (k, Seq(v)) }
    )

    def submitAjaxSearch(search: FiltersFormModel): WSResponse = post("/ajax")(
      FiltersForm.form.fill(search).data.map { case (k, v) => (k, Seq(v)) }
    )

    def healthcheck(): WSResponse =
      wsClient
        .url(s"$baseUrl/ping/ping")
        .get()
        .futureValue

    private def get(uri: String): WSResponse = {
      buildClient(uri)
        .get()
        .futureValue
    }

    def post(uri: String)(body: Map[String, Seq[String]]): WSResponse =
      buildClient(uri)
        .withHttpHeaders("Csrf-Token" -> "nocheck")
        .post(body)
        .futureValue

    private def buildClient(path: String) = {
      wsClient
        .url(s"$baseUrl$baseURI$path")
    }
  }
}
