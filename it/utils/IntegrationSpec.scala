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

package utils

import akka.util.Timeout
import com.cjwwdev.http.headers.HeaderPackage
import com.cjwwdev.http.headers.HeaderPackage._
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.testing.integration.IntegrationTestSpec
import com.cjwwdev.testing.integration.application.IntegrationApplication
import com.cjwwdev.testing.integration.wiremock.WireMockSetup
import org.joda.time.LocalDateTime
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import repositories._

import scala.concurrent.Future
import scala.concurrent.duration._

trait IntegrationSpec
  extends IntegrationTestSpec
    with IntegrationApplication
    with Fixtures
    with TestDataGenerator
    with WireMockSetup {

  trait ApiTest {
    def result: Future[WSResponse]
  }

  override implicit def defaultAwaitTimeout: Timeout = 5.seconds

  override val appConfig = Map(
    "repositories.DefaultManagementAccountRepository.collection" -> "it-management-accounts",
    "microservice.allowedApps"                                   -> "SNuIUBkrTWDoXUis-W1k8RBMSSMg_nDXTW07Gpi9877j_UnRfEB4HK1nMdSha54DlyjxVsWH2Wiqkc3UMyIFT8Hz9LgbigxpL5BMk9Vd5Y0",
    "root.username"                                              -> "cm9vdA==",
    "root.email"                                                 -> "dGVzdEB0ZXN0LmNvbQ==",
    "root.password"                                              -> "dGVzdGluZzEyMw==",
    "repositories.DefaultManagementAccountRepository.database"   -> "test-accounts"
  )

  override val currentAppBaseUrl = "administration"

  lazy val managementAccountRepository = app.injector.instanceOf[ManagementAccountRepository]

  val testCookieId = generateTestSystemId(SESSION)

  def client(url: String): WSRequest = ws.url(url).withHeaders(
    "cjww-headers" -> HeaderPackage("d6e3a79b-cb31-40a1-839a-530803d76156", Some(testCookieId)).encrypt,
    CONTENT_TYPE   -> TEXT
  )

  def testApiResponse(uri: String, method: String, status: Int, body: String): JsValue = Json.obj(
    "uri"    -> s"$uri",
    "method" -> s"$method",
    "status" -> status,
    "body"   -> s"$body",
    "stats"  -> Json.obj(
      "requestCompletedAt" -> s"${LocalDateTime.now}"
    )
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWm()
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    await(managementAccountRepository.collection.flatMap(_.drop(failIfNotFound = false)))
    startWm()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    await(managementAccountRepository.collection.flatMap(_.drop(failIfNotFound = false)))
  }

  override def afterAll(): Unit = {
    super.afterAll()
    await(managementAccountRepository.collection.flatMap(_.drop(failIfNotFound = false)))
    stopWm()
  }
}
