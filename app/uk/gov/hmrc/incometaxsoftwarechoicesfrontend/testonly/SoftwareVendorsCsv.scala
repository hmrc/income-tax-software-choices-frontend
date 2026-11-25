/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.testonly

import play.api.i18n.Messages
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.VendorFilter.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.FeatureStatus.{Available, Intended, NotApplicable}

object SoftwareVendorsCsv {
  def featureLabel(filter: VendorFilter)(implicit messages: Messages): String = filter match {
    case QuarterlyUpdates | TaxReturn => messages(s"search-software.vendor-detail.submission-type.${filter.key}")
    case FullyReady => messages("search-software.vendor-detail.readiness.fully-ready")
    case VoluntaryClass2NationalInsurance => "Voluntary Class 2 National Insurance"
    case FreeTrial => "Free trial"
    case ForeignIncome => "Foreign income"
    case AveragingAdjustment => "Averaging adjustment"
    case FosterCarer => "Foster carer"
    case TrustIncome => "Trust income"
    case _ =>
      val section = filter match {
        case SoleTrader | UkProperty | OverseasProperty => "business-income"
        case DesktopApplication | WebBrowser => "software-type"
        case MicrosoftWindows | MacOS | Linux => "compatible-with"
        case Android | Apple => "mobile-app"
        case English | Welsh => "language"
        case FreeVersion | Agent | Individual | StandardUpdatePeriods | CalendarUpdatePeriods |
             RecordKeeping | Bridging | Vat | Visual | Hearing | Motor | Cognitive | HMRCAssist => "features-provided"
        case _ => "other-income"
      }
      messages(s"product-details.$section.${filter.key}")
  }

  private def cell(value: String): String = {
    // Quoting alone does not prevent spreadsheet applications from executing formulas.
    val safeValue = if (value.dropWhile(_.isWhitespace).headOption.exists("=+-@".contains(_)) ||
      value.headOption.exists("\t\r\n".contains(_))) s"'$value" else value
    "\"" + safeValue.replace("\"", "\"\"") + "\""
  }

  def render(overview: SoftwareVendorsOverview)(implicit messages: Messages): String = {
    val headers = Seq("Product", "Outstanding commitments", "In development for April 2027") ++
      overview.groups.flatMap { case (group, features) => features.map(feature => s"$group: ${featureLabel(feature)}") }
    val rows = overview.matchingVendors.map { vendor =>
      Seq(vendor.name, overview.outstandingFeatures(vendor).size.toString,
        overview.outstandingFeatures(vendor).map(featureLabel).mkString("; ")) ++
        overview.allFeatures.map(feature => vendor.filters.get(feature) match {
          case Some(Available) => "Available"
          case Some(Intended) => "In development"
          case Some(NotApplicable) => "Not included"
          case None => "Not provided"
        })
    }
    "\uFEFF" + (headers +: rows).map(_.map(cell).mkString(",")).mkString("", "\r\n", "\r\n")
  }
}
