/*
 * Copyright 2021 HM Revenue & Customs
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

package connectors

import scala.concurrent.ExecutionContext.Implicits.global

import base.IntegrationSpec
import config.AppConfig
import models.email.Email
import play.api.test.Helpers._
import testdata.TestData
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

class CustomsDataStoreConnectorISpec extends IntegrationSpec {

  implicit val appConfig: AppConfig = inject[AppConfig]

  val connector = new CustomsDataStoreConnector(inject[HttpClient])(appConfig, global)

  "CustomsDataStoreConnector.getEmailAddress" when {

    "email service responds with OK (200) and email is deliverable" should {
      "return a valid Email instance" in {
        val testVerifiedEmailJson = """{"address":"some@email.com","timestamp": "2020-03-20T01:02:03Z"}"""
        val expectedVerifiedEmailAddress = Email("some@email.com", deliverable = true)
        val path = s"/customs-data-store/eori/${TestData.eori}/verified-email"
        getFromDownstreamService(path, OK, Some(testVerifiedEmailJson))

        val response = connector.getEmailAddress(TestData.eori)(HeaderCarrier()).futureValue

        response mustBe Some(expectedVerifiedEmailAddress)
        verifyGetFromDownStreamService(path)
      }
    }

    "email service responds with OK (200) and email is undeliverable" should {
      "return a valid Email instance" in {
        val testUndeliverableEmailJson =
          """{
             |  "address": "some@email.com",
             |  "timestamp": "2020-03-20T01:02:03Z",
             |  "undeliverable": {
             |    "subject": "subject-example",
             |    "eventId": "example-id",
             |    "groupId": "example-group-id",
             |    "timestamp": "2021-05-14T10:59:45.811+01:00",
             |    "event": {
             |      "id": "example-id",
             |      "event": "someEvent",
             |      "emailAddress": "some@email.com",
             |      "detected": "2021-05-14T10:59:45.811+01:00",
             |      "code": 12,
             |      "reason": "Inbox full",
             |      "enrolment": "HMRC-CUS-ORG~EORINumber~testEori"
             |    }
             |  }
             |}""".stripMargin

        val expectedUndeliverableEmailAddress = Email("some@email.com", deliverable = false)
        val path = s"/customs-data-store/eori/${TestData.eori}/verified-email"
        getFromDownstreamService(path, OK, Some(testUndeliverableEmailJson))

        val response = connector.getEmailAddress(TestData.eori)(HeaderCarrier()).futureValue

        response mustBe Some(expectedUndeliverableEmailAddress)
        verifyGetFromDownStreamService(path)
      }
    }

    "email service responds with NOT_FOUND (404)" should {
      "return empty Option" in {
        val path = s"/customs-data-store/eori/${TestData.eori}/verified-email"
        getFromDownstreamService(path, NOT_FOUND, None)

        val response = connector.getEmailAddress(TestData.eori)(HeaderCarrier()).futureValue

        response mustBe None
        verifyGetFromDownStreamService(path)
      }
    }

    "email service responds with any other error code" should {
      "return failed Future" in {
        val path = s"/customs-data-store/eori/${TestData.eori}/verified-email"
        val errorMsg = "Upstream service test error"
        getFromDownstreamService(path, BAD_GATEWAY, Some(errorMsg))

        val response = connector.getEmailAddress(TestData.eori)(HeaderCarrier()).failed.futureValue

        response.getMessage must include(errorMsg)
        verifyGetFromDownStreamService(path)
      }
    }
  }

  "CustomsDataStoreConnector.verifiedEmailPath" should {
    "return correct path" in {
      val expectedPath = s"/customs-data-store/eori/${TestData.eori}/verified-email"

      val actualPath = CustomsDataStoreConnector.verifiedEmailPath(TestData.eori)

      actualPath mustBe expectedPath
    }
  }
}
