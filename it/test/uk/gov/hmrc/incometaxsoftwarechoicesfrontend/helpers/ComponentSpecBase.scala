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

import org.jsoup.nodes.Element
import org.scalatest.Checkpoints.Checkpoint
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{Assertion, GivenWhenThen, Succeeded}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.crypto.CookieSigner
import play.api.libs.ws.WSBodyWritables.writeableOf_urlEncodedForm
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.forms.*
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.helpers.IntegrationTestConstants.baseURI
import uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models.*

import scala.jdk.CollectionConverters.*

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

    def productDetails(productId: String): WSResponse = get(s"/product-details?productId=$productId")

    def getBusinessIncome: WSResponse = get("/which-income-source-quarterly-updates")

    def postBusinessIncome(pageAnswers: Seq[VendorFilter], editMode: Boolean = false, userTypeString: String = "individual"): WSResponse = post(s"/which-income-source-quarterly-updates?editMode=$editMode")(
      BusinessIncomeForm.form(userTypeString).fill(pageAnswers).data.map { case (k, v) => (k, Seq(v)) }
    )

    def getNeedAdditionalSoftware(editMode: Boolean = false): WSResponse = {
      val url = editMode match {
        case true => "/need-additional-software?editMode=true"
        case false => "/need-additional-software"
      }
      get(url)
    }

    def getSoftwareInDevelopment: WSResponse = get("/software-in-development")

    def getAdditionalIncome: WSResponse = get("/which-other-income-source-tax-return")

    def submitAdditionalIncome(maybeKeys: Option[Seq[String]], editMode: Boolean = false): WSResponse = {
      val body: Map[String, Seq[String]] = maybeKeys match {
        case Some(keys) if keys.nonEmpty => Map("additionalIncome[]" -> keys)
        case Some(_) => Map("additionalIncome[]" -> Seq(AdditionalIncomeForm.noneKey))
        case None => Map.empty
      }
      post(s"/which-other-income-source-tax-return?editMode=$editMode")(body)
    }

    def getOtherItems: WSResponse = get("/which-other-items-tax-return")

    def postOtherItems(maybeKeys: Option[Seq[String]], editMode: Boolean = false): WSResponse = {
      val body: Map[String, Seq[String]] = maybeKeys match {
        case Some(keys) if keys.nonEmpty => Map("otherItems[]" -> keys)
        case Some(_) => Map("otherItems[]" -> Seq(OtherItemsForm.noneKey))
        case None => Map.empty
      }
      post(s"/which-other-items-tax-return?editMode=$editMode")(body)
    }

    def getAccountingPeriodNotAligned: WSResponse = get("/accounting-period-not-aligned")
    def getAccountingPeriodNotAlignedEditMode: WSResponse = get("/accounting-period-not-aligned?editMode=true")

    def postAccountingPeriodNotAligned: WSResponse = post("/accounting-period-not-aligned")(Map.empty)

    def getAccountingPeriod: WSResponse = get("/accounting-period-check")

    def submitAccountingPeriod(request: Option[Seq[String]], editMode: Boolean = false): WSResponse = {
      val body: Map[String, Seq[String]] = request match {
        case Some(keys) if keys.nonEmpty => Map(s"${AccountingPeriodForm.formKey}[]" -> keys)
        case None => Map.empty
      }
      post(s"/accounting-period-check?editMode=$editMode")(body)
    }

    def getUserType(editMode: Boolean = false): WSResponse = {
      editMode match {
        case true => get("/about-you?editMode=true")
        case false => get("/about-you")
      }

    }

    def submitUserType(request: Option[UserType], editMode: Boolean = false): WSResponse = {
      val url = editMode match {
        case true => "/about-you?editMode=true"
        case false => "/about-you"
      }
      post(url)(
        request.fold(Map.empty[String, Seq[String]])(
          model => UserTypeForm.userTypeForm.fill(model).data.map { case (k, v) => (k, Seq(v)) }
        )
      )
    }

    def getChoosingSoftware: WSResponse = get("/choosing-software-from-your-results")
    
    def postChoosingSoftware: WSResponse = post("/choosing-software-from-your-results")(Map.empty)
    
    def getCheckYourAnswers: WSResponse = get("/check-answers")

    def postCheckYourAnswers(): WSResponse = post("/check-answers")(Map.empty)

    def getSoftwareResults: WSResponse = get("/software-results")

    def submitSoftwareSearch(search: FiltersFormModel): WSResponse = post("/software-results") {
      FiltersForm.form.fill(search).data.map { case (k, v) => (k, Seq(v)) }
    }

    def getZeroSoftwareResults(): WSResponse = get("/no-software-matches")

    def postZeroSoftwareResults(): WSResponse = post("/no-software-matches")(Map.empty)

    def getNoListedSoftware(editMode: Boolean = false): WSResponse =  {
      val url = editMode match {
        case true => "/software-not-recognised?editMode=true"
        case false => "/software-not-recognised"
      }
      get(url)
    }
    
    def getHowYouFindSoftware(): WSResponse = get("/how-to-find-software")
    
    def postHowYouFindSoftware(pageAnswer: Option[JourneyType]): WSResponse = {
      post("/how-to-find-software")(
        pageAnswer.fold(Map.empty[String, Seq[String]])(
          journeyType=> HowYouFindSoftwareForm.form.fill(journeyType).data.map { case (k, v) => (k, Seq(v)) }
        )
      )
    }

    def getEnterSoftwareName(editMode: Boolean = false): WSResponse = {
      val url = editMode match {
        case true => "/check-software?editMode=true"
        case false => "/check-software"
      }
      get(url)
    }

    def postEnterSoftwareName(pageAnswer: Option[Int], editMode: Boolean = false): WSResponse = {
      val url = editMode match {
        case true => "/check-software?editMode=true"
        case false => "/check-software"
      }
      post(url)(
        pageAnswer.fold(Map.empty[String, Seq[String]])(
        productId => EnterSoftwareNameForm.form.fill(productId).data.map { case (k, v) => (k, Seq(v)) }
        )
      )
    }

    def clearEnterSoftwareName(editMode: Boolean = false): WSResponse = {
      val url = editMode match {
        case true => "/check-software/clear?editMode=true"
        case false => "/check-software/clear"
      }
      get(url)
    }

    def getFullyCompatible(): WSResponse = get("/fully-compatible")
    
    def getPartiallyCompatible(): WSResponse = get("/partly-compatible")

    def getQuarterlyOnly(): WSResponse = get("/quarterly-updates-only")
    
    def getNotCompatible(): WSResponse = get("/not-compatible")

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

  implicit class CustomSelectors(element: Element) {

    lazy val getServiceLink: String = element.selectHead(".govuk-service-navigation__link").attr("href")

    lazy val getServiceName: String = element.selectHead(".govuk-service-navigation__link").text

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

    def getForm: Element = element.selectHead("form")

    def getTable(index: Int): Element = selectSeq(".govuk-table").lift(index - 1)
      .getOrElse(fail(s"No table found at index: $index"))

    def getSummaryList: Element = element.selectHead("dl.govuk-summary-list")

    def getSummaryListRow(nth: Int): Element = element.selectHead(s"div.govuk-summary-list__row:nth-of-type($nth)")

    def getSummaryListKey: Element = element.selectHead("dt.govuk-summary-list__key")

    def getSummaryListValue: Element = element.selectHead("dd.govuk-summary-list__value")

    def getSummaryListActions: Element = element.selectHead("dd.govuk-summary-list__actions")
  }

  implicit class ComponentTests(element: Element) {

    def mustHaveCheckbox(selector: String)(groupNth: Option[Int] = None,
                                           checkbox: Int,
                                           legend: String,
                                           isHeading: Boolean,
                                           isLegendHidden: Boolean,
                                           name: String,
                                           label: String,
                                           value: String,
                                           checked: Boolean = false,
                                           isExclusive: Boolean = false): Assertion = {

      val checkpoint: Checkpoint = new Checkpoint()
      val fieldSet: Element = element.selectHead(selector)

      val checkboxGroup = groupNth match {
        case Some(n) if n == 0 => fail(s"Invalid nth selector of $n, must be >= 1")
        case Some(n) => element.selectNth(".govuk-checkboxes", n)
        case _ => element.selectHead(".govuk-checkboxes")
      }

      validateFieldSetLegend(fieldSet, legend, isHeading, isLegendHidden, checkpoint)

      val item: Element = checkboxGroup.selectNth(".govuk-checkboxes__item", checkbox)

      checkpoint {
        item.selectHead("input").attr("name") shouldBe name
      }
      checkpoint {
        item.selectHead(".govuk-checkboxes__label").text shouldBe label
      }
      checkpoint {
        item.selectHead("input").attr("value") shouldBe value
      }
      checkpoint {
        item.selectHead("input").hasAttr("checked") shouldBe checked
      }
      checkpoint {
        if (isExclusive) {
          item.selectHead("input").attr("data-behaviour") shouldBe "exclusive"
        } else {
          item.selectHead("input").hasAttr("data-behaviour") shouldBe false
        }
      }

      checkpoint.reportAll()
      Succeeded

    }

    def mustHaveRadioInput(selector: String)(name: String,
                                             legend: String,
                                             isHeading: Boolean,
                                             isLegendHidden: Boolean,
                                             hint: Option[String],
                                             errorMessage: Option[String],
                                             radioContents: Seq[RadioItem],
                                             isInline: Boolean = false): Assertion = {

      val checkpoint: Checkpoint = new Checkpoint()
      val radioFieldSet: Element = element.selectHead(selector)

      validateFieldSetLegend(radioFieldSet, legend, isHeading, isLegendHidden, checkpoint)

      hint.foreach { hint =>
        val radioFieldSetHint: Element = radioFieldSet.selectHead(".govuk-hint")
        checkpoint {
          radioFieldSet.attr("aria-describedby") must include(radioFieldSetHint.attr("id"))
        }
        checkpoint {
          radioFieldSetHint.text mustBe hint
        }
      }

      errorMessage.foreach { errorMessage =>
        val radioFieldSetError: Element = radioFieldSet.selectHead(".govuk-error-message")
        checkpoint {
          radioFieldSet.attr("aria-describedby") must include(radioFieldSetError.attr("id"))
        }
        checkpoint {
          radioFieldSetError.text must include(errorMessage)
        }
      }

      val radioField: Element = if (isInline) element.selectHead(".govuk-radios--inline") else element.selectHead(".govuk-radios")

      radioContents.zipWithIndex foreach { case (radioContent, index) =>
        if (radioContent.divider.isDefined) {
          validateRadioDivider(radioField, radioContent, index, checkpoint)
        } else {
          validateRadioItem(radioField, name, radioContent, index, checkpoint)
        }
      }
      checkpoint.reportAll()
      Succeeded
    }

    private def validateRadioItem(radioField: Element, name: String, radioItem: RadioItem, index: Int, checkpoint: Checkpoint): Unit = {
      val radioItemElement: Element = radioField.child(index)
      val radioInput: Element = radioItemElement.selectHead("input")
      val radioLabel: Element = radioItemElement.selectHead("label")
      val radioInputId: String = if (index == 0) name else s"$name-${index + 1}"

      checkpoint {
        radioItemElement.className() mustBe "govuk-radios__item"
      }
      checkpoint {
        radioInput.attr("id") mustBe radioInputId
      }
      checkpoint {
        radioInput.attr("name") mustBe name
      }
      checkpoint {
        radioInput.attr("type") mustBe "radio"
      }
      checkpoint {
        radioInput.attr("value") mustBe radioItem.value.getOrElse("")
      }
      checkpoint {
        radioLabel.attr("for") mustBe radioInput.attr("id")
      }
      checkpoint {
        Text(radioLabel.text) mustBe radioItem.content
      }
      radioItem.hint.foreach { hint =>
        checkpoint {
          Text(radioItemElement.selectHead(".govuk-radios__hint").text) mustBe hint.content
        }
      }
    }

    private def validateRadioDivider(radioField: Element, radioDivider: RadioItem, index: Int, checkpoint: Checkpoint): Unit = {
      val dividerElement: Element = radioField.child(index)
      checkpoint {
        dividerElement.className() mustBe "govuk-radios__divider"
      }
      checkpoint {
        dividerElement.text() mustBe radioDivider.divider.get
      }
    }

    private def validateFieldSetLegend(fieldSet: Element,
                                       legend: String,
                                       isHeading: Boolean,
                                       isLegendHidden: Boolean,
                                       checkpoint: Checkpoint): Unit = {
      val fieldSetLegend: Element = fieldSet.selectHead("legend")

      if (isHeading) {
        checkpoint {
          fieldSetLegend.selectHead("h1").text shouldBe legend
        }
      } else {
        checkpoint {
          fieldSetLegend.text shouldBe legend
        }
      }
      if (isLegendHidden) {
        checkpoint {
          fieldSetLegend.attr("class") should include("govuk-visually-hidden govuk-!-display-block")
        }
      } else {
        checkpoint {
          fieldSetLegend.attr("class") shouldNot include("govuk-visually-hidden govuk-!-display-block")
        }
      }
    }

  }


}
