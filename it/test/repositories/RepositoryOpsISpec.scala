/*
 * Copyright 2024 HM Revenue & Customs
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

import base.{TestMongoDB, UnitSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import testdata.notifications.NotificationsTestData._

class RepositoryOpsISpec extends UnitSpec with GuiceOneAppPerSuite {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[com.codahale.metrics.MetricRegistry]
      .configure(TestMongoDB.mongoConfiguration)
      .build()

  private val repository = fakeApplication().injector.instanceOf[NotificationsRepository]

  private val document1 = parsedNotification
  private val document2 = unparsedNotification

  private val keyId = "details.fileReference"
  private val keyValue = fileReference

  override def beforeEach(): Unit = {
    super.beforeEach()
    repository.removeAll.futureValue
  }

  override def afterEach(): Unit = {
    repository.removeAll.futureValue
    super.afterEach()
  }

  "RepositoryOps.findAll" should {

    "return an empty list" when {
      "the collection is empty" in {
        repository.findAll.futureValue.size mustBe 0
      }
    }

    "return all documents" when {
      "the collection is NOT empty" in {
        repository.insertOne(document1).futureValue
        repository.insertOne(document1).futureValue

        repository.findAll.futureValue.size mustBe 2
      }
    }
  }

  "RepositoryOps.findAll for a given key" should {

    "return an empty list" when {
      "the collection does not contain documents for that key" in {
        repository.insertOne(document2).futureValue
        repository.findAll(keyId, keyValue).futureValue.size mustBe 0
      }
    }

    "return all documents for that key" when {
      "the collection does contain documents for that key" in {
        repository.insertOne(document1).futureValue
        repository.insertOne(document2).futureValue

        repository.findAll(keyId, keyValue).futureValue.size mustBe 1
      }
    }
  }

  "RepositoryOps.findOne" should {

    "return None" when {
      "the collection does not contain documents for the given key" in {
        repository.insertOne(document2).futureValue
        repository.findOne(keyId, keyValue).futureValue mustBe None
      }
    }

    "return the expected document" when {
      "the collection does contain at least one document for the given key" in {
        repository.insertOne(document1).futureValue
        repository.insertOne(document2).futureValue

        repository.findOne(keyId, keyValue).futureValue.value mustBe document1
      }
    }
  }

  "RepositoryOps.findOneOrCreate" should {

    "create and return the given document" when {
      "the collection does not contain documents for the given key" in {
        repository.insertOne(document2).futureValue
        repository.findOneOrCreate(keyId, keyValue, document1).futureValue mustBe document1
        repository.size.futureValue mustBe 2
      }
    }

    "return the expected document (and not create a new one)" when {
      "the collection does contain a document for the given key" in {
        repository.insertOne(document1).futureValue
        repository.findOneOrCreate(keyId, keyValue, document2).futureValue mustBe document1
        repository.size.futureValue mustBe 1
      }
    }
  }

  "RepositoryOps.findOneAndReplace" should {

    "create and return the given document" when {
      "the collection does not contain documents for the given key" in {
        repository.insertOne(document2).futureValue
        repository.findOneAndReplace(keyId, keyValue, document1).futureValue mustBe document1
        repository.size.futureValue mustBe 2
      }
    }

    "return the updated document" when {
      "the collection does contain a document for the given key" in {
        repository.insertOne(document1).futureValue
        val updated = document1.copy(payload = "Updated payload")
        repository.findOneAndReplace(keyId, keyValue, updated).futureValue mustBe updated
        repository.size.futureValue mustBe 1
      }
    }
  }

  "RepositoryOps.findOneAndRemove" should {

    "return None" when {
      "the collection does not contain documents for the given key" in {
        repository.insertOne(document2).futureValue
        repository.findOneAndRemove(keyId, keyValue).futureValue mustBe None
        repository.size.futureValue mustBe 1
      }
    }

    "return the removed document" when {
      "the collection does contain at least one document for the given key" in {
        repository.insertOne(document1).futureValue
        repository.insertOne(document2).futureValue
        repository.size.futureValue mustBe 2

        repository.findOneAndRemove(keyId, keyValue).futureValue.value mustBe document1
        repository.size.futureValue mustBe 1
      }
    }
  }

  "RepositoryOps.insertOne" should {
    "create and return the given documents" in {
      repository.size.futureValue mustBe 0

      repository.insertOne(document1).futureValue mustBe Right(document1)
      repository.size.futureValue mustBe 1

      repository.insertOne(document2).futureValue mustBe Right(document2)
      repository.size.futureValue mustBe 2
    }
  }

  "RepositoryOps.removeAll" should {
    "remove all collection's documents" in {
      repository.insertOne(document1).futureValue mustBe Right(document1)
      repository.insertOne(document2).futureValue mustBe Right(document2)
      repository.size.futureValue mustBe 2

      repository.removeAll.futureValue
      repository.size.futureValue mustBe 0
    }
  }

  "RepositoryOps.removeEvery" should {
    "remove all documents for the given key" in {
      repository.insertOne(document1).futureValue mustBe Right(document1)
      repository.insertOne(document1).futureValue mustBe Right(document1)
      repository.insertOne(document2).futureValue mustBe Right(document2)
      repository.size.futureValue mustBe 3

      repository.removeEvery(keyId, keyValue).futureValue
      repository.size.futureValue mustBe 1
    }
  }

  "RepositoryOps.removeOne" should {
    "remove all documents for the given key" in {
      repository.insertOne(document1).futureValue mustBe Right(document1)
      repository.insertOne(document1).futureValue mustBe Right(document1)
      repository.insertOne(document2).futureValue mustBe Right(document2)
      repository.size.futureValue mustBe 3

      repository.removeOne(keyId, keyValue).futureValue
      repository.size.futureValue mustBe 2
    }
  }
}
