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

package config

import pureconfig.generic.ProductHint
import pureconfig.{CamelCase, ConfigFieldMapping, KebabCase}

import scala.concurrent.duration.Duration

case class AppConfig(mongodb: Mongo, notifications: Notifications, microservice: Microservice)

object AppConfig {
  implicit val hint: ProductHint[AppConfig] = ProductHint(new ConfigFieldMapping {
    def apply(fieldName: String): String =
      KebabCase.fromTokens(CamelCase.toTokens(fieldName))
  })
}

case class Mongo(uri: String, encryptionEnabled: Boolean, ttl: Duration)

case class Notifications(ttlSeconds: Int)

case class Microservice(services: Services)

case class Services(customsDeclarationsInformation: CustomsDeclarationsInformation)

case class CustomsDeclarationsInformation(
  host: String,
  port: Option[Int],
  fetchMrnStatussdfsdf: String,
  apiVersion: String,
  bearerToken: String,
  clientId: String
)
