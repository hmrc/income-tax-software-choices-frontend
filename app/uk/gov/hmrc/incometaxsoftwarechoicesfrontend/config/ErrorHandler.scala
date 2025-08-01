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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config

import play.api.Logging
import play.api.i18n.MessagesApi
import play.api.mvc.Results.NotFound
import play.api.mvc.{RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.templates.ErrorTemplate
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject()(val messagesApi: MessagesApi,
                             errorTemplate: ErrorTemplate)
                            (implicit val ec: ExecutionContext) extends FrontendErrorHandler with Logging {

  def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: RequestHeader): Future[Html] = {
    Future.successful(errorTemplate(pageTitle, heading, message))
  }

  override def resolveError(rh: RequestHeader, ex: Throwable): Future[Result] = {
    ex match {
      case _: NotFoundException =>
        notFoundTemplate(rh) map { html =>
          NotFound(html)
        }
      case _ =>
        logger.error(s"[ErrorHandler][resolveError] Internal Server Error, (${rh.method})(${rh.uri})", ex)
        super.resolveError(rh, ex)
    }
  }
}