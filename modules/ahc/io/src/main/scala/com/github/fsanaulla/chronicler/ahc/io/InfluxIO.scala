/*
 * Copyright 2017-2019 Faiaz Sanaulla
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

package com.github.fsanaulla.chronicler.ahc.io

import com.github.fsanaulla.chronicler.ahc.shared.InfluxConfig
import com.github.fsanaulla.chronicler.core.model.InfluxCredentials
import org.asynchttpclient.AsyncHttpClientConfig

import scala.concurrent.ExecutionContext

object InfluxIO {

  /** Retrieve IO InfluxDB client, without management functionality
    *
    * @param host              - hostname
    * @param port              - port value
    * @param credentials       - user credentials
    * @param compress           - enable gzip compression
    * @param asyncClientConfig - custom configuration
    * @param ec                - implicit execution context, by default use standard one
    * @return                  - [[AhcIOClient]]
    */
  def apply(
      host: String,
      port: Int = 8086,
      credentials: Option[InfluxCredentials] = None,
      compress: Boolean = false,
      asyncClientConfig: Option[AsyncHttpClientConfig] = None
  )(implicit ec: ExecutionContext): AhcIOClient =
    new AhcIOClient(host, port, compress, credentials, asyncClientConfig)

  /** Retrieve IO InfluxDB client, without management functionality using configuration object
    *
    * @param conf - configuration object
    * @param ex   - implicit execution context, by default use standard one
    * @return     - [[AhcIOClient]]
    */
  def apply(conf: InfluxConfig)(implicit ex: ExecutionContext): AhcIOClient =
    apply(conf.host, conf.port, conf.credentials, conf.compress, conf.asyncClientConfig)
}
