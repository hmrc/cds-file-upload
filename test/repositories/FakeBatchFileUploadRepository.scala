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

package repositories

import domain.{BatchFileUpload, EORI}

import scala.concurrent.Future

class FakeBatchFileUploadRepository(seed: List[BatchFileUpload]) extends BatchFileUploadRepository {

  private var store = seed

  override def put(eori: EORI, data: BatchFileUpload): Future[Unit] =
    Future.successful { store = data :: store; () }

  override def putAll(eori: EORI, data: List[BatchFileUpload]): Future[Unit] =
    Future.successful { store = data; () }

  override def getAll(eori: EORI): Future[List[BatchFileUpload]] =
    Future.successful(store)

  override def started: Future[Boolean] = Future.successful(true)
}