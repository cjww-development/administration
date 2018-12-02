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

import com.cjwwdev.config.ConfigurationLoader
import com.codahale.metrics.Timer
import com.kenshoo.play.metrics.Metrics
import javax.inject.Inject

import scala.concurrent.{Future, ExecutionContext => ExC}

class DefaultMetricsService @Inject()(val config: ConfigurationLoader,
                                      val metrics: Metrics) extends MetricsService {
  override val enabled: Boolean = {
    config.get[Boolean]("metrics.enabled") &
      config.get[Boolean]("metrics.graphite.enabled")
  }
}

trait MetricsService {

  val metrics: Metrics

  val enabled: Boolean

  private def timer(timerId: String): Timer.Context = metrics.defaultRegistry.timer(s"$timerId-response-timer").time()

  def mongoQueryResponseTime[T](id: String)(f: => Future[T])(implicit ec: ExC): Future[T] = {
    if(enabled) {
      val time = timer(id)
      f map { data =>
        time.stop()
        data
      } recover { case e =>
        time.stop()
        throw e
      }
    } else {
      f
    }
  }

  def outboundCallResponseTime[T](id: String)(f: => Future[T])(implicit ec: ExC): Future[T] = {
    if(enabled) {
      val time = timer(id)
      f map { data =>
        time.stop()
        data
      } recover { case e =>
        time.stop()
        throw e
      }
    } else {
      f
    }
  }
}
