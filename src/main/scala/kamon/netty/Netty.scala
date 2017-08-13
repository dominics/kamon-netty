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

package kamon.netty

import com.typesafe.config.Config
import io.netty.handler.codec.http.HttpRequest
import kamon.Kamon

object Netty {
  def generateHttpClientOperationName(request: HttpRequest):String = {
    request.getUri
  }


  loadConfiguration(Kamon.config())

  Kamon.onReconfigure((newConfig: Config) => Netty.loadConfiguration(newConfig))

  private def loadConfiguration(config: Config): Unit = synchronized {
    val nettyConfig = config.getConfig("kamon.netty")
    println("------------------------------->> " + nettyConfig)
  }
}
