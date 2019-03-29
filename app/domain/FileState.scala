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

package domain

import play.api.libs.json.{Format, JsError, JsObject, JsResult, JsString, JsSuccess, JsValue, Json}

sealed trait FileState

object FileState {

  final case class Waiting(uploadRequest: UploadRequest) extends FileState
  case object Uploaded extends FileState
  case class Successful(fileName: String) extends FileState
  case class Failed(fileName: String) extends FileState
  case object VirusDetected extends FileState
  case object UnacceptableMimeType extends FileState
  case object UnknownFailure extends FileState

  object Waiting {

    implicit val format = Json.format[Waiting]
  }

  object Successful {

    private val jsonKey = "successful"

    implicit val format = new Format[Successful] {
      override def reads(json: JsValue): JsResult[Successful] =
        (json \\ jsonKey)
          .headOption
          .flatMap(s => s.asOpt[String].map(Successful(_)))
          .fold[JsResult[Successful]](JsError(""))(JsSuccess(_))

      override def writes(o: Successful): JsValue =
        JsObject(Map(jsonKey -> Json.toJson(o.fileName)))
    }
  }

  object Failed {

    private val jsonKey = "failed"

    implicit val format = new Format[Failed] {
      override def reads(json: JsValue): JsResult[Failed] =
        (json \\ jsonKey)
          .headOption
          .flatMap(s => s.asOpt[String].map(Failed(_)))
          .fold[JsResult[Failed]](JsError(""))(JsSuccess(_))

      override def writes(o: Failed): JsValue =
        JsObject(Map(jsonKey -> Json.toJson(o.fileName)))
    }
  }

  private val uploaded = "uploaded"
  private val virus    = "virus"
  private val mimeType = "mimeType"
  private val unknown  = "unknown"

  implicit val format = new Format[FileState] {

    override def writes(o: FileState): JsValue = o match {
      case w@Waiting(_)         => Json.toJson(w)
      case w@Successful(_)      => Json.toJson(w)
      case w@Failed(_)          => Json.toJson(w)
      case Uploaded             => JsString(uploaded)
      case VirusDetected        => JsString(virus)
      case UnacceptableMimeType => JsString(mimeType)
      case UnknownFailure       => JsString(unknown)
    }

    override def reads(json: JsValue): JsResult[FileState] = json match {
      case JsString(`uploaded`) => JsSuccess(Uploaded)
      case JsString(`virus`)    => JsSuccess(VirusDetected)
      case JsString(`mimeType`) => JsSuccess(UnacceptableMimeType)
      case JsString(`unknown`)  => JsSuccess(UnknownFailure)
      case js                   =>
        Json.fromJson[Waiting](js) orElse
        Json.fromJson[Successful](js) orElse
        Json.fromJson[Failed](js)
    }
  }
}