/*
 * Copyright 2023 HM Revenue & Customs
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

package migrations.changelogs.notification

import com.mongodb.client.MongoDatabase
import migrations.changelogs.{MigrationDefinition, MigrationInformation}
import org.bson.{BsonType, Document}
import org.mongodb.scala.model.Filters.{`type`, equal, not}
import play.api.Logging

import scala.jdk.CollectionConverters.IterableHasAsScala

class PurgeExpiredNotifications extends MigrationDefinition with Logging {

  override val migrationInformation: MigrationInformation =
    MigrationInformation(
      id = "CEDS-4869: Purge 'createdAt' String notifications and upgrade 'createdAtIndex'",
      order = 2,
      author = "Mohammad Dweik",
    )

  override def migrationFunction(db: MongoDatabase): Unit = {
    val createdAt = "createdAt"
    val notificationsCollection = db.getCollection("notifications")

    logger.info(s"Applying '${migrationInformation.id}' db migration...")

    val redundantIndexToBeDeleted = Vector("createdAtIndex")
    notificationsCollection.listIndexes().iterator().forEachRemaining { idx =>
      val indexName = idx.getString("name")
      if (redundantIndexToBeDeleted.contains(indexName))
        notificationsCollection.dropIndex(indexName)

      def filter = not(`type`(createdAt, BsonType.DATE_TIME))

      try {
        val recordsToDelete: Iterable[Document] = notificationsCollection
          .find(filter)
          .asScala

        val totalDeleted = recordsToDelete.foldLeft(0) { (count, document) =>
          val documentId = document.get("_id")
          val docFilter = equal("_id", documentId)
          val deleteResult = notificationsCollection.deleteOne(docFilter)
          count + deleteResult.getDeletedCount.toInt
        }

        logger.info(s"Deleted $totalDeleted records from the Notifications collection where 'createdAt' was not a Date.")
        logger.info(s"Finished applying '${migrationInformation.id}' db migration.")
      } catch {
        case e: Exception => logger.error(s"An error occurred during the db migration '${migrationInformation.id}'", e)
      }

    }
  }
}
