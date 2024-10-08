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

package connectors

import config.AppConfig
import models.email.{Email, EmailResponse}
import play.api.http.Status.NOT_FOUND
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsDataStoreConnector @Inject() (implicit httpClientV2: HttpClientV2, appConfig: AppConfig, ec: ExecutionContext) extends Connector {

  import CustomsDataStoreConnector._

  protected def httpClient: HttpClientV2 = httpClientV2

  def getEmailAddress(eori: String)(implicit hc: HeaderCarrier): Future[Option[Email]] =
    get[EmailResponse](verifiedEmailUrl(eori)).map {
      case EmailResponse(email, _, None) => Some(Email(email, deliverable = true))
      case EmailResponse(email, _, _)    => Some(Email(email, deliverable = false))
    }.recover { case UpstreamErrorResponse(_, NOT_FOUND, _, _) =>
      None
    }
}

object CustomsDataStoreConnector {

  def verifiedEmailPath(eori: String)(implicit appConfig: AppConfig): String =
    s"${appConfig.verifiedEmailPath.replace("EORI", eori)}"

  def verifiedEmailUrl(eori: String)(implicit appConfig: AppConfig): String =
    s"${appConfig.customsDataStoreBaseUrl}${verifiedEmailPath(eori)}"
}
