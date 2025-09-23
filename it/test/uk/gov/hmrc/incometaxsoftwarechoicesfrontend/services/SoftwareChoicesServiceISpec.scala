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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.services

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.CurrentFeature

import scala.util.Try

class SoftwareChoicesServiceISpec extends PlaySpec with GuiceOneServerPerSuite {

  def app(file: String): Application = new GuiceApplicationBuilder()
    .configure("vendor-list.file-name" -> file)
    .build()

  Seq("software-vendors.json", "software-vendors-local.json") foreach { file =>
    file must {
      "be successfully parsed and translated by the software choices service" in {
        Try {
          app(file).injector.instanceOf[SoftwareChoicesService].softwareVendors
          succeed
        }.getOrElse(fail(s"[SoftwareChoicesISpec] - could not successfully load using file: $file"))
      }
    }
  }

  "software-vendors.json must have all websites starting with https://" in {
    val test = app("software-vendors.json").injector.instanceOf[SoftwareChoicesService].softwareVendors.vendors
    test.foreach(element => element.website should startWith("https://"))
  }

  "software-vendors.json must have unique names" in {
    val test = app("software-vendors.json").injector.instanceOf[SoftwareChoicesService].softwareVendors.vendors.map(_.name)
    test.size shouldBe test.distinct.size
  }

  "software-vendors.json must only include current features" in {
    val test = app("software-vendors.json").injector.instanceOf[SoftwareChoicesService].softwareVendors.vendors
    test.foreach { vendor =>
      vendor.filters.foreach(filter => filter._2 shouldBe CurrentFeature)
      }
    }

  "a file which does not exist" must {
    "throw an exception from the service" in {
      intercept[Exception](app("non-existent.json").injector.instanceOf[SoftwareChoicesService].softwareVendors)
    }
  }

}
