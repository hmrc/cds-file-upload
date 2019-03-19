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

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Json}

sealed trait FileState

object FileState {

  case object Uploaded extends FileState
  case object Success  extends FileState
  case object Failed   extends FileState

  implicit val formats = new Format[FileState] {
    override def writes(o: FileState): JsValue = o match {
      case Uploaded => JsString("uploaded")
      case Success  => JsString("success")
      case Failed   => JsString("failed")
    }

    override def reads(json: JsValue): JsResult[FileState] = json match {
      case JsString("uploaded") => JsSuccess(Uploaded)
      case JsString("success")  => JsSuccess(Success)
      case JsString("failed")   => JsSuccess(Failed)
      case _                    => JsError("Unable to parse file state")
    }
  }
}

case class EORI(value: String)

case class MRN(value: String)

object MRN {

  implicit val formats = Json.format[MRN]
}

case class File(reference: String, state: FileState)

object File {

  implicit val formats = Json.format[File]
}

case class BatchFileUpload(mrn: MRN, files: List[File])

object BatchFileUpload {

  implicit val formats = Json.format[BatchFileUpload]
}