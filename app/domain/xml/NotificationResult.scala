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

import domain.FileState
import domain.xml.Outcome.{Failure, Success}
import domain.xml.Reason.{Quarantined, Rejected, Unknown}

import scala.xml.NodeSeq

sealed trait NotificationResult { self =>

  val reference: String

  lazy val fileState: FileState = self match {
    case FileTransmissionResult(_, fileName, Success) => FileState.Successful(fileName)
    case FileTransmissionResult(_, fileName, Failure) => FileState.Failed(fileName)
    case UpscanFailure(_, Quarantined, _)             => FileState.VirusDetected
    case UpscanFailure(_, Rejected, _)                => FileState.UnacceptableMimeType
    case UpscanFailure(_, Unknown, _)                 => FileState.UnknownFailure
  }
}

object NotificationResult {

  def parse(xml: NodeSeq): Option[NotificationResult] =
    FileTransmissionResult.parse(xml) orElse UpscanFailure.parse(xml)
}

sealed abstract case class FileTransmissionResult(reference: String, fileName: String, outcome: Outcome)
  extends NotificationResult

object FileTransmissionResult {

  private[xml] def parse(xml: NodeSeq): Option[FileTransmissionResult] =
    for {
      ref     <- (xml \\ "FileReference").headOption
      outcome <- (xml \\ "Outcome").headOption.flatMap(n => Outcome.fromString(n.text))
    } yield {
      val fileName = (xml \\ "FileName").headOption.fold("")(_.text)
      new FileTransmissionResult(ref.text, fileName, outcome) {}
    }
}

sealed abstract case class UpscanFailure(reference: String, reason: Reason, message: String)
  extends NotificationResult

object UpscanFailure {

  val FAILED = "FAILED"

  private[xml] def parse(xml: NodeSeq): Option[UpscanFailure] =
    for {
      reference <- (xml \\ "reference").headOption
      _         <- (xml \\ "fileStatus").find(_.text == FAILED)
      reason    <- (xml \\ "failureReason").headOption.flatMap(r => Reason.fromString(r.text))
    } yield {
      val message = (xml \\ "message").headOption.fold("")(_.text)

      new UpscanFailure(reference.text, reason, message) {}
    }
}