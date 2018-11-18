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

package helpers.services

import com.kenshoo.play.metrics.Metrics
import org.scalatest.mockito.MockitoSugar
import services.MetricsService

trait MockMetricsService extends MockitoSugar {

  private val mockMetrics: Metrics = mock[Metrics]

  val mockMetricsService: MetricsService = new MetricsService {
    override val metrics: Metrics = mockMetrics
    override val enabled: Boolean = false
  }

}
