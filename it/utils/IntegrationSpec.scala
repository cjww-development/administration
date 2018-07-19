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
    "microservice.allowedApps"                                   -> "EjeNVhx_3N3Lr_VFJZdVWc47rWweHqRu4DBwFeVoStiN8X54rQ84JG4nJIgxaxynkrU59UfxZ9xAREaihbzNEm1dNd0O_YnX8q7kjqL6vfE",
    "root.user"                                                  -> "hPYSDFsyQE5AfCe4ANk9D94MYsUJTvPU-E4kcV_19Rg0OZ0dUuDo5FcOlPMfKJEvTjDBSDlEaW1HSjS6xnVGu2GbYSLLgLWRWyjFii_fncYqAklrZo9SdxacYzy9t5NNtt4QkWA5zZ0_GvbvMlrs-ANByn14mb3H5VLpPGAPRJN9LMB82FWBZzciXvz57-2GIDbFoueUCpDXc7y0r5vnGPf_P7DBVZ--bV3qNRwm0_oE77zYu9ih7Hom9exbn7gcnDfalI5tji98f6QFQCknN2hFXeD-scWZJyad-VJPyhf9bgJUse4FpWN7OCZTEv591oLue38FA-PF3AWC_KyM_49kE-pH4ZtdBP4QhEgk8OUlNDiObaNwOi6tr0VkEDsq",
    "repositories.DefaultManagementAccountRepository.database"   -> "test-accounts"
  )

  override val currentAppBaseUrl = "administration"

  lazy val managementAccountRepository = app.injector.instanceOf[ManagementAccountRepository]

  val testCookieId = generateTestSystemId(SESSION)

  def client(url: String): WSRequest = ws.url(url).withHeaders(
    "cjww-headers" -> HeaderPackage("d6e3a79b-cb31-40a1-839a-530803d76156", testCookieId).encryptType,
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

  private def afterITest(): Unit = {
    managementAccountRepository.collection.flatMap(_.drop(failIfNotFound = false))
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWm()
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWm()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    afterITest()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    afterITest()
    stopWm()
  }
}
