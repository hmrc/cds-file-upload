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

package migrations.changelogs.notification

import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{and, exists, or, eq => feq}
import org.mongodb.scala.model.UpdateOneModel
import org.mongodb.scala.model.Updates.{combine, set, unset}
import play.api.Logger
import uk.gov.hmrc.exports.migrations.changelogs.{MigrationDefinition, MigrationInformation}

import scala.collection.JavaConverters._

class MakeParsedDetailsOptional extends MigrationDefinition {

  private val logger = Logger(this.getClass)

  private val INDEX_ID = "_id"
  private val FILEREF = "fileReference"
  private val OUTCOME = "outcome"
  private val FILENAME = "filename"
  private val DETAILS = "details"
  private val PAYLOAD = "payload"

  private val DEFAULT_PAYLOAD_VALUE = "N/A"

  private val collectionName = "notifications"

  override val migrationInformation: MigrationInformation =
    MigrationInformation(
      id = "CEDS-2786 Make the parsed detail fields fileReference, outcome and filename optional",
      order = 1,
      author = "Tim Wilkins",
      runAlways = true
    )

  override def migrationFunction(db: MongoDatabase): Unit = {

    logger.info(s"Applying '${migrationInformation.id}' db migration...")

    val query = or(exists(FILEREF), exists(OUTCOME), exists(FILENAME))
    val queryBatchSize = 10
    val updateBatchSize = 100

    def createDetailsSubDoc(document: Document) = {
      val fileReference = document.get(FILEREF).asInstanceOf[String]
      val outcome = document.get(OUTCOME).asInstanceOf[String]
      val filename = document.get(FILENAME).asInstanceOf[String]

      new Document()
        .append(FILEREF, fileReference)
        .append(OUTCOME, outcome)
        .append(FILENAME, filename)
    }

    val collection = db.getCollection(collectionName)

    val redundantIndexesToBeDeleted = Vector("fileReferenceIndex")
    collection.listIndexes().iterator().forEachRemaining { idx =>
      val indexName = idx.getString("name")
      if (redundantIndexesToBeDeleted.contains(indexName))
        collection.dropIndex(indexName)
    }

    getDocumentsToUpdate(db, query, queryBatchSize).map { document =>
      val documentId = document.get(INDEX_ID)

      val detailsSubDoc = createDetailsSubDoc(document)
      logger.info(s"Creating new sub-document: $detailsSubDoc for documentId=$documentId")

      val filter = and(feq(INDEX_ID, documentId))
      val update = combine(unset(FILEREF), unset(OUTCOME), unset(FILENAME), set(PAYLOAD, DEFAULT_PAYLOAD_VALUE), set(DETAILS, detailsSubDoc))
      logger.info(s"[filter: $filter] [update: $update]")

      new UpdateOneModel[Document](filter, update)
    }.grouped(updateBatchSize).zipWithIndex.foreach {
      case (requests, idx) =>
        logger.info(s"Updating batch no. $idx...")

        collection.bulkWrite(seqAsJavaList(requests))
        logger.info(s"Updated batch no. $idx")
    }

    logger.info(s"Applying '${migrationInformation.id}' db migration... Done.")
  }

  private def getDocumentsToUpdate(db: MongoDatabase, filter: Bson, queryBatchSize: Int) =
    asScalaIterator(
      db.getCollection(collectionName)
        .find(filter)
        .batchSize(queryBatchSize)
        .iterator
    )
}
