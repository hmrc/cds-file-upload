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

package migrations.repositories

import com.mongodb.client.MongoDatabase
import org.bson.Document
import migrations.repositories.ChangeEntry.{KeyAuthor, KeyChangeId}

import scala.collection.JavaConverters.asScalaIterator

class ChangeEntryRepository(collectionName: String, mongoDatabase: MongoDatabase)
    extends MongoRepository(mongoDatabase, collectionName, Array(KeyAuthor, KeyChangeId)) {

  private[migrations] def findAll(): List[Document] = asScalaIterator(collection.find().iterator()).toList

  private[migrations] def save(changeEntry: ChangeEntry): Unit = collection.insertOne(changeEntry.buildFullDBObject)

}
