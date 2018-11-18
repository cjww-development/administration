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

import javax.inject.Inject
import repositories.ManagementAccountRepository
import selectors.AccountSelectors

import scala.concurrent.{ExecutionContext => ExC, Future}

class DefaultValidationService @Inject()(val managementAccountRepository: ManagementAccountRepository) extends ValidationService

trait ValidationService {
  val managementAccountRepository: ManagementAccountRepository

  def isEmailInUse(email: String)(implicit ec: ExC): Future[Boolean] = {
    managementAccountRepository.getManagementUser(AccountSelectors.emailSelector(email)) map {
      _.isDefined
    }
  }

  def isUserNameInUse(username: String)(implicit ec: ExC): Future[Boolean] = {
    managementAccountRepository.getManagementUser(AccountSelectors.userNameSelector(username)) map {
      _.isDefined
    }
  }
}