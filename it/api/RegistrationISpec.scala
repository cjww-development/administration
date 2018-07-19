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
import play.api.libs.ws.WSResponse
import repositories.ManagementAccountRepository
import utils.IntegrationSpec

import scala.concurrent.Future

class RegistrationISpec extends IntegrationSpec {

  lazy val repo = app.injector.instanceOf[ManagementAccountRepository]

  "/administration/register" should {
    "return a Created" when {
      "the user has been registered" in new ApiTest {
        override def result: Future[WSResponse] = client(s"$testAppUrl/register")
          .post(
            s"""
              |{
              |   "username" : "testUserN",
              |   "email" : "test@email.com",
              |   "password" : "${SHA512.encrypt("testPassword")}",
              |   "permissions" : [
              |       "all"
              |   ]
              |}
            """.stripMargin.encrypt
          )

        awaitAndAssert(result) {
          _.status mustBe CREATED
        }
      }
    }

    "return a Conflict" when {
      "the email address is already in use" in new ApiTest {
        override def result: Future[WSResponse] = client(s"$testAppUrl/register").post(
          s"""
            |{
            |   "username" : "testUserN",
            |   "email" : "test@email.com",
            |   "password" : "${SHA512.encrypt("testPassword")}",
            |   "permissions" : [
            |       "all"
            |   ]
            |}
          """.stripMargin.encrypt
        )

        await(repo.insertManagementAccount(Account("testId", "testUserN", "test@email.com", s"${SHA512.encrypt("testPassword")}", List("all"))))

        awaitAndAssert(result) {
          _.status mustBe CONFLICT
        }
      }

      "the user name is already in use" in new ApiTest {
        override def result: Future[WSResponse] = client(s"$testAppUrl/register").post(
          s"""
            |{
            |   "username" : "testUserN",
            |   "email" : "test@email.com",
            |   "password" : "${SHA512.encrypt("testPassword")}",
            |   "permissions" : [
            |       "all"
            |   ]
            |}
          """.stripMargin.encrypt
        )

        await(repo.insertManagementAccount(Account("testId", "testUserN", "test1@email.com", s"${SHA512.encrypt("testPassword")}", List("all"))))

        awaitAndAssert(result) {
          _.status mustBe CONFLICT
        }
      }

      "both the user name and email are in use" in new ApiTest {
        override def result: Future[WSResponse] = client(s"$testAppUrl/register").post(
          s"""
            |{
            |   "username" : "testUserN",
            |   "email" : "test@email.com",
            |   "password" : "${SHA512.encrypt("testPassword")}",
            |   "permissions" : [
            |       "all"
            |   ]
            |}
          """.stripMargin.encrypt
        )

        await(repo.insertManagementAccount(Account("testId", "testUserN", "test@email.com", s"${SHA512.encrypt("testPassword")}", List("all"))))

        awaitAndAssert(result) {
          _.status mustBe CONFLICT
        }
      }
    }
  }
}
