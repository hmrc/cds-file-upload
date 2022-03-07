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

package controllers.declarations

import base.ControllerUnitSpec
import connectors.CustomsDeclarationsInformationConnector
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.Status.{NOT_FOUND, OK}
import testdata.TestData._
import testdata.declarationinformation.DeclarationStatusTestData.DeclarationStatusWithAllData
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationInformationControllerSpec extends ControllerUnitSpec {

  private val cdiConnector = mock[CustomsDeclarationsInformationConnector]

  private val controller = new DeclarationInformationController(authAction, cdiConnector, stubControllerComponents())

  implicit val hc = HeaderCarrier(authorization = Some(Authorization("Bearer qwertyuiop")))

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(cdiConnector)
    authorisedUser()
  }

  override protected def afterEach(): Unit = {
    reset(cdiConnector)
    super.afterEach()
  }

  "DeclarationInformationController on getDeclarationInformation" should {

    "call CustomsDeclarationsInformationConnector passing MRN provided" in {
      when(cdiConnector.getDeclarationStatus(any())(any()))
        .thenReturn(Future.successful(Some(DeclarationStatusWithAllData(mrn).model)))

      controller.getDeclarationInformation(mrn)(getRequest()).futureValue

      verify(cdiConnector).getDeclarationStatus(meq(mrn))(any())
    }

    "return Ok with DeclarationStatus received from CustomsDeclarationsInformationConnector" when {
      "CustomsDeclarationsInformationConnector responds with non-empty Option" in {
        when(cdiConnector.getDeclarationStatus(any())(any()))
          .thenReturn(Future.successful(Some(DeclarationStatusWithAllData(mrn).model)))

        val result = controller.getDeclarationInformation(mrn)(getRequest())

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(DeclarationStatusWithAllData(mrn).model)
      }
    }

    "return NotFound" when {
      "CustomsDeclarationsInformationConnector responds with empty Option" in {
        when(cdiConnector.getDeclarationStatus(any())(any())).thenReturn(Future.successful(None))

        val result = controller.getDeclarationInformation(mrn)(getRequest())

        status(result) mustBe NOT_FOUND
      }
    }
  }
}
