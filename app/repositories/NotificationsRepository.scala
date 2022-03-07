/*
 * Copyright 2022 HM Revenue & Customs
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

import com.mongodb.client.model.Indexes.ascending
import config.AppConfig
import models.Notification
import models.Notification.MongoFormat
import org.bson.BsonNull
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

@Singleton
class NotificationsRepository @Inject()(mongoComponent: MongoComponent, appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[Notification](
      mongoComponent = mongoComponent,
      collectionName = "notifications",
      domainFormat = MongoFormat.format,
      indexes = NotificationRepository.indexes(appConfig)
    ) with RepositoryOps[Notification] {

  override def classTag: ClassTag[Notification] = implicitly[ClassTag[Notification]]
  implicit val executionContext = ec

  def findNotificationsByReference(fileReference: String): Future[Seq[Notification]] =
    findAll("details.fileReference", fileReference)
}

object NotificationRepository {

  def indexes(appConfig: AppConfig): Seq[IndexModel] =
    List(
      IndexModel(ascending("details.fileReference"), IndexOptions().name("detailsFileReferenceIdx")),
      IndexModel(
        ascending("details"),
        IndexOptions()
          .name("detailsMissingIdx")
          .partialFilterExpression(BsonDocument("details" -> BsonNull.VALUE))
      ),
      IndexModel(
        ascending("createdAt"),
        IndexOptions()
          .name("createdAtIndex")
          .expireAfter(appConfig.notificationsTtlSeconds, TimeUnit.SECONDS)
      )
    )
}
