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

package base

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import controllers.actions.AuthActionImpl
import models.{Eori, SignedInUser}
import org.mockito.{ArgumentMatcher, ArgumentMatchers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, reset, when}
import org.mockito.stubbing.ScalaOngoingStubbing
import play.api.test.Helpers._
import testdata.TestData
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments

trait AuthActionMock extends UnitSpec {

  private val internalId = "internalId"

  val mockAuthConnector = mock[AuthConnector]
  val cc = stubControllerComponents()
  val authAction = new AuthActionImpl(mockAuthConnector, stubControllerComponents())(global)
  val enrolment: Predicate = Enrolment("HMRC-CUS-ORG")
  val userEori = Eori(TestData.eori)

  def cdsEnrollmentMatcher(user: SignedInUser): ArgumentMatcher[Predicate] = new ArgumentMatcher[Predicate] {
    override def matches(p: Predicate): Boolean =
      p == enrolment && user.enrolments.getEnrolment("HMRC-CUS-ORG").isDefined
  }

  override protected def afterEach(): Unit = {
    reset(mockAuthConnector)

    super.afterEach()
  }

  def authorisedUser(): ScalaOngoingStubbing[Future[Option[String]]] =
    when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any())).thenReturn(Future.successful(Some(internalId)))

  def nonAuthorisedUser(): ScalaOngoingStubbing[Future[Option[String]]] =
    when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any())).thenReturn(Future.successful(None))

  def withAuthorizedUser(user: SignedInUser = newUser(userEori.value, "external1")): Unit =
    when(mockAuthConnector.authorise(ArgumentMatchers.argThat(cdsEnrollmentMatcher(user)), ArgumentMatchers.eq(allEnrolments))(any(), any()))
      .thenReturn(Future.successful(user.enrolments))

  def newUser(eori: String, externalId: String): SignedInUser = SignedInUser(
    Credentials("2345235235", "GovernmentGateway"),
    Name(Some("Aldo"), Some("Rain")),
    Some("amina@hmrc.co.uk"),
    eori,
    externalId,
    Some("Int-ba17b467-90f3-42b6-9570-73be7b78eb2b"),
    Some(AffinityGroup.Individual),
    Enrolments(
      Set(
        Enrolment("IR-SA", List(EnrolmentIdentifier("UTR", "111111111")), "Activated", None),
        Enrolment("IR-CT", List(EnrolmentIdentifier("UTR", "222222222")), "Activated", None),
        Enrolment("HMRC-CUS-ORG", List(EnrolmentIdentifier("EORINumber", eori)), "Activated", None)
      )
    )
  )
}
