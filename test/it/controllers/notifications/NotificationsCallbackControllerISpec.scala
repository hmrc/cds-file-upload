/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.notifications

import base.UnitSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.WSClient
import play.api.test.Helpers._

class NotificationsCallbackControllerISpec extends UnitSpec with GuiceOneServerPerSuite {

  private val wsClient: WSClient = app.injector.instanceOf[WSClient]

  private val notificationUrl = s"http://localhost:$port/internal/notification"

  private val validNotification =
    <Root>
      <FileReference>some-file-ref-123</FileReference>
      <BatchId>5e634e09-77f6-4ff1-b92a-8a9676c715c4</BatchId>
      <FileName>sample.pdf</FileName>
      <Outcome>SUCCESS</Outcome>
      <Details>[detail block]</Details>
    </Root>

  private val notificationWithEmptyFileReference =
    <Root>
      <FileReference></FileReference>
      <BatchId>5e634e09-77f6-4ff1-b92a-8a9676c715c4</BatchId>
      <FileName>sample.pdf</FileName>
      <Outcome>SUCCESS</Outcome>
      <Details>[detail block]</Details>
    </Root>

  private val notificationWithEmptyOutcome =
    <Root>
      <FileReference>some-file-ref-123</FileReference>
      <BatchId>5e634e09-77f6-4ff1-b92a-8a9676c715c4</BatchId>
      <FileName>sample.pdf</FileName>
      <Outcome></Outcome>
      <Details>[detail block]</Details>
    </Root>

  private val invalidNotification = <Invalid/>

  "Notification endpoint" should {

    "return 202 (ACCEPTED) status on POST request for invalid xml" in {
      val response = await(
        wsClient
          .url(notificationUrl)
          .withHttpHeaders("Content-Type" -> "application/xml")
          .post(invalidNotification)
      )

      response.status mustBe ACCEPTED
    }

    "return 202 (ACCEPTED) status on POST request for xml with empty File Reference" in {
      val response = await(
        wsClient
          .url(notificationUrl)
          .withHttpHeaders("Content-Type" -> "application/xml")
          .post(notificationWithEmptyFileReference)
      )

      response.status mustBe ACCEPTED
    }

    "return 202 (ACCEPTED) status on POST request for xml with empty Outcome" in {
      val response = await(
        wsClient
          .url(notificationUrl)
          .withHttpHeaders("Content-Type" -> "application/xml")
          .post(notificationWithEmptyOutcome)
      )

      response.status mustBe ACCEPTED
    }

    "return 202 Accepted on POST request for valid xml" in {
      val response = await(
        wsClient
          .url(notificationUrl)
          .withHttpHeaders("Content-Type" -> "application/xml")
          .post(validNotification)
      )

      response.status mustBe ACCEPTED
    }
  }
}
