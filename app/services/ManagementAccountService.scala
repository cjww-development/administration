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

package services

import com.cjwwdev.mongo.responses.{MongoCreateResponse, MongoDeleteResponse, MongoUpdatedResponse}
import common.MissingAccountException
import javax.inject.Inject

import models.{Account, Credentials}
import repositories.ManagementAccountRepository
import selectors.AccountSelectors

import scala.concurrent.{ExecutionContext => ExC, Future}

class DefaultManagementAccountService @Inject()(val managementAccountRepository: ManagementAccountRepository) extends ManagementAccountService

trait ManagementAccountService {

  val managementAccountRepository: ManagementAccountRepository

  def insertNewManagementUser(account: Account)(implicit ec: ExC): Future[MongoCreateResponse] = {
    managementAccountRepository.insertManagementAccount(account)
  }

  def getManagementUser(key: String, value: String)(implicit ec: ExC): Future[Account] = {
    val selector = key match {
      case "managementId" => AccountSelectors.managementIdSelector(value)
      case "username"     => AccountSelectors.userNameSelector(value)
      case "email"        => AccountSelectors.emailSelector(value)
      case _              => throw new IllegalStateException("Invalid query key [getManagementUser]")
    }

    managementAccountRepository.getManagementUser(selector) map {
      _.getOrElse(throw new MissingAccountException(s"No account found based on key $key with value $value"))
    }
  }

  def getAllManagementUsers(implicit ec: ExC): Future[List[Account]] = {
    managementAccountRepository.getAllManagementUsers
  }

  def authenticateUser(credentials: Credentials)(implicit ec: ExC): Future[Option[String]] = {
    managementAccountRepository.getManagementUser(AccountSelectors.loginSelector(credentials)) map {
      _.map(_.managementId)
    }
  }

  def updateEmail(managementId: String, email: String)(implicit ec: ExC): Future[MongoUpdatedResponse] = {
    managementAccountRepository.updateEmail(managementId, email)
  }

  def updatePassword(managementId: String, oldPassword: String, newPassword: String)(implicit ec: ExC): Future[MongoUpdatedResponse] = {
    for {
      acc <- managementAccountRepository.getManagementUser(AccountSelectors.passwordSelector(managementId, oldPassword)) map {
        _.getOrElse(throw new MissingAccountException(s"No account found for managementId $managementId"))
      }
      mur <- managementAccountRepository.updatePassword(acc.managementId, oldPassword, newPassword)
    } yield mur
  }

  def deleteUser(managementId: String)(implicit ec: ExC): Future[MongoDeleteResponse] = {
    managementAccountRepository.deleteManagementUser(managementId)
  }
}
