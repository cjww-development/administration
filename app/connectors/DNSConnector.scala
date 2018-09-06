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

package connectors

import com.cjwwdev.logging.Logging
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class DefaultDNSConnector @Inject()(val wsClient: WSClient,
                                    val config: Configuration) extends DNSConnector {
  override protected val updateURL = config.get[String]("dns.freedns-api.url")
  override protected val ipfyUrl   = config.get[String]("dns.ipfy.url")
}

trait DNSConnector extends Logging {

  protected val wsClient: WSClient

  protected val updateURL: String
  protected val ipfyUrl: String

  def getPublicIPAddress: Future[String] = {
    wsClient.url(ipfyUrl).get() map {
      _.json.\("ip").as[String]
    }
  }

  def updateFreeDNS(token: String): Future[WSResponse] = {
    wsClient.url(s"$updateURL$token").get()
  }
}
