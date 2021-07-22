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

package com.github.fsanaulla.chronicler.udp

/** Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 14.03.18
  */
object InfluxUdp {

  /** *
    * Constructor for creating UDP client
    *
    * @param host - InfluxDB host value
    * @param port - InfluxDB port value
    *
    * @return     - [[InfluxUDPClient]]
    */
  def apply(host: String, port: Int = 8089): InfluxUDPClient =
    new InfluxUDPClient(host, port)
}
