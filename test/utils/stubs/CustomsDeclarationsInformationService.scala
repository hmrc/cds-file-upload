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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.ContentTypes
import play.api.mvc.Codec
import play.api.test.Helpers._
import stubs.CustomsDeclarationsInformationService._
import testdata.declarationinformation.DeclarationStatusTestData._

import scala.xml.Elem

trait CustomsDeclarationsInformationService extends MockGenericDownstreamService {

  def getFromCDIService(status: Int, mrn: String, delay: Int = 0): StubMapping = {
    val url = fetchMrnStatusUrl.replace(id, mrn)
    val body = buildResponseBody(status, mrn).toString()
    getFromDownstreamService(url, status, Some(body), delay)
  }

  private def buildResponseBody(status: Int, mrn: String): Elem = status match {
    case OK        => DeclarationStatusWithAllData(mrn).xml
    case NOT_FOUND => declarationStatusNotFoundResponse
    case _         => declarationStatusInternalServerErrorResponse
  }

  def verifyDecServiceWasCalledCorrectly(mrn: String, expectedApiVersion: String): Unit =
    verify(
      getRequestedFor(urlMatching(fetchMrnStatusUrl.replace(id, mrn)))
        .withHeader(CONTENT_TYPE, equalTo(ContentTypes.XML(Codec.utf_8)))
        .withHeader(ACCEPT, equalTo(s"application/vnd.hmrc.$expectedApiVersion+xml"))
    )
}

object CustomsDeclarationsInformationService {
  val apiVersion: String = "1.0"
  val bearerToken: String = "Bearer authToken"

  val id: String = "ID"
  val fetchMrnStatusUrl: String = "/mrn/" + id + "/status"
}
