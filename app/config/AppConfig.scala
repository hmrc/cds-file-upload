/*
 * Copyright 2019 HM Revenue & Customs
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

import pureconfig.{CamelCase, ConfigFieldMapping, KebabCase, ProductHint}

import scala.concurrent.duration._

case class AppConfig(microservice: Microservices, mongodb: Mongo)

object AppConfig {
  implicit val hint: ProductHint[AppConfig] = ProductHint(new ConfigFieldMapping {
    def apply(fieldName: String): String =
      KebabCase.fromTokens(CamelCase.toTokens(fieldName))
  })

  val empty: AppConfig = AppConfig(Microservices.empty, Mongo.empty)
}

case class Microservices(services: Services)

object Microservices {

  val empty: Microservices = Microservices(Services.empty)
}

case class Services(customsDeclarations: CustomsDeclarations)

object Services {

  val empty: Services = Services(CustomsDeclarations.empty)
}

case class CustomsDeclarations(host: String, port: Int, protocol: String, bearerToken: String)

object CustomsDeclarations {

  val empty: CustomsDeclarations = CustomsDeclarations("", 0, "", "")
}

case class Mongo(uri: String, encryptionEnabled: Boolean, ttl: Duration)

object Mongo {

  val empty: Mongo = Mongo("", false, 0.seconds)
}