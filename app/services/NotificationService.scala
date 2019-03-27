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

package services

import com.google.inject._
import com.softwaremill.quicklens._
import domain.{EORI, FileState}
import repositories.BatchFileUploadRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationService @Inject()(cache: BatchFileUploadRepository)(implicit ec: ExecutionContext) {

  def handle(eori: EORI, ref: String, state: FileState): Future[Unit] = {

    cache.getAll(eori).flatMap { batches =>

      val updatedBatch =
        batches
          .modify(_.each.files.eachWhere(_.reference == ref).state)
          .setTo(state)

      cache.putAll(eori, updatedBatch)
    }
  }
}