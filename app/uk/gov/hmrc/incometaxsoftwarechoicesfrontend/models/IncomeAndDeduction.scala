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

package uk.gov.hmrc.incometaxsoftwarechoicesfrontend.models

import play.api.libs.json.{Reads, __}

sealed trait IncomeAndDeduction {
  val key: String
}

object IncomeAndDeduction {

  case object BlindPersonsAllowance extends IncomeAndDeduction {
    override val key: String = "blind-persons-allowance"
  }

  case object CapitalGainsTax extends IncomeAndDeduction {
    override val key: String = "capital-gains-tax"
  }

  case object ComplexPartnerships extends IncomeAndDeduction {
    override val key: String = "complex-partnerships"
  }

  case object ConstructionIndustryScheme extends IncomeAndDeduction {
    override val key: String = "construction-industry-scheme"
  }

  case object Employment extends IncomeAndDeduction {
    override val key: String = "employment"
  }

  case object ForeignIncome extends IncomeAndDeduction {
    override val key: String = "foreign-income"
  }

  case object GiftAid extends IncomeAndDeduction {
    override val key: String = "gift-aid"
  }

  case object CharitableGiving extends IncomeAndDeduction {
    override val key: String = "charitable-giving"
  }

  case object Class2NationalInsurance extends IncomeAndDeduction {
    override val key: String = "class-2-national-insurance"
  }

  case object HighIncomeChildBenefit extends IncomeAndDeduction {
    override val key: String = "high-income-child-benefit"
  }

  case object Investments extends IncomeAndDeduction {
    override val key: String = "investments"
  }

  case object LloydsUnderwriters extends IncomeAndDeduction {
    override val key: String = "lloyds-underwriters"
  }

  case object MarriageAllowance extends IncomeAndDeduction {
    override val key: String = "marriage-allowance"
  }

  case object MarriedAllowance extends IncomeAndDeduction {
    override val key: String = "married-allowance"
  }

  case object MemberOfParliament extends IncomeAndDeduction {
    override val key: String = "member-of-parliament"
  }

  case object MinisterOfReligion extends IncomeAndDeduction {
    override val key: String = "minister-of-religion"
  }

  case object PartnerIncome extends IncomeAndDeduction {
    override val key: String = "partner-income"
  }

  case object PAYE extends IncomeAndDeduction {
    override val key: String = "paye"
  }

  case object PensionContributions extends IncomeAndDeduction {
    override val key: String = "pension-contributions"
  }

  case object Pensions extends IncomeAndDeduction {
    override val key: String = "pensions"
  }

  case object PropertyBusiness extends IncomeAndDeduction {
    override val key: String = "property-business"
  }

  case object ResidenceAndRemittance extends IncomeAndDeduction {
    override val key: String = "residence-and-remittance"
  }

  case object SAAdditionalIncome extends IncomeAndDeduction {
    override val key: String = "sa-additional-income"
  }

  case object SelfEmployment extends IncomeAndDeduction {
    override val key: String = "self-employment"
  }

  case object SimplePartnerships extends IncomeAndDeduction {
    override val key: String = "simple-partnerships"
  }

  case object StatePension extends IncomeAndDeduction {
    override val key: String = "state-pension"
  }

  case object StudentLoans extends IncomeAndDeduction {
    override val key: String = "student-loans"
  }

  case object UKDividends extends IncomeAndDeduction {
    override val key: String = "uk-dividends"
  }

  case object UKInterest extends IncomeAndDeduction {
    override val key: String = "uk-interest"
  }

  val incomeAndDeductionKeyToIncomeAndDeduction: Map[String, IncomeAndDeduction] = Seq(
    BlindPersonsAllowance,
    CapitalGainsTax,
    ComplexPartnerships,
    ConstructionIndustryScheme,
    Employment,
    ForeignIncome,
    GiftAid,
    CharitableGiving,
    Class2NationalInsurance,
    HighIncomeChildBenefit,
    Investments,
    LloydsUnderwriters,
    MarriageAllowance,
    MarriedAllowance,
    MemberOfParliament,
    MinisterOfReligion,
    PartnerIncome,
    PAYE,
    PensionContributions,
    Pensions,
    PropertyBusiness,
    ResidenceAndRemittance,
    SAAdditionalIncome,
    SelfEmployment,
    SimplePartnerships,
    StatePension,
    StudentLoans,
    UKDividends,
    UKInterest
  ).map(value => value.key -> value).toMap

  implicit val reads: Reads[IncomeAndDeduction] = __.read[String] map incomeAndDeductionKeyToIncomeAndDeduction

  // product details page groups //
  val personalIncomeSourcesGroup: List[IncomeAndDeduction] = List(ConstructionIndustryScheme, CapitalGainsTax, PAYE, ForeignIncome, UKDividends, UKInterest)
  val deductionsGroup: List[IncomeAndDeduction] = List(CharitableGiving, StudentLoans, Class2NationalInsurance, HighIncomeChildBenefit)
  val pensionsGroup: List[IncomeAndDeduction] = List(StatePension, Pensions, PensionContributions)
  val allowancesGroup: List[IncomeAndDeduction] = List(MarriageAllowance)
}


