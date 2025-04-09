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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.repositories

import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import org.mongodb.scala.SingleObservableFuture
import play.api.libs.json.Format
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.UserFilters
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.play.http.logging.Mdc

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserFiltersRepository @Inject()(
                                   mongoComponent: MongoComponent,
                                   appConfig: AppConfig,
                                   clock: Clock
                                 )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[UserFilters](
    collectionName = "user-filters",
    mongoComponent = mongoComponent,
    domainFormat   = UserFilters.format,
    indexes        = Seq(
      IndexModel(
        Indexes.ascending("lastUpdated"),
        IndexOptions()
          .name("lastUpdatedIdx")
          .expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
      )
    )
  ) {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private def byId(id: String): Bson = Filters.equal("_id", id)

  def keepAlive(id: String): Future[Boolean] = Mdc.preservingMdc {
    collection
      .updateOne(
        filter = byId(id),
        update = Updates.set("lastUpdated", Instant.now(clock)),
      )
      .toFuture()
      .map(_ => true)
  }

  def get(id: String): Future[Option[UserFilters]] = Mdc.preservingMdc {
    keepAlive(id).flatMap {
      _ =>
        collection
          .find(byId(id))
          .headOption()
    }
  }

  def set(filters: UserFilters): Future[Boolean] = Mdc.preservingMdc {

    val updatedFilters = filters copy (lastUpdated = Instant.now(clock))

    collection
      .replaceOne(
        filter      = byId(updatedFilters.id),
        replacement = updatedFilters,
        options     = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => true)
  }
}
