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

package com.github.fsanaulla.chronicler.urlhttp.management

import com.github.fsanaulla.chronicler.core.model.InfluxCredentials
import com.github.fsanaulla.chronicler.urlhttp.shared.InfluxConfig
import com.github.fsanaulla.chronicler.urlhttp.shared.implicits.{tryFunctor, urlFk}

object InfluxMng {

  /** Retrieve InfluxDB management client, without IO functionality
    *
    * @param host        - hostname
    * @param port        - port value
    * @param credentials - user credentials
    * @return            - UrlManagementClient
    */
  def apply(
      host: String,
      port: Int = 8086,
      credentials: Option[InfluxCredentials] = None
  ): UrlManagementClient =
    new UrlManagementClient(host, port, credentials)

  /** Retrieve management InfluxDB client, without IO functionality using [[InfluxConfig]]
    *
    * @param conf - configuration object
    * @return     - [[UrlManagementClient]]
    */
  def apply(conf: InfluxConfig): UrlManagementClient =
    apply(conf.host, conf.port, conf.credentials)
}
