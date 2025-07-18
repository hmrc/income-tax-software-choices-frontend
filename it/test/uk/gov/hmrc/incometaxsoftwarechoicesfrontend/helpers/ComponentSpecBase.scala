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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers

import org.scalatest.GivenWhenThen
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.crypto.CookieSigner
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.{AccountingPeriodForm, AdditionalIncomeForm, BusinessIncomeForm, FiltersForm, OtherItemsForm, UserTypeForm}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.baseURI
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{AccountingPeriod, FiltersFormModel, UserType, VendorFilter}

trait ComponentSpecBase extends AnyWordSpec
  with GivenWhenThen
  with Matchers
  with CustomMatchers
  with ScalaFutures
  with IntegrationPatience
  with GuiceOneServerPerSuite
  with SessionCookieBaker {

  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val cookieSignerCache: Application => CookieSigner = Application.instanceCache[CookieSigner]
  override lazy val cookieSigner: CookieSigner = cookieSignerCache(app)

  private val wsClient = app.injector.instanceOf[WSClient]
  private val baseUrl = s"http://localhost:$port"

  def config: Map[String, Any] = Map(
    "metrics.enabled" -> false,
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
  )

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(config)
      .build()

  implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  object SoftwareChoicesFrontend {

    def index(): WSResponse = get("/")

    def clear(): WSResponse = get("/clear")

    def productDetails(name: String): WSResponse = get(s"/product-details/$name")

    def getBusinessIncome: WSResponse = get("/business-income")

    def postBusinessIncome(pageAnswers: Seq[VendorFilter], editMode: Boolean = false): WSResponse = post(s"/business-income?editMode=$editMode")(
      BusinessIncomeForm.form.fill(pageAnswers).data.map { case (k, v) => (k, Seq(v)) }
    )

    def getAdditionalIncome: WSResponse = get("/additional-income")

    def submitAdditionalIncome(maybeKeys: Option[Seq[String]], editMode: Boolean = false): WSResponse = {
      val body: Map[String, Seq[String]] = maybeKeys match {
        case Some(keys) if keys.nonEmpty => Map("additionalIncome[]" -> keys)
        case Some(_) => Map("additionalIncome[]" -> Seq(AdditionalIncomeForm.noneKey))
        case None => Map.empty
      }
      post(s"/additional-income?editMode=$editMode")(body)
    }

    def getOtherItems: WSResponse = get("/other-items")

    def postOtherItems(maybeKeys: Option[Seq[String]], editMode: Boolean = false): WSResponse = {
      val body: Map[String, Seq[String]] = maybeKeys match {
        case Some(keys) if keys.nonEmpty => Map("otherItems[]" -> keys)
        case Some(_) => Map("otherItems[]" -> Seq(OtherItemsForm.noneKey))
        case None => Map.empty
      }
      post(s"/other-items?editMode=$editMode")(body)
    }

    def getUnsupportedAccountingPeriod: WSResponse = get("/unsupported-accounting-period")

    def getAccountingPeriod: WSResponse = get("/accounting-period")

    def submitAccountingPeriod(request: Option[AccountingPeriod]): WSResponse = {
      post("/accounting-period")(
        request.fold(Map.empty[String, Seq[String]])(
          model => AccountingPeriodForm.accountingPeriodForm.fill(model).data.map { case (k, v) => (k, Seq(v)) }
        )
      )
    }

    def getUserType: WSResponse = get("/type-of-user")

    def submitUserType(request: Option[UserType]): WSResponse = {
      post("/type-of-user")(
        request.fold(Map.empty[String, Seq[String]])(
          model => UserTypeForm.userTypeForm.fill(model).data.map { case (k, v) => (k, Seq(v)) }
        )
      )
    }

    def getCheckYourAnswers: WSResponse = get("/check-your-answers")

    def postCheckYourAnswers(): WSResponse = post("/check-your-answers")(Map.empty)

    def getSoftwareResults: WSResponse = get("/software-results")

    def submitSoftwareSearch(search: FiltersFormModel): WSResponse = post("/software-results") {
      FiltersForm.form.fill(search).data.map { case (k, v) => (k, Seq(v)) }
    }

    def getZeroSoftwareResults(): WSResponse = get("/zero-software-results")

    def postZeroSoftwareResults(): WSResponse = post("/zero-software-results")(Map.empty)

    def healthcheck(): WSResponse =
      wsClient
        .url(s"$baseUrl/ping/ping")
        .get()
        .futureValue

    private def get(uri: String): WSResponse = {
      buildClient(uri)
        .withHttpHeaders(HeaderNames.COOKIE -> bakeSessionCookie())
        .get()
        .futureValue
    }

    def post(uri: String)(body: Map[String, Seq[String]]): WSResponse =
      buildClient(uri)
        .withHttpHeaders("Csrf-Token" -> "nocheck", HeaderNames.COOKIE -> bakeSessionCookie())
        .post(body)
        .futureValue

    private def buildClient(path: String) = {
      wsClient
        .url(s"$baseUrl$baseURI$path")
        .withFollowRedirects(false)
    }
  }
}
