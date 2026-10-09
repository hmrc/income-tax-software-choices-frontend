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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.views.html.AccountingPeriodNotAlignedView

class AccountingPeriodNotAlignedViewSpec extends ViewSpec {

  object AccountingPeriodNotAlignedViewContent {
    val headingIndividual = "You’ll need to adjust your income figures before reporting them to HMRC"
    val headingAgent = "Your client’s income figures will need to be adjusted before they are reported to HMRC"
    val titleIndividual = s"$headingIndividual - ${PageContentBase.title} - GOV.UK"
    val titleAgent = s"$headingAgent - ${PageContentBase.title} - GOV.UK"
    val subHeadingIndividual = "Reporting your income to HMRC with a non-aligned accounting period"
    val subHeadingAgent = "Reporting income to HMRC for a non-aligned accounting period"
    val paraOneIndividual = "Your accounting period does not end on 5 April or 31 March. This means it is not aligned with the tax year, so you have a non-aligned accounting period."
    val paraOneAgent = "Your client’s accounting period does not end on 5 April or 31 March. This means it is not aligned with the tax year, so they have a non-aligned accounting period."
    val paraTwoIndividual = "At the end of the tax year, you’ll need to adjust the income figures shown in your chosen software. This is so they align with the tax year before you report your property or self-employment income to HMRC."
    val paraTwoAgent = "At the end of the tax year, you or your client will need to adjust the income figures shown in the chosen software. This is so they align with the tax year before reporting property or self-employment income to HMRC."
    val paraThree = "HMRC will publish guidance on how to do this in due course."
    val paraFourIndividual = "You can continue to choose your software."
    val paraFourAgent = "You can continue to choose software."
    val continue = "Continue"
  }

  private val view = app.injector.instanceOf[AccountingPeriodNotAlignedView]
  private val SoftwareName = "Bright"

  def page(userType: String): HtmlFormat.Appendable = view(postAction = testCall, backLink = testBackUrl, Some(SoftwareName), userTypeString = userType)
  val individualDocument: Document = Jsoup.parse(page("individual").body)
  val agentDocument: Document = Jsoup.parse(page("agent").body)

  "AccountingPeriodNotAlignedView" must {
    "have a title" in {
      individualDocument.title() shouldBe AccountingPeriodNotAlignedViewContent.titleIndividual
      agentDocument.title() shouldBe AccountingPeriodNotAlignedViewContent.titleAgent
    }
    "have the correct headings" in {
      individualDocument.mainContent.selectHead("h1").text shouldBe AccountingPeriodNotAlignedViewContent.headingIndividual
      agentDocument.mainContent.selectHead("h1").text shouldBe AccountingPeriodNotAlignedViewContent.headingAgent
      individualDocument.mainContent.selectHead("h2").text shouldBe AccountingPeriodNotAlignedViewContent.subHeadingIndividual
      agentDocument.mainContent.selectHead("h2").text shouldBe AccountingPeriodNotAlignedViewContent.subHeadingAgent
    }
    "have a software name caption" in {
      individualDocument.mainContent.selectHead("span.govuk-caption-l").text() shouldBe SoftwareName
      agentDocument.mainContent.selectHead("span.govuk-caption-l").text() shouldBe SoftwareName
    }
    "have the correct paragraphs" in {
      individualDocument.mainContent.selectNth("p", 1).text shouldBe AccountingPeriodNotAlignedViewContent.paraOneIndividual
      agentDocument.mainContent.selectNth("p", 1).text shouldBe AccountingPeriodNotAlignedViewContent.paraOneAgent
      individualDocument.mainContent.selectNth("p", 2).text shouldBe AccountingPeriodNotAlignedViewContent.paraTwoIndividual
      agentDocument.mainContent.selectNth("p", 2).text shouldBe AccountingPeriodNotAlignedViewContent.paraTwoAgent
      individualDocument.mainContent.selectNth("p", 3).text shouldBe AccountingPeriodNotAlignedViewContent.paraThree
      agentDocument.mainContent.selectNth("p", 3).text shouldBe AccountingPeriodNotAlignedViewContent.paraThree
      individualDocument.mainContent.selectNth("p", 4).text shouldBe AccountingPeriodNotAlignedViewContent.paraFourIndividual
      agentDocument.mainContent.selectNth("p", 4).text shouldBe AccountingPeriodNotAlignedViewContent.paraFourAgent
    }
    "have a form" which {
      val individualForm: Element = individualDocument.selectHead("form")
      val agentForm: Element = agentDocument.selectHead("form")

      "has the correct method and action" in {
        individualForm.attr("method") shouldBe testCall.method
        individualForm.attr("action") shouldBe testCall.url
        agentForm.attr("method") shouldBe testCall.method
        agentForm.attr("action") shouldBe testCall.url
      }

      "has a continue button" in {
        individualForm.selectHead(".govuk-button").text() shouldBe AccountingPeriodNotAlignedViewContent.continue
        agentForm.selectHead(".govuk-button").text() shouldBe AccountingPeriodNotAlignedViewContent.continue
      }
    }
  }

}