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

import config.AppConfig
import javax.inject.Inject
import models.dis.parsers.DeclarationStatusParser
import models.dis.{DeclarationStatus, XmlTags}
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import play.mvc.Http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, InternalServerException}

import scala.concurrent.{ExecutionContext, Future}

class CustomsDeclarationsInformationConnector @Inject()(
  declarationStatusParser: DeclarationStatusParser,
  appConfig: AppConfig,
  httpClient: HttpClient
)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def getDeclarationStatus(mrn: String)(implicit hc: HeaderCarrier): Future[Option[DeclarationStatus]] =
    httpClient
      .doGet(url = s"${appConfig.customsDeclarationsInformationBaseUrl}${appConfig.fetchMrnStatus.replace(XmlTags.id, mrn)}", headers = headers())
      .map { response =>
        response.status match {
          case OK =>
            logger.debug(loggerMessage(response))
            Some(declarationStatusParser.parse(xml.XML.loadString(response.body)))
          case NOT_FOUND =>
            logger.debug(loggerMessage(response))
            None
          case status =>
            logger.warn(loggerMessage(response))
            throw new InternalServerException(s"Customs Declarations Information Service returned [$status]")
        }
      }

  private def headers(): Seq[(String, String)] = Seq(
    "X-Client-ID" -> appConfig.cdiClientId,
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.${appConfig.cdiApiVersion}+xml",
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
    HeaderNames.CACHE_CONTROL -> "no-cache"
  )

  private def loggerMessage(response: HttpResponse): String =
    s"CUSTOMS_DECLARATIONS_INFORMATION respond with status: ${response.status}, body: ${response.body}"
}
