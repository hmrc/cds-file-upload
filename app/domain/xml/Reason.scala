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

package domain.xml

sealed trait Reason

object Reason {

  case object Quarantined extends Reason
  case object Rejected    extends Reason
  case object Unknown     extends Reason

  def fromString(s: String): Option[Reason] =
    s.toLowerCase match {
      case "quarantined" => Some(Quarantined)
      case "rejected"    => Some(Rejected)
      case "unknown"     => Some(Unknown)
      case _             => None
    }
}
