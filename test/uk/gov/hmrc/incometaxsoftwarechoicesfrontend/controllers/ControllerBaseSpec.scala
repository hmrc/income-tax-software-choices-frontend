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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.controllers

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, stubMessagesControllerComponents}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig

import scala.concurrent.ExecutionContext

trait ControllerBaseSpec extends AnyWordSpecLike with Matchers with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val mcc: MessagesControllerComponents = stubMessagesControllerComponents()

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  implicit class FakeRequestUtil(fakeRequest: FakeRequest[_]) {
    def post[T](form: Form[T], data: T): FakeRequest[AnyContentAsFormUrlEncoded] =
      fakeRequest.post(form.fill(data))

    def postInvalid[T, I](form: Form[T], data: I): FakeRequest[AnyContentAsFormUrlEncoded] =
      fakeRequest.withFormUrlEncodedBody(form.mapping.key -> data.toString)

    def post[T](form: Form[T]): FakeRequest[AnyContentAsFormUrlEncoded] =
      fakeRequest.withFormUrlEncodedBody(form.data.toSeq: _*)
        .withHeaders("Content-Type" -> "application/x-www-form-urlencoded")
        .withMethod(POST)
  }

}
