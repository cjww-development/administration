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

package api

import com.cjwwdev.implicits.ImplicitDataSecurity._
import models.Account
import play.api.libs.ws.WSResponse
import play.api.test.FakeRequest
import repositories.ManagementAccountRepository
import utils.IntegrationSpec

import scala.concurrent.Future

class DeleteUserISpec extends IntegrationSpec {

  lazy val repo = app.injector.instanceOf[ManagementAccountRepository]

  implicit val req = FakeRequest()

  "DELETE /users/:managementId" should {
    "return a No content" when {
      "the specified user has been deleted" in new ApiTest {
        override def result: Future[WSResponse] = client(s"$testAppUrl/user/management-a6e9c2dd-98b9-4635-895a-c59d78048682").delete()

        await(repo.insertManagementAccount(Account(
          managementId = "management-a6e9c2dd-98b9-4635-895a-c59d78048682",
          username     = "testuser",
          email        = "test@email.com",
          password     = "testPassword".sha512,
          permissions  = List("all")
        )))

        awaitAndAssert(result) { res =>
          statusOf(res) mustBe NO_CONTENT
        }
      }
    }
  }
}
