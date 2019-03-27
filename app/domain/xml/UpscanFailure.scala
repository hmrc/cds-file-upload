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

import scala.xml.NodeSeq

sealed abstract case class UpscanFailure(reference: String, reason: String, message: String)

object UpscanFailure {

  val FAILED = "FAILED"

  def parse(xml: NodeSeq): Option[UpscanFailure] =
    for {
      reference <- (xml \\ "reference").headOption
      _         <- (xml \\ "fileStatus").find(_.text == FAILED)
    } yield {
      val reason  = (xml \\ "failureReason").headOption.fold("")(_.text)
      val message = (xml \\ "message").headOption.fold("")(_.text)

      new UpscanFailure(reference.text, reason, message) {}
    }
}