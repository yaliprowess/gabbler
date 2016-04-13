/*
 * Copyright 2016 Heiko Seeberger
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

package de.heikoseeberger.gabbler.user

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import org.scalatest.{ Matchers, WordSpec }

class UserApiSpec extends WordSpec with Matchers with ScalatestRouteTest {

  "UserApi" should {
    "terminate if it can't bind to a socket" in {
      val probe = TestProbe()
      val userApi = system.actorOf(UserApi.props("localhost", 80))
      probe.watch(userApi)
      probe.expectTerminated(userApi)
    }
  }

  "UserApi's route" should {
    """respond to some request with an OK with a "Hello, world!" body""" in {
      Get() ~> UserApi.route ~> check {
        status shouldBe OK
        responseAs[String] shouldBe "Hello, world!"
      }
    }
  }
}
