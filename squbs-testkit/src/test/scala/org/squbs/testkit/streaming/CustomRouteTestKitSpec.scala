/*
 * Copyright 2015 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squbs.testkit.streaming

import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpecLike, Matchers}
import org.squbs.testkit.Timeouts
import org.squbs.unicomplex.streaming.RouteDefinition
import org.squbs.unicomplex.{JMX, UnicomplexBoot}

import scala.util.{Failure, Success}

class CustomRouteTestKitSpec extends CustomRouteTestKit with FlatSpecLike with Matchers {

  it should "return pong on a ping" in {
    val route = TestRoute[MyRoute]
    Get("/ping") ~> route ~> check {
      responseAs[String] should be ("pong")
    }
  }
}

class CustomRouteTestKitWithBootSpec extends CustomRouteTestKit(CustomRouteTestKitWithBootSpec.boot)
with FlatSpecLike with Matchers {

  it should "return pong on a ping" in {
    val route = TestRoute[MyRoute]
    Get("/ping") ~> route ~> check {
      responseAs[String] should be ("pong")
    }
  }

  it should "return response from well-known actor" in {
    val route = TestRoute[ReverserRoute]
    Get("/msg/withBoot") ~> route ~> check {
      responseAs[String] should be ("withBoot".reverse)
    }
  }
}

object CustomRouteTestKitWithBootSpec {

  val config = ConfigFactory.parseString {
    s"""
      |squbs {
      |  actorsystem-name = streaming-CustomRouteTestKitWithBootSpec
      |  ${JMX.prefixConfig} = true
      |}
    """.stripMargin
  }

  lazy val boot = UnicomplexBoot(config)
    .scanResources(getClass.getClassLoader.getResource("").getPath + "/CustomRouteTestKitSpec/META-INF/squbs-meta.conf")
    .start()
}

class CustomRouteTestKitWithActorSystemNameSpec extends CustomRouteTestKit("streaming-CustomRouteTestKitWithActorSystemNameSpecParam")
with FlatSpecLike with Matchers {

  it should "return pong on a ping" in {
    val route = TestRoute[MyRoute]
    Get("/ping") ~> route ~> check {
      responseAs[String] should be ("pong")
    }
  }

  it should "set actor system name to the value passed in constructor" in {
    system.name should equal ("streaming-CustomRouteTestKitWithActorSystemNameSpecParam")
  }
}

class CustomRouteTestKitWithConfigSpec extends CustomRouteTestKit(CustomRouteTestKitWithConfigSpec.config)
with FlatSpecLike with Matchers {

  it should "return pong on a ping" in {
    val route = TestRoute[MyRoute]
    Get("/ping") ~> route ~> check {
      responseAs[String] should be ("pong")
    }
  }

  it should "set actor system name to the value defined in config" in {
    system.name should equal ("streaming-CustomRouteTestKitWithConfigSpecInConfig")
  }
}

object CustomRouteTestKitWithConfigSpec {

  val config = ConfigFactory.parseString {
    s"""
       |squbs {
       |  actorsystem-name = streaming-CustomRouteTestKitWithConfigSpecInConfig
       |  ${JMX.prefixConfig} = true
       |}
    """.stripMargin
  }
}

class CustomRouteTestKitWithResourcesSpec extends CustomRouteTestKit(CustomRouteTestKitWithResourcesSpec.resources,
                                                                     withClassPath = false)
with FlatSpecLike with Matchers {

  it should "return pong on a ping" in {
    val route = TestRoute[MyRoute]
    Get("/ping") ~> route ~> check {
      responseAs[String] should be ("pong")
    }
  }

  it should "return response from well-known actor" in {
    val route = TestRoute[ReverserRoute]
    Get("/msg/withResources") ~> route ~> check {
      responseAs[String] should be ("withResources".reverse)
    }
  }
}

object CustomRouteTestKitWithResourcesSpec {
  val resources = Seq(getClass.getClassLoader.getResource("").getPath + "/CustomRouteTestKitSpec/META-INF/squbs-meta.conf")
}

class CustomRouteTestKitWithActorSystemNameWithResourcesSpec
  extends CustomRouteTestKit("streaming-CustomRouteTestKitWithActorSystemNameWithResourcesSpecParam",
                             CustomRouteTestKitWithResourcesSpec.resources, withClassPath = false)
  with FlatSpecLike with Matchers {

  it should "return pong on a ping" in {
    val route = TestRoute[MyRoute]
    Get("/ping") ~> route ~> check {
      responseAs[String] should be ("pong")
    }
  }

  it should "return response from well-known actor" in {
    val route = TestRoute[ReverserRoute]
    Get("/msg/withActorSystemNameWithResources") ~> route ~> check {
      responseAs[String] should be ("withActorSystemNameWithResources".reverse)
    }
  }

  it should "set actor system name to the value defined in config" in {
    system.name should equal ("streaming-CustomRouteTestKitWithActorSystemNameWithResourcesSpecParam")
  }
}

class CustomRouteTestKitWithConfigWithResourcesSpec
  extends CustomRouteTestKit(CustomRouteTestKitWithConfigWithResourcesSpec.config,
                             CustomRouteTestKitWithConfigWithResourcesSpec.resources,
                             withClassPath = false)
  with FlatSpecLike with Matchers {

  it should "return pong on a ping" in {
    val route = TestRoute[MyRoute]
    Get("/ping") ~> route ~> check {
      responseAs[String] should be ("pong")
    }
  }

  it should "return response from well-known actor" in {
    val route = TestRoute[ReverserRoute]
    Get("/msg/withConfigWithResources") ~> route ~> check {
      responseAs[String] should be ("withConfigWithResources".reverse)
    }
  }

  it should "set actor system name to the value defined in config" in {
    system.name should equal ("streaming-CustomRouteTestKitWithConfigWithResourcesSpecInConfig")
  }
}

object CustomRouteTestKitWithConfigWithResourcesSpec {

  val config = ConfigFactory.parseString {
    s"""
       |squbs {
       |  actorsystem-name = streaming-CustomRouteTestKitWithConfigWithResourcesSpecInConfig
       |  ${JMX.prefixConfig} = true
       |}
    """.stripMargin
  }

  val resources = Seq(getClass.getClassLoader.getResource("").getPath + "/CustomRouteTestKitSpec/META-INF/squbs-meta.conf")
}

class ReverserActor extends Actor {

  override def receive: Receive = {
    case msg: String => sender() ! msg.reverse
  }
}

class ReverserRoute extends RouteDefinition {

  import akka.pattern.ask
  import Timeouts._

  val route =
    path("msg" / Segment) { msg =>
      get {
        onComplete((context.actorSelection("/user/CustomRouteTestKitSpec/reverser") ? msg).mapTo[String]) {
          case Success(value) => complete(value)
          case Failure(ex)    => complete(s"An error occurred: ${ex.getMessage}")
        }
      }
    }
}

