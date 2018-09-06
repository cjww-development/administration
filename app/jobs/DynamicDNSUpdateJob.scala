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

package jobs

import akka.actor.ActorSystem
import com.cjwwdev.logging.Logging
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.scheduling.{JobComplete, JobCompletionStatus, JobFailed, ScheduledJob}
import javax.inject.Inject
import play.api.Configuration
import services.DNSService

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class DynamicDNSUpdateJob @Inject()(val actorSystem: ActorSystem,
                                    val config: Configuration,
                                    val dnsService: DNSService,
                                    val executionContext: ExecutionContext) extends ScheduledJob with Logging {
  override val jobName  = "dns-update"
  override val enabled  = config.get[Boolean](s"jobs.$jobName.enabled")
  override val interval = config.get[Long](s"jobs.$jobName.interval")

  private val apiToken = config.getOptional[String]("dns.freedns-api.token")

  override def scheduledJob: Future[JobCompletionStatus] = {
    apiToken match {
      case Some(token) => dnsService.updatePublicIP(token) map { ip =>
        logger.info(s"Dynamic DNS has been updated ${ip.encrypt}")
        JobComplete
      }
      case None        =>
        logger.warn("No DNS API token found; updating DNS entry aborted")
        Future(JobFailed)
    }
  }

  if(enabled) {
    actorSystem.scheduler.schedule(initialDelay = 0.seconds, interval = interval.second)(scheduledJob)
  } else {
    Future(JobFailed)
  }
}
