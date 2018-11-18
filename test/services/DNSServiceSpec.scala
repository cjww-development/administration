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

import scala.concurrent.ExecutionContext.Implicits.global

class DNSServiceSpec extends ServiceSpec {

  val testService = new DNSService {
    override protected val dnsConnector = mockDNSConnector
  }

  "updatePublicIP" should {
    "return the fetched IP" when {
      "the IP address hasn't changed" in {
        System.setProperty("public-ip", "1.2.3.4")

        mockGetPublicIPAddress(ip = "1.2.3.4")

        awaitAndAssert(testService.updatePublicIP("testToken")) {
          _ mustBe "1.2.3.4"
        }
      }

      "the IP address has changed" in {
        System.setProperty("public-ip", "0.1.2.3")

        mockGetPublicIPAddress(ip = "1.2.3.4")
        mockUpdateFreeDNS

        awaitAndAssert(testService.updatePublicIP("testToken")) {
          _ mustBe "1.2.3.4"
        }
      }
    }
  }
}
