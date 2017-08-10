/*
 * =========================================================================================
 * Copyright © 2013-2017 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package kamon.netty.util

import java.util

import io.netty.channel.EventLoop
import kamon.netty.Metrics
import kamon.netty.Metrics.EventLoopMetrics
import kamon.netty.instrumentation.EventLoopUtils.name

class MonitoredQueue(eventLoop:EventLoop, underlying:util.Queue[Runnable]) extends QueueWrapperAdapter[Runnable](underlying) {

  implicit lazy val eventLoopMetrics: EventLoopMetrics = Metrics.forEventLoop(name(eventLoop))

  override def add(runnable: Runnable): Boolean = {
    eventLoopMetrics.taskQueueSize.increment()
    underlying.add(new TimedTask(runnable))
  }

  override def offer(runnable: Runnable): Boolean = {
    eventLoopMetrics.taskQueueSize.increment()
    underlying.offer(new TimedTask(runnable))
  }

  override def remove(): Runnable = {
    val runnable = underlying.remove()
    eventLoopMetrics.taskQueueSize.decrement()
    runnable
  }

  override def poll(): Runnable = {
    val runnable = underlying.poll()

    if(runnable != null) {
      eventLoopMetrics.taskQueueSize.decrement()
    }
    runnable
  }
}

object MonitoredQueue {
  def apply(eventLoop: EventLoop, underlying: util.Queue[Runnable]): MonitoredQueue =
    new MonitoredQueue(eventLoop, underlying)
}

private[this] class TimedTask(underlying:Runnable)(implicit metrics: EventLoopMetrics) extends Runnable {
  override def run(): Unit =
    Latency.measure(metrics.taskProcessingTime)(underlying.run())
}