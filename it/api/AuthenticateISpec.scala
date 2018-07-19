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
import com.cjwwdev.security.encryption.SHA512
import models.Account
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import repositories.ManagementAccountRepository
import utils.IntegrationSpec

import scala.concurrent.Future

class AuthenticateISpec extends IntegrationSpec {

  lazy val repo = app.injector.instanceOf[ManagementAccountRepository]

  "/authenticate" should {
    "return an Ok with the users management id in the body" in new ApiTest {
      await(repo.insertManagementAccount(Account(
        managementId = "management-a6e9c2dd-98b9-4635-895a-c59d78048682",
        username     = "testuser",
        email        = "test@email.com",
        password     = SHA512.encrypt("testPassword"),
        permissions  = List("all")
      )))

      override def result: Future[WSResponse] = client(s"$testAppUrl/authenticate")
        .post(
          s"""
            |{
            |   "username" : "testuser",
            |   "password" : "${SHA512.encrypt("testPassword")}"
            |}
          """.stripMargin.encrypt
        )

        awaitAndAssert(result) { res =>
          statusOf(res)                                         mustBe OK
          bodyAsJson[JsValue](res).\("body").as[String].decrypt mustBe "management-a6e9c2dd-98b9-4635-895a-c59d78048682"
        }
    }

    "return a Forbidden" in new ApiTest {
      override def result: Future[WSResponse] = client(s"$testAppUrl/authenticate")
        .post(
          s"""
             |{
             |   "username" : "testuser2",
             |   "password" : "${SHA512.encrypt("testPassword")}"
             |}
          """.stripMargin.encrypt
        )

      awaitAndAssert(result) { res =>
        statusOf(res) mustBe FORBIDDEN
      }
    }
  }
}
