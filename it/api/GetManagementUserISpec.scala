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
import com.cjwwdev.security.deobfuscation.DeObfuscation._
import models.Account
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import repositories.ManagementAccountRepository
import utils.IntegrationSpec

import scala.concurrent.Future

class GetManagementUserISpec extends IntegrationSpec {

  lazy val repo = app.injector.instanceOf[ManagementAccountRepository]

  "/user/:managementId" should {
    "return an Ok" when {
      "a matching user has been found" in new ApiTest {
        await(repo.insertManagementAccount(testManagementAccount))

        override def result: Future[WSResponse] = client(s"$testAppUrl/user/${testManagementAccount.managementId}").get()

        awaitAndAssert(result) { res =>
          statusOf(res)                                                  mustBe OK
          bodyAsJson[JsValue](res).\("body").as[String].decrypt[JsValue] mustBe Left(Json.parse(
            s"""
              |{
              |   "managementId" : "${testManagementAccount.managementId}",
              |   "username" : "${testManagementAccount.username}",
              |   "email" : "${testManagementAccount.email}",
              |   "permissions" : [
              |       "all"
              |   ]
              |}
            """.stripMargin
          ))
        }
      }
    }

    "return a NotFound" when {
      "no matching user could be found" in new ApiTest {
        override def result: Future[WSResponse] = client(s"$testAppUrl/user/${generateTestSystemId("managementId")}").get()

        awaitAndAssert(result) { res =>
          statusOf(res) mustBe NOT_FOUND
        }
      }
    }
  }

  "/users" should {
    "return an Ok" when {
      "a set of users have been found" in new ApiTest {
        override def result: Future[WSResponse] = client(s"$testAppUrl/users").get()

        await(managementAccountRepository.insertManagementAccount(testManagementAccount))

        awaitAndAssert(result) { res =>
          statusOf(res)                                                   mustBe OK
          jsonContent[JsValue](res).\("body").as[String].decrypt[JsValue] mustBe Left(Json.parse(
            s"""
              |[
              |   ${Json.prettyPrint(Json.toJson(testManagementAccount)(Account.outgoingAccountWrites))}
              |]
            """.stripMargin
          ))
        }
      }
    }

    "return a NoContent" when {
      "no users have been found" in new ApiTest {
        override def result: Future[WSResponse] = client(s"$testAppUrl/users").get()

        await(repo.collection.flatMap(_.drop(failIfNotFound = false)))

        awaitAndAssert(result) { res =>
          statusOf(res) mustBe NO_CONTENT
        }
      }
    }
  }
}
