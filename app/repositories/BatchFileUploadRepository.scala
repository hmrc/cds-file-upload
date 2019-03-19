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

import com.google.inject.{ImplementedBy, Inject}
import config.AppConfig
import play.api.libs.json.{JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import config.Crypto
import domain.{BatchFileUpload, EORI, File}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[MongoBatchFileUploadRepository])
trait BatchFileUploadRepository extends Repository {

  def put(eori: EORI, data: List[BatchFileUpload]): Future[Unit]

  def getAll(eori: EORI): Future[List[BatchFileUpload]]
}

class MongoBatchFileUploadRepository @Inject()(mongo: ReactiveMongoApi,
                                                  appConfig: AppConfig,
                                                  crypto: Crypto)(implicit ex: ExecutionContext)
  extends BatchFileUploadRepository {

  import crypto._

  private val collectionName: String = "fileUpload"
  private val expireAfterSecond = "expireAfterSeconds"
  private val ttl = appConfig.mongodb.ttl
  private val idField = "eori"
  private val dataField = "data"

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  private val index = Index(
    key = Seq(("eori", IndexType.Ascending)),
    name = Some("eori_index"),
    unique = true,
    options = BSONDocument(expireAfterSecond -> ttl.toSeconds)
  )

  def put(eori: EORI, data: List[BatchFileUpload]): Future[Unit] = {

    val selector = Json.obj(idField -> eori.value)

    val modifier = Json.obj(
      "$set" -> Json.obj(
        dataField -> encrypt(data)
      )
    )

    collection.flatMap {
      _.findAndUpdate(selector, modifier, upsert = true).map(_ => ())
    }
  }

  def getAll(eori: EORI): Future[List[BatchFileUpload]] =
    collection
      .flatMap(_.find(Json.obj(idField -> eori.value), None).one[JsValue])
      .map(_.flatMap { json =>
        (json \ dataField).asOpt[JsValue].flatMap(decrypt[List[BatchFileUpload]])
      }.getOrElse(Nil))

  val started: Future[Boolean] = collection.flatMap(_.indexesManager.ensure(index))

}


