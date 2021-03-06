/*
 *  Copyright 2015 PayPal
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.squbs.httpclient.dummy

import org.squbs.httpclient.env._

object DummyProdEnvironmentResolver extends EnvironmentResolver {

  override def resolve(svcName: String): Environment = PROD

  override def name: String = "DummyProdEnvironmentResolver"
}

object DummyPriorityEnvironmentResolver extends EnvironmentResolver {

  override def resolve(svcName: String): Environment = svcName match {
    case "abc" => QA
    case _ => Default
  }

  override def name: String = "DummyPriorityEnvironmentResolver"
}
