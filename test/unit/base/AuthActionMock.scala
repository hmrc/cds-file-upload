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
import controllers.actions.{AuthActionImpl, AuthActionWithEori}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, reset, when}
import org.mockito.stubbing.ScalaOngoingStubbing
import play.api.test.Helpers._
import testdata.TestData.eori
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, EnrolmentIdentifier, Enrolments}

trait AuthActionMock extends UnitSpec {

  private val internalId = "internalId"
  private val authConnector = mock[AuthConnector]

  val authAction = new AuthActionImpl(authConnector, stubControllerComponents())(global)
  val authActionWithEori = new AuthActionWithEori(authConnector, stubControllerComponents())(global)

  override protected def afterEach(): Unit = {
    reset(authConnector)

    super.afterEach()
  }

  def authorisedUser(): ScalaOngoingStubbing[Future[Option[String]]] =
    when(authConnector.authorise[Option[String]](any(), any())(any(), any())).thenReturn(Future.successful(Some(internalId)))

  def authorisedUserWithEori(): Unit =
    when(authConnector.authorise[Enrolments](any(), any())(any(), any()))
      .thenReturn(Future.successful(Enrolments(Set(Enrolment("HMRC-CUS-ORG", List(EnrolmentIdentifier("EORINumber", eori)), "Activated", None)))))

  def nonAuthorisedUser(): ScalaOngoingStubbing[Future[Option[String]]] =
    when(authConnector.authorise[Option[String]](any(), any())(any(), any())).thenReturn(Future.successful(None))
}
