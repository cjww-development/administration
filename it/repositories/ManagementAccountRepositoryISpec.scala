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

package repositories

import com.cjwwdev.mongo.responses.{MongoSuccessCreate, MongoSuccessUpdate}
import models.Account
import play.api.test.FakeRequest
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import selectors.AccountSelectors
import utils.IntegrationSpec

class ManagementAccountRepositoryISpec extends IntegrationSpec {

  implicit val req = FakeRequest()

  "insertManagementAccount" should {
    "successfully insert a new management account" in {
      awaitAndAssert(managementAccountRepository.insertManagementAccount(testManagementAccount)) {
        _ mustBe MongoSuccessCreate
      }

      val readResult = for {
        col <- managementAccountRepository.collection
        res <- col.find(BSONDocument("managementId" -> testManagementAccount.managementId)).one[Account]
      } yield res

      awaitAndAssert(readResult) {
        _ mustBe Some(testManagementAccount)
      }
    }
  }

  "getManagementUser" should {
    "fetch a management account" when {
      def buildTestSelector(key: String): BSONDocument = key match {
        case "managementId" => BSONDocument("managementId" -> testManagementAccount.managementId)
        case "username"     => BSONDocument("username"     -> testManagementAccount.username)
        case "email"        => BSONDocument("email"        -> testManagementAccount.email)
      }

      "queried by managementId" in {
        await(managementAccountRepository.collection.flatMap(_.insert(testManagementAccount)))

        awaitAndAssert(managementAccountRepository.getManagementUser(buildTestSelector("managementId"))) {
          _ mustBe Some(testManagementAccount)
        }
      }

      "queried by user name" in {
        await(managementAccountRepository.collection.flatMap(_.insert(testManagementAccount)))

        awaitAndAssert(managementAccountRepository.getManagementUser(buildTestSelector("username"))) {
          _ mustBe Some(testManagementAccount)
        }
      }

      "queried by email address" in {
        await(managementAccountRepository.collection.flatMap(_.insert(testManagementAccount)))

        awaitAndAssert(managementAccountRepository.getManagementUser(buildTestSelector("email"))) {
          _ mustBe Some(testManagementAccount)
        }
      }
    }

    "fetch no management account" when {
      "no matching account could be found" in {
        awaitAndAssert(managementAccountRepository.getManagementUser(BSONDocument("managementId" -> "invalid"))) {
          _ mustBe None
        }
      }
    }
  }

  "updateEmail" should {
    "return a MongoSuccessUpdate" in {
      await(managementAccountRepository.collection.flatMap(_.insert(testManagementAccount)))

      awaitAndAssert(managementAccountRepository.updateEmail(testManagementAccount.managementId, "testUpdatedEmail")) {
        _ mustBe MongoSuccessUpdate
      }

      val readResult = for {
        col <- managementAccountRepository.collection
        res <- col.find(AccountSelectors.managementIdSelector(testManagementAccount.managementId)).one[Account]
      } yield res.get

      awaitAndAssert(readResult) { acc =>
        assert(acc.email != testManagementAccount.email)
        assert(acc.email == "testUpdatedEmail")
      }
    }
  }

  "updatePassword" should {
    "return a MongoSuccessUpdate" when {
      "the account exists and the password has been updated" in {
        await(managementAccountRepository.collection.flatMap(_.insert(testManagementAccount)))

        awaitAndAssert(managementAccountRepository.updatePassword(testManagementAccount.managementId, testManagementAccount.password, "testPasswordUpdated")) {
          _ mustBe MongoSuccessUpdate
        }

        val readResult = for {
          col <- managementAccountRepository.collection
          res <- col.find(AccountSelectors.managementIdSelector(testManagementAccount.managementId)).one[Account]
        } yield res.get

        awaitAndAssert(readResult) { acc =>
          assert(acc.password != testManagementAccount.password)
          assert(acc.password == "testPasswordUpdated")
        }
      }
    }
  }
}
