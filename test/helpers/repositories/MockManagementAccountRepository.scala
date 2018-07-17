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

package helpers.repositories

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
import repositories.ManagementAccountRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockManagementAccountRepository extends BeforeAndAfterEach with MockitoSugar with Fixtures {
  self: PlaySpec =>

  protected val mockManagementAccountRepository: ManagementAccountRepository = mock[ManagementAccountRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockManagementAccountRepository)
  }

  def mockInsertManagementAccount(success: Boolean): OngoingStubbing[Future[MongoCreateResponse]] = {
    when(mockManagementAccountRepository.insertManagementAccount(ArgumentMatchers.any()))
      .thenReturn(Future(if(success) MongoSuccessCreate else MongoFailedCreate))
  }

  def mockGetManagementUser(found: Boolean): OngoingStubbing[Future[Option[Account]]] = {
    when(mockManagementAccountRepository.getManagementUser(ArgumentMatchers.any()))
      .thenReturn(Future(if(found) Some(testManagementAccount) else None))
  }

  def mockFailedGetManagementUser: OngoingStubbing[Future[Option[Account]]] = {
    when(mockManagementAccountRepository.getManagementUser(ArgumentMatchers.any()))
      .thenReturn(Future.failed(new MissingAccountException("No account found")))
  }

  def mockUpdateEmail(updated: Boolean): OngoingStubbing[Future[MongoUpdatedResponse]] = {
    when(mockManagementAccountRepository.updateEmail(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(if(updated) MongoSuccessUpdate else MongoFailedUpdate))
  }

  def mockUpdatePassword(updated: Boolean): OngoingStubbing[Future[MongoUpdatedResponse]] = {
    when(mockManagementAccountRepository.updatePassword(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(if(updated) MongoSuccessUpdate else MongoFailedUpdate))
  }
}
