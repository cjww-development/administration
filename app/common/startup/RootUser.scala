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

package common.startup

import com.cjwwdev.config.ConfigurationLoader
import javax.inject.Inject
import com.cjwwdev.implicits.ImplicitDataSecurity._
import models.Account
import repositories.ManagementAccountRepository
import selectors.AccountSelectors

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class DefaultRootUser @Inject()(val managementAccountRepository: ManagementAccountRepository,
                                val config: ConfigurationLoader) extends RootUser {
  override val rootAccount: Account = config.loadedConfig.get[String]("root.user").decryptIntoType[Account]
  setupRootUser()
}

trait RootUser {
  val managementAccountRepository: ManagementAccountRepository

  val rootAccount: Account

  private def await[T](future: Future[T]): T = Await.result(future, 10.seconds)

  def setupRootUser(): Boolean = {
    await(for {
      existingUser <- managementAccountRepository.getManagementUser(AccountSelectors.rootSelector(rootAccount))
      insertUser   <- if(existingUser.isEmpty) {
        managementAccountRepository.insertManagementAccount(rootAccount) map(_ => true)
      } else {
        Future(false)
      }
    } yield insertUser)
  }
}

