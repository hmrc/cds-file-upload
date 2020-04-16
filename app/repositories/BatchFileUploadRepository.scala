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

package repositories

import com.google.inject.Inject
import config.AppConfig
import play.api.libs.json.{JsArray, JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import config.Crypto
import domain.{BatchFileUpload, BatchFileUploadDbModel, EORI}
import javax.inject.Singleton
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BatchFileUploadRepository @Inject()(mongo: ReactiveMongoComponent, appConfig: AppConfig, crypto: Crypto)(implicit ec: ExecutionContext)
    extends ReactiveRepository[BatchFileUploadDbModel, BSONObjectID](
      collectionName = "fileUpload",
      mongo = mongo.mongoConnector.db,
      domainFormat = BatchFileUploadDbModel.format,
      idFormat = ReactiveMongoFormats.objectIdFormats
    ) {

  import crypto._

  private val expireAfterSecond = "expireAfterSeconds"
  private val idField = "eori"
  private val dataField = "data"

  override def indexes: Seq[Index] = Seq(
    Index(
      key = Seq(("eori", IndexType.Ascending)),
      name = Some("eori_index"),
      unique = true,
      options = BSONDocument(expireAfterSecond -> appConfig.mongodb.ttl.toSeconds)
    )
  )

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = Future.successful(Seq.empty)

  def ensureIndex(index: Index)(implicit ec: ExecutionContext): Future[Unit] =
    collection.indexesManager
      .create(index)
      .map(wr => logger.info(wr.toString))
      .recover {
        case t =>
          logger.warn(s"$message (${index.eventualName})", t)
      }

  Future.sequence(indexes.map(ensureIndex))

  def put(eori: EORI, data: BatchFileUpload): Future[Unit] = {

    val selector = Json.obj(idField -> eori.value)

    val modifier = Json.obj("$push" -> Json.obj(dataField -> encrypt(data)))

    collection.findAndUpdate(selector, modifier, upsert = true).map(_ => ())
  }

  def getAll(eori: EORI): Future[List[BatchFileUpload]] =
    collection
      .find(Json.obj(idField -> eori.value), None)
      .one[JsValue]
      .map(_.flatMap { json =>
        (json \ dataField)
          .asOpt[JsArray]
          .map(_.value.toList)
          .map(_.map(decrypt[BatchFileUpload]).collect {
            case Some(value) => value
          })
      }.getOrElse(Nil))
}
