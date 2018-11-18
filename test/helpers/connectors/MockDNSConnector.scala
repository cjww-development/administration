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

package helpers.connectors

import connectors.DNSConnector
import helpers.other.Fixtures
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockDNSConnector extends BeforeAndAfterEach with MockitoSugar with Fixtures {
  self: PlaySpec =>

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDNSConnector)
  }

  val mockDNSConnector = mock[DNSConnector]

  def mockGetPublicIPAddress(ip: String): OngoingStubbing[Future[String]] = {
    when(mockDNSConnector.getPublicIPAddress(any()))
      .thenReturn(Future(ip))
  }

  def mockUpdateFreeDNS: OngoingStubbing[Future[WSResponse]] = {
    when(mockDNSConnector.updateFreeDNS(any())(any()))
      .thenReturn(Future(fakeWSResponse))
  }

  val fakeWSResponse: WSResponse = new WSResponse {
    override def status = ???
    override def statusText = ???
    override def headers = ???
    override def underlying[T] = ???
    override def cookies = ???
    override def cookie(name: String) = ???
    override def body = ???
    override def bodyAsBytes = ???
    override def bodyAsSource = ???
    override def allHeaders = ???
    override def xml = ???
    override def json = ???
  }
}
