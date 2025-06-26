/*
 * Copyright 2018 HM Revenue & Customs
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

import org.mockito.Mockito.when
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsPath
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter._
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.{UserAnswers, UserFilters}
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.pages.QuestionPage
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}

class UserFiltersRepositoryISpec
  extends AnyWordSpecLike
    with Matchers
    with DefaultPlayMongoRepositorySupport[UserFilters]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  private case object DummyPage extends QuestionPage[String] {
    override def toString: String = "dummy"

    override def path: JsPath = JsPath \ toString
  }

  private val dummyUserAnswers: UserAnswers = UserAnswers().set(DummyPage, "Test").get

  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1L

  val repository: UserFiltersRepository = new UserFiltersRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )(scala.concurrent.ExecutionContext.Implicits.global)

  override def beforeEach(): Unit = {
    repository.collection.drop().toFuture().futureValue
    repository.ensureIndexes().futureValue
  }

  val testSessionIdOne: String = "testSessionIdOne"
  val testSessionIdTwo: String = "testSessionIdTwo"
  val emptyUserFilters: UserFilters = UserFilters(testSessionIdOne, None, Seq.empty)
  val oneUserFilters: UserFilters = UserFilters(testSessionIdTwo, Some(dummyUserAnswers), Seq(Hearing))


  "get" should {
    "return None if their is no data relating to the id" when {
      "a document with the reference and dataId is found" in {
        repository.get(testSessionIdOne).futureValue shouldBe None
      }
    }
    "return the data relating to the id" when {
      "a document with the reference and dataId is found" in {
        repository.set(oneUserFilters).futureValue
        val result = repository.get(testSessionIdTwo).futureValue
        result.get.id shouldBe testSessionIdTwo
        result.get.finalFilters shouldBe Seq(Hearing)
        result.get.answers shouldBe Some(dummyUserAnswers)
      }
    }
  }

  "set" should {
    "update a record if it already exists" in {
      repository.set(emptyUserFilters).futureValue
      repository.set(emptyUserFilters.copy(answers = Some(dummyUserAnswers), finalFilters = Seq(Motor)))

      val result = repository.get(testSessionIdOne).futureValue.get
      result.finalFilters shouldBe Seq(Motor)
      result.answers shouldBe Some(dummyUserAnswers)
    }
  }

  "keeepAlive" should {
    "update the lastUpdated time" in {
      repository.set(emptyUserFilters).futureValue
      val startTime = Instant.now
      Thread.sleep(10)
      repository.keepAlive(testSessionIdOne).futureValue shouldBe true
      Thread.sleep(5)
      repository.get(testSessionIdOne).futureValue.get.lastUpdated.isAfter(startTime)
    }
  }

}
