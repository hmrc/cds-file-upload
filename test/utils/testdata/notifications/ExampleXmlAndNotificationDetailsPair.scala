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

package testdata.notifications

import models.NotificationDetails
import testdata.notifications.NotificationsTestData._

import scala.xml.Elem

final case class ExampleXmlAndNotificationDetailsPair(asXml: Elem = <xml/>, asDomainModel: NotificationDetails = emptyNotificationDetails)
    extends ExampleXmlAndDomainModelPair[NotificationDetails]

object ExampleXmlAndNotificationDetailsPair {

  def exampleNotification(
    fileReference: String = fileReference,
    outcome: String = outcomeSuccess,
    filename: String = filename,
    batchId: String = batchId
  ) =
    ExampleXmlAndNotificationDetailsPair(asXml = <Root>
        <FileReference>{fileReference}</FileReference>
        <BatchId>{batchId}</BatchId>
        <FileName>{filename}</FileName>
        <Outcome>{outcome}</Outcome>
        <Details>[detail block]</Details>
      </Root>, asDomainModel = NotificationDetails(fileReference, outcome, Some(filename)))

  def exampleNotificationMandatoryElementsOnly(fileReference: String = fileReference, outcome: String = outcomeSuccess, filename: String = filename) =
    ExampleXmlAndNotificationDetailsPair(asXml = <Root>
          <FileReference>{fileReference}</FileReference>
          <FileName>{filename}</FileName>
          <Outcome>{outcome}</Outcome>
        </Root>, asDomainModel = NotificationDetails(fileReference, outcome, Some(filename)))

  def exampleNotificationMissingFileReference(outcome: String = outcomeSuccess, filename: String = filename, batchId: String = batchId) =
    ExampleXmlAndNotificationDetailsPair(asXml = <Root>
          <BatchId>{batchId}</BatchId>
          <FileName>{filename}</FileName>
          <Outcome>{outcome}</Outcome>
        </Root>, asDomainModel = NotificationDetails("", outcome, Some(filename)))

  def exampleNotificationMissingFileName(fileReference: String = fileReference, outcome: String = outcomeSuccess, batchId: String = batchId) =
    ExampleXmlAndNotificationDetailsPair(asXml = <Root>
          <FileReference>{fileReference}</FileReference>
          <BatchId>{batchId}</BatchId>
          <Outcome>{outcome}</Outcome>
        </Root>, asDomainModel = NotificationDetails(fileReference, outcome, None))

  def exampleNotificationMissingOutcome(fileReference: String = fileReference, filename: String = filename, batchId: String = batchId) =
    ExampleXmlAndNotificationDetailsPair(asXml = <Root>
          <FileReference>{fileReference}</FileReference>
          <BatchId>{batchId}</BatchId>
          <FileName>{filename}</FileName>
        </Root>, asDomainModel = NotificationDetails(fileReference, "", Some(filename)))

}
