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

import helpers.services.ServiceSpec
import repositories.ManagementAccountRepository

import scala.concurrent.ExecutionContext.Implicits.global

class ValidationServiceSpec extends ServiceSpec {

  private val testService = new ValidationService {
    override val managementAccountRepository: ManagementAccountRepository = mockManagementAccountRepository
  }

  "isEmailInUse" should {
    "return true" when {
      "the email is in use" in {
        mockGetManagementUser(found = true)

        awaitAndAssert(testService.isEmailInUse("testEmail")) {
          _ mustBe true
        }
      }
    }

    "return false" when {
      "the email is not in use" in {
        mockGetManagementUser(found = false)

        awaitAndAssert(testService.isEmailInUse("testEmail")) {
          _ mustBe false
        }
      }
    }
  }

  "isUserNameInUse" should {
    "return true" when {
      "the user name is in use" in {
        mockGetManagementUser(found = true)

        awaitAndAssert(testService.isUserNameInUse("testUserName")) {
          _ mustBe true
        }
      }
    }

    "return false" when {
      "the user name is not in use" in {
        mockGetManagementUser(found = false)

        awaitAndAssert(testService.isUserNameInUse("testUserName")) {
          _ mustBe false
        }
      }
    }
  }
}
