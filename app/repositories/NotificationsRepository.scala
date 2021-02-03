/*
 * Copyright 2021 HM Revenue & Customs
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

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.Notification
import play.api.Logger
import play.api.libs.json.{JsNull, JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONNull, BSONObjectID}
import reactivemongo.play.json.collection._
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationsRepository @Inject()(mc: ReactiveMongoComponent, appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends ReactiveRepository[Notification, BSONObjectID](
      collectionName = "notifications",
      mongo = mc.mongoConnector.db,
      domainFormat = Notification.DbFormat.notificationFormat,
      idFormat = ReactiveMongoFormats.objectIdFormats
    ) {

  override lazy val collection: JSONCollection =
    mongo().collection[JSONCollection](collectionName, failoverStrategy = RepositorySettings.failoverStrategy)

  override def indexes: Seq[Index] = Seq(
    Index(key = Seq(("details.fileReference", IndexType.Ascending)), name = Some("detailsFileReferenceIdx")),
    Index(
      key = Seq(("createdAt", IndexType.Ascending)),
      name = Some("createdAtIndex"),
      options = BSONDocument("expireAfterSeconds" -> appConfig.notificationsTtlSeconds)
    ),
    Index(Seq("details" -> IndexType.Ascending), name = Some("detailsMissingIdx"), partialFilter = Some(BSONDocument("details" -> BSONNull)))
  )

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = Future.successful(Seq.empty)

  def save(notification: Notification): Future[Either[Throwable, Unit]] =
    insert(notification)
      .map(_ => Right(()))
      .recover { case e => Left(e) }

  def updateNotification(notification: Notification): Future[Boolean] =
    findAndUpdate(query = Json.obj("_id" -> notification._id), update = Json.toJson(notification).as[JsObject]).map { result =>
      result.lastError.foreach(_.err.foreach(errorMsg => logger.error(s"Problem during database update: $errorMsg")))
      result.lastError.isEmpty
    }

  def ensureIndex(index: Index)(implicit ec: ExecutionContext): Future[Unit] =
    collection.indexesManager
      .create(index)
      .map(wr => logger.info(wr.toString))
      .recover {
        case t =>
          logger.warn(s"$message (${index.eventualName})", t)
      }

  def findNotificationsByReference(reference: String): Future[Seq[Notification]] =
    find("details.fileReference" -> reference)

  def findUnparsedNotifications(): Future[Seq[Notification]] =
    find("details" -> JsNull)

  Future.sequence(indexes.map(ensureIndex))
}
