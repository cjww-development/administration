/*
 * Copyright 2018 CJWW Development
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

package helpers.services

import com.cjwwdev.mongo.responses._
import common.MissingAccountException
import helpers.other.Fixtures
import models.Account
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import services.ManagementAccountService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockManagementAccountService extends BeforeAndAfterEach with MockitoSugar with Fixtures {
  self: PlaySpec =>

  val mockManagementAccountService: ManagementAccountService = mock[ManagementAccountService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockManagementAccountService)
  }

  def mockInsertNewManagementUser(inserted: Boolean): OngoingStubbing[Future[MongoCreateResponse]] = {
    when(mockManagementAccountService.insertNewManagementUser(ArgumentMatchers.any()))
      .thenReturn(Future(if(inserted) MongoSuccessCreate else MongoFailedCreate))
  }

  def mockGetManagementUser(fetched: Boolean): OngoingStubbing[Future[Account]] = {
    when(mockManagementAccountService.getManagementUser(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(if(fetched) Future(testManagementAccount) else Future.failed(new MissingAccountException("No account")))
  }

  def mockAuthenticateUser(authenticated: Boolean): OngoingStubbing[Future[Option[String]]] = {
    when(mockManagementAccountService.authenticateUser(ArgumentMatchers.any()))
      .thenReturn(Future(if(authenticated) Some(generateTestSystemId("management")) else None))
  }

  def mockUpdateEmail(updated: Boolean): OngoingStubbing[Future[MongoUpdatedResponse]] = {
    when(mockManagementAccountService.updateEmail(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(if(updated) MongoSuccessUpdate else MongoFailedUpdate))
  }

  def mockUpdatePassword(updated: Boolean): OngoingStubbing[Future[MongoUpdatedResponse]] = {
    when(mockManagementAccountService.updatePassword(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(if(updated) MongoSuccessUpdate else MongoFailedUpdate))
  }
}
