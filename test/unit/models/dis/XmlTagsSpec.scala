/*
 * Copyright 2022 HM Revenue & Customs
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

package models.dis

import base.UnitSpec

// Unit test for incrementing the code coverage percentage ;0)
class XmlTagsSpec extends UnitSpec {

  "XmlTags values" should {
    "be" in {
      XmlTags.declarationStatusDetails mustBe "DeclarationStatusDetails"
      XmlTags.declaration mustBe "Declaration"
      XmlTags.goodsShipment mustBe "GoodsShipment"
      XmlTags.versionId mustBe "VersionID"
      XmlTags.submitter mustBe "Submitter"
      XmlTags.roe mustBe "ROE"
      XmlTags.ics mustBe "ICS"
      XmlTags.irc mustBe "IRC"
      XmlTags.totalPackageQuantity mustBe "TotalPackageQuantity"
      XmlTags.goodsItemQuantity mustBe "GoodsItemQuantity"
      XmlTags.ucr mustBe "UCR"
      XmlTags.receivedDateTime mustBe "ReceivedDateTime"
      XmlTags.goodsReleasedDateTime mustBe "GoodsReleasedDateTime"
      XmlTags.acceptanceDateTime mustBe "AcceptanceDateTime"
      XmlTags.dateTimeString mustBe "DateTimeString"
      XmlTags.traderAssignedReferenceID mustBe "TraderAssignedReferenceID"
      XmlTags.id mustBe "ID"
      XmlTags.typeCode mustBe "TypeCode"
      XmlTags.previousDocument mustBe "PreviousDocument"

      XmlTags.errorResponse mustBe "errorResponse"
      XmlTags.code mustBe "code"
      XmlTags.message mustBe "message"
    }
  }
}
