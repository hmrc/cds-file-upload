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

package controllers

import base.ControllerUnitSpec
import connectors.CustomsDataStoreConnector
import models.email.Email
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import play.api.libs.json.Json
import play.api.test.Helpers._
import testdata.TestData
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmailByEoriControllerUnitSpec extends ControllerUnitSpec {

  private val connector = mock[CustomsDataStoreConnector]
  private val controller = new EmailByEoriController(authActionWithEori, connector, stubControllerComponents())

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(connector)
    authorisedUserWithEori()
  }

  override protected def afterEach(): Unit = {
    reset(connector)
    super.afterEach()
  }

  "GET EmailIfVerified endpoint" should {

    "return 200(OK) status and deliverable = true if the email address for the given EORI is verified" in {
      val expectedEmailAddress = Email("some@email.com", deliverable = true)

      when(connector.getEmailAddress(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(expectedEmailAddress)))

      val response = controller.getEmailIfVerified(TestData.eori)(getRequest())
      status(response) mustBe OK
      contentAsJson(response) mustBe Json.toJson(expectedEmailAddress)

      verify(connector).getEmailAddress(meq(TestData.eori))(any[HeaderCarrier])
    }

    "return 200(OK) status and deliverable = false if the email address for the given EORI is not deliverable" in {
      val expectedEmailAddress = Email("some@email.com", deliverable = false)

      when(connector.getEmailAddress(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(expectedEmailAddress)))

      val response = controller.getEmailIfVerified(TestData.eori)(getRequest())
      status(response) mustBe OK
      contentAsJson(response) mustBe Json.toJson(expectedEmailAddress)

      verify(connector).getEmailAddress(meq(TestData.eori))(any[HeaderCarrier])
    }

    "return 404(NOT_FOUND) status if the email address for the given EORI was not provided or was not verified yet" in {
      when(connector.getEmailAddress(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(None))

      val response = controller.getEmailIfVerified(TestData.eori)(getRequest())
      status(response) mustBe NOT_FOUND
    }
  }

  "GET Email endpoint" should {

    "return 200(OK) status and deliverable = true if the email address for the given EORI is verified" in {
      val expectedEmailAddress = Email("some@email.com", deliverable = true)

      when(connector.getEmailAddress(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(expectedEmailAddress)))

      val response = controller.getEmail(getRequest())
      status(response) mustBe OK
      contentAsJson(response) mustBe Json.toJson(expectedEmailAddress)

      verify(connector).getEmailAddress(meq(TestData.eori))(any[HeaderCarrier])
    }

    "return 200(OK) status and deliverable = false if the email address for the given EORI is not deliverable" in {
      val expectedEmailAddress = Email("some@email.com", deliverable = false)

      when(connector.getEmailAddress(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(expectedEmailAddress)))

      val response = controller.getEmail(getRequest())
      status(response) mustBe OK
      contentAsJson(response) mustBe Json.toJson(expectedEmailAddress)

      verify(connector).getEmailAddress(meq(TestData.eori))(any[HeaderCarrier])
    }

    "return 404(NOT_FOUND) status if the email address for the given EORI was not provided or was not verified yet" in {
      when(connector.getEmailAddress(any[String])(any[HeaderCarrier])).thenReturn(Future.successful(None))

      val response = controller.getEmail(getRequest())
      status(response) mustBe NOT_FOUND
    }
  }
}
