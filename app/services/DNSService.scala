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

import com.cjwwdev.logging.Logging
import connectors.DNSConnector
import javax.inject.Inject

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class DefaultDNSService @Inject()(val dnsConnector: DNSConnector) extends DNSService

trait DNSService extends Logging {

  protected val dnsConnector: DNSConnector

  private val IP_KEY = "public-ip"

  def updatePublicIP(token: String): Future[String] = {
    dnsConnector.getPublicIPAddress flatMap { ip =>
      if(ip == getIP) {
        logger.warn("[updatePublicIP] - The fetched IP hasn't changed; update aborted this time")
        Future(ip)
      } else {
        dnsConnector.updateFreeDNS(token) map { _ =>
          logger.info("[updatePublicIP] - The IP address has changed, updating dynamic dns provider")
          setIP(ip)
        }
      }
    }
  }

  private def getIP: String = System.getProperty(IP_KEY, "8.8.8.8")

  private def setIP(ip: String): String = {
    System.setProperty(IP_KEY, ip)
    ip
  }
}
