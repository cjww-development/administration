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

package controllers

import com.cjwwdev.http.headers.HeaderPackage
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.implicits.ImplicitJsValues._
import com.cjwwdev.security.deobfuscation.DeObfuscation._
import com.cjwwdev.security.obfuscation.Obfuscation._
import helpers.controllers.ControllerSpec
import models.Account
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import services.{ManagementAccountService, ValidationService}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class AccountControllerSpec extends ControllerSpec {

  private val testController = new AccountController {
    override val appId: String                                        = "testAppId"
    override val managementAccountService: ManagementAccountService   = mockManagementAccountService
    override val validationService: ValidationService                 = mockValidationService
    override protected def controllerComponents: ControllerComponents = stubControllerComponents()
    override implicit val ec: ExecutionContext                        = global
  }

  def postRequest: FakeRequest[String] = FakeRequest()
    .withHeaders("cjww-headers" -> HeaderPackage("d6e3a79b-cb31-40a1-839a-530803d76156", None).encrypt)
    .withBody(Json.parse(
      s"""
         |{
         |   "username" : "testUserN",
         |   "email" : "test@email.com",
         |   "password" : "${"testPassword".sha512}",
         |   "permissions" : [
         |      "all"
         |   ]
         |}
      """.stripMargin
    ).encrypt)

  "createNewUser" should {
    "return a CREATED" in {
      mockIsUserNameInUse(inUse = false)
      mockIsEmailInUse(inUse = false)

      mockInsertNewManagementUser(inserted = true)

      assertFutureResult(testController.createNewUser()(postRequest)) { res =>
        status(res)                            mustBe CREATED
        contentAsJson(res).get[String]("body") mustBe "Account created"
      }
    }

    "return an INTERNAL SERVER ERROR" in {
      mockIsUserNameInUse(inUse = false)
      mockIsEmailInUse(inUse = false)

      mockInsertNewManagementUser(inserted = false)

      assertFutureResult(testController.createNewUser()(postRequest)) { res =>
        status(res)                                    mustBe INTERNAL_SERVER_ERROR
        contentAsJson(res).get[String]("errorMessage") mustBe "There was a problem creating the new account"
      }
    }

    "return a CONFLICT" when {
      "the email is in use" in {
        mockIsUserNameInUse(inUse = false)
        mockIsEmailInUse(inUse = true)

        assertFutureResult(testController.createNewUser()(postRequest)) { res =>
          status(res)                                    mustBe CONFLICT
          contentAsJson(res).get[String]("errorMessage") mustBe "Could not create new account; either the email or username is already in use"
        }
      }

      "the user name is in use" in {
        mockIsUserNameInUse(inUse = true)
        mockIsEmailInUse(inUse = false)

        assertFutureResult(testController.createNewUser()(postRequest)) { res =>
          status(res)                                    mustBe CONFLICT
          contentAsJson(res).get[String]("errorMessage") mustBe "Could not create new account; either the email or username is already in use"
        }
      }

      "both are in use" in {
        mockIsUserNameInUse(inUse = true)
        mockIsEmailInUse(inUse = true)

        assertFutureResult(testController.createNewUser()(postRequest)) { res =>
          status(res)                                    mustBe CONFLICT
          contentAsJson(res).get[String]("errorMessage") mustBe "Could not create new account; either the email or username is already in use"
        }
      }
    }
  }

  "authenticateUser" should {
    lazy val request = FakeRequest()
      .withHeaders("cjww-headers" -> HeaderPackage("d6e3a79b-cb31-40a1-839a-530803d76156", None).encrypt)
      .withBody(Json.parse(
        s"""
           |{
           |   "username" : "testUserName",
           |   "password" : "${"testPassword".sha512}"
           |}
        """.stripMargin
      ).encrypt)

    "return an Ok" in {
      mockAuthenticateUser(authenticated = true)

      assertFutureResult(testController.authenticateUser()(request)) { res =>
        status(res)                                     mustBe OK
        contentAsJson(res).\("body").as[String].decrypt[String] mustBe Left(s"management-$uuid")
      }
    }

    "return a Forbidden" in {
      mockAuthenticateUser(authenticated = false)

      assertFutureResult(testController.authenticateUser()(request)) { res =>
        status(res) mustBe FORBIDDEN
      }
    }
  }

  "getManagementUser" should {
    lazy val request = FakeRequest()
      .withHeaders("cjww-headers" -> HeaderPackage("d6e3a79b-cb31-40a1-839a-530803d76156", None).encrypt)
    "return an Ok" in {
      mockGetManagementUser(fetched = true)

      assertFutureResult(testController.getManagementUser("testManagementId")(request)) { res =>
        status(res)                                                      mustBe OK
        contentAsJson(res).\("body").as[String].decrypt[JsValue] mustBe Left(Json.parse(
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

    "return a NotFound" in {
      mockGetManagementUser(fetched = false)

      assertFutureResult(testController.getManagementUser("testManagementId")(request)) { res =>
        status(res) mustBe NOT_FOUND
      }
    }
  }

  "fetchAllManagementUsers" should {
    lazy val request = FakeRequest()
      .withHeaders("cjww-headers" -> HeaderPackage("d6e3a79b-cb31-40a1-839a-530803d76156", None).encrypt)

    "return an Ok" in {
      mockGetAllManagementUsers(populated = true)

      assertFutureResult(testController.fetchAllManagementUsers()(request)) { res =>
        status(res)                                             mustBe OK
        contentAsJson(res).\("body").as[String].decrypt[JsValue] mustBe Left(Json.arr(Json.toJson(testManagementAccount)(Account.outgoingAccountWrites)))
      }
    }

    "return a No content" in {
      mockGetAllManagementUsers(populated = false)

      assertFutureResult(testController.fetchAllManagementUsers()(request)) { res =>
        status(res) mustBe NO_CONTENT
      }
    }
  }

  "deleteManagementUser" should {
    lazy val request = FakeRequest()
      .withHeaders("cjww-headers" -> HeaderPackage("d6e3a79b-cb31-40a1-839a-530803d76156", None).encrypt)

    "return a NoContent" in {
      mockDeleteManagementUser(deleted = true)

      assertFutureResult(testController.deleteManagementUser(generateTestSystemId(MANAGEMENT))(request)) { res =>
        status(res) mustBe NO_CONTENT
      }
    }

    "return an Internal server error" in {
      mockDeleteManagementUser(deleted = false)

      assertFutureResult(testController.deleteManagementUser(generateTestSystemId(MANAGEMENT))(request)) { res =>
        status(res) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
