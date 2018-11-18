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

import java.util.{Base64, UUID}

import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.security.sha.SHA512
import javax.inject.Inject
import models.Account
import repositories.ManagementAccountRepository
import selectors.AccountSelectors

import scala.concurrent.{Await, ExecutionContext => ExC, Future}
import scala.concurrent.duration._

class DefaultRootUser @Inject()(val managementAccountRepository: ManagementAccountRepository,
                                val config: ConfigurationLoader,
                                implicit val ec: ExC) extends RootUser {
  override val userName = new String(Base64.getDecoder.decode(config.get[String]("root.username")), "UTF-8")
  override val email    = new String(Base64.getDecoder.decode(config.get[String]("root.email")),    "UTF-8")
  override val password = new String(Base64.getDecoder.decode(config.get[String]("root.password")), "UTF-8")
  setupRootUser()
}

trait RootUser {
  val managementAccountRepository: ManagementAccountRepository

  val userName: String
  val email: String
  val password: String

  private def rootAccount: Account = Account(
    managementId = s"management-${UUID.randomUUID()}",
    username     = userName,
    email        = email,
    password     = SHA512.encrypt(password),
    permissions  = List("all")
  )

  private def await[T](future: Future[T]): T = Await.result(future, 10.seconds)

  def setupRootUser()(implicit ec: ExC): Boolean = {
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

