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

import com.cjwwdev.mongo.responses._
import common.MissingAccountException
import helpers.services.ServiceSpec
import models.Credentials
import repositories.ManagementAccountRepository

import scala.concurrent.ExecutionContext.Implicits.global

class ManagementAccountServiceSpec extends ServiceSpec {

  private val testService = new ManagementAccountService {
    override val managementAccountRepository: ManagementAccountRepository = mockManagementAccountRepository
  }

  "insertNewManagementUser" should {
    "return a MongoSuccessCreate" in {
      mockInsertManagementAccount(success = true)

      awaitAndAssert(testService.insertNewManagementUser(testManagementAccount)) {
        _ mustBe MongoSuccessCreate
      }
    }

    "return a MongoFailedCreate" in {
      mockInsertManagementAccount(success = false)

      awaitAndAssert(testService.insertNewManagementUser(testManagementAccount)) {
        _ mustBe MongoFailedCreate
      }
    }
  }

  "getManagementUser" should {
    "return an account" when {
      "querying by managementId" in {
        mockGetManagementUser(found = true)

        awaitAndAssert(testService.getManagementUser("managementId", generateTestSystemId(MANAGEMENT))) {
          _ mustBe testManagementAccount
        }
      }

      "querying by username" in {
        mockGetManagementUser(found = true)

        awaitAndAssert(testService.getManagementUser("username", generateTestSystemId(MANAGEMENT))) {
          _ mustBe testManagementAccount
        }
      }

      "querying by email" in {
        mockGetManagementUser(found = true)

        awaitAndAssert(testService.getManagementUser("email", generateTestSystemId(MANAGEMENT))) {
          _ mustBe testManagementAccount
        }
      }
    }

    "throw an IllegalStateException" when {
      "providing an invalid query key" in {
        intercept[IllegalStateException](await(testService.getManagementUser("invalidKey", generateTestSystemId(MANAGEMENT))))
      }
    }

    "throw a missing account exception" in {
      mockGetManagementUser(found = false)

      intercept[MissingAccountException](await(testService.getManagementUser("managementId", generateTestSystemId(MANAGEMENT))))
    }
  }

  "getAllManagementUsers" should {
    "return a populated list of users" in {
      mockGetAllManagementUsers(populated = true)

      awaitAndAssert(testService.getAllManagementUsers) { res =>
        assert(res.nonEmpty)
      }
    }

    "return an empty list" in {
      mockGetAllManagementUsers(populated = false)

      awaitAndAssert(testService.getAllManagementUsers) { res =>
        assert(res.isEmpty)
      }
    }
  }

  "authenticateUser" should {
    "return a managementId" when {
      "the user could be authenticated" in {
        mockGetManagementUser(found = true)

        awaitAndAssert(testService.authenticateUser(Credentials(username = "testUserName", password = "testPassword"))) {
          _ mustBe Some(testManagementAccount.managementId)
        }
      }
    }

    "return no id" when {
      "no matching user could be found" in {
        mockGetManagementUser(found = false)

        awaitAndAssert(testService.authenticateUser(Credentials(username = "testUserName", password = "testPassword"))) {
          _ mustBe None
        }
      }
    }
  }

  "updateEmail" should {
    "return a MongoSuccessUpdate" in {
      mockUpdateEmail(updated = true)

      awaitAndAssert(testService.updateEmail(generateTestSystemId(MANAGEMENT), "testEmail")) {
        _ mustBe MongoSuccessUpdate
      }
    }

    "return a MongoFailedUpdate" in {
      mockUpdateEmail(updated = false)

      awaitAndAssert(testService.updateEmail(generateTestSystemId(MANAGEMENT), "testEmail")) {
        _ mustBe MongoFailedUpdate
      }
    }
  }

  "updatePassword" should {
    "return a MongoSuccessUpdate" in {
      mockGetManagementUser(found = true)

      mockUpdatePassword(updated = true)

      awaitAndAssert(testService.updatePassword(generateTestSystemId(MANAGEMENT), testManagementAccount.password, "testUpdatedPassword")) {
        _ mustBe MongoSuccessUpdate
      }
    }

    "return a MongoFailedUpdate" in {
      mockGetManagementUser(found = true)

      mockUpdatePassword(updated = false)

      awaitAndAssert(testService.updatePassword(generateTestSystemId(MANAGEMENT), testManagementAccount.password, "testUpdatedPassword")) {
        _ mustBe MongoFailedUpdate
      }
    }

    "throw a missing account exception" in {
      mockGetManagementUser(found = false)

      intercept[MissingAccountException](await(testService.updatePassword(
        generateTestSystemId(MANAGEMENT),
        testManagementAccount.password,
        "testUpdatedPassword"
      )))
    }
  }

  "deleteManagementUser" should {
    "return a MongoSuccessDelete" in {
      mockDeleteManagementUser(deleted = true)

      awaitAndAssert(testService.deleteUser(generateTestSystemId(MANAGEMENT))) {
        _ mustBe MongoSuccessDelete
      }
    }

    "return a MongoFailedDelete" in {
      mockDeleteManagementUser(deleted = false)

      awaitAndAssert(testService.deleteUser(generateTestSystemId(MANAGEMENT))) {
        _ mustBe MongoFailedDelete
      }
    }
  }
}
