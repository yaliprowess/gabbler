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

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes.{ BadRequest, Conflict, Created, NoContent, NotFound, OK }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Source
import akka.testkit.{ TestActor, TestDuration, TestProbe }
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.CirceSupport
import de.heikoseeberger.akkasse.headers.`Last-Event-ID`
import de.heikoseeberger.akkasse.{ EventStreamUnmarshalling, ServerSentEvent }
import org.scalatest.{ Matchers, WordSpec }
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class UserApiSpec extends WordSpec with Matchers with ScalatestRouteTest {
  import CirceSupport._
  import EventStreamUnmarshalling._
  import UserRepository._
  import io.circe.generic.auto._
  import io.circe.syntax._

  private implicit val timeout = Timeout(1.second.dilated)

  private val user1 = User("user-1", "User One", "user1@gabbler.io")

  "UserApi" should {
    "terminate if it can't bind to a socket" in {
      val probe = TestProbe()
      val userApi = system.actorOf(UserApi.props("localhost", 80, system.deadLetters, 1.second.dilated))
      probe.watch(userApi)
      probe.expectTerminated(userApi)
    }
  }

  "UserApi's UserApi.route" should {
    "respond to GET /users with an OK and a correct Users" in {
      val userRepository = TestProbe(): TestProbe // TODO Remove type ascription once IntelliJ is intelligent enough!
      userRepository.setAutoPilot(new TestActor.AutoPilot {
        override def run(sender: ActorRef, msg: Any) = msg match {
          case GetUsers =>
            sender ! Users(Set(user1))
            TestActor.NoAutoPilot
        }
      })
      Get("/users") ~> UserApi.route(userRepository.ref) ~> check {
        status shouldBe OK
        responseAs[Set[User]] shouldBe Set(user1)
      }
    }

    "respond to an invalid POST /users with a Conflict and a correct error message" in {
      import user1._
      val userRepository = TestProbe(): TestProbe // TODO Remove type ascription once IntelliJ is intelligent enough!
      userRepository.setAutoPilot(new TestActor.AutoPilot {
        override def run(sender: ActorRef, msg: Any) = msg match {
          case AddUser(`username`, `nickname`, `email`) =>
            sender ! UsernameTaken(username)
            TestActor.NoAutoPilot
        }
      })
      Post("/users", AddUser(username, nickname, email)) ~> UserApi.route(userRepository.ref) ~> check {
        status shouldBe Conflict
        responseAs[String] shouldBe s"Username $username taken!"
      }
    }

    "respond to a valid POST /users with a Created a correct User" in {
      import user1._
      val userRepository = TestProbe(): TestProbe // TODO Remove type ascription once IntelliJ is intelligent enough!
      userRepository.setAutoPilot(new TestActor.AutoPilot {
        override def run(sender: ActorRef, msg: Any) = msg match {
          case AddUser(`username`, `nickname`, `email`) =>
            sender ! UserAdded(user1)
            TestActor.NoAutoPilot
        }
      })
      Post("/users", AddUser(username, nickname, email)) ~> UserApi.route(userRepository.ref) ~> check {
        status shouldBe Created
        responseAs[User] shouldBe user1
      }
    }

    "respond to an invalid DELETE /users/<username> with a NotFound and a correct error message" in {
      import user1._
      val userRepository = TestProbe(): TestProbe // TODO Remove type ascription once IntelliJ is intelligent enough!
      userRepository.setAutoPilot(new TestActor.AutoPilot {
        override def run(sender: ActorRef, msg: Any) = msg match {
          case RemoveUser(`username`) =>
            sender ! UsernameUnknown(username)
            TestActor.NoAutoPilot
        }
      })
      Delete(s"/users/$username") ~> UserApi.route(userRepository.ref) ~> check {
        status shouldBe NotFound
        responseAs[String] shouldBe s"Username $username not found!"
      }
    }

    "respond to a valid DELETE /users/<username> with a NoContent" in {
      import user1._
      val userRepository = TestProbe(): TestProbe // TODO Remove type ascription once IntelliJ is intelligent enough!
      userRepository.setAutoPilot(new TestActor.AutoPilot {
        override def run(sender: ActorRef, msg: Any) = msg match {
          case RemoveUser(`username`) =>
            sender ! UserRemoved(username)
            TestActor.NoAutoPilot
        }
      })
      Delete(s"/users/$username") ~> UserApi.route(userRepository.ref) ~> check {
        status shouldBe NoContent
      }
    }

    "respond to a valid GET /user-events with an OK a correct UserEvents" in {
      val userRepository = TestProbe(): TestProbe // TODO Remove type ascription once IntelliJ is intelligent enough!
      userRepository.setAutoPilot(new TestActor.AutoPilot {
        override def run(sender: ActorRef, msg: Any) = msg match {
          case GetUserEvents(1) =>
            sender ! UserEvents(Source(Vector(
              (1L, UserAdded(user1)),
              (2L, UserRemoved(user1.username))
            )))
            TestActor.NoAutoPilot
        }
      })
      Get("/user-events") ~> UserApi.route(userRepository.ref) ~> check {
        status shouldBe OK
        val userEvents = responseAs[Source[ServerSentEvent, Any]]
        val events = Await.result(
          userEvents.take(2).runFold(Vector.empty[ServerSentEvent])(_ :+ _),
          10.seconds.dilated
        )
        events shouldBe Vector(
          ServerSentEvent(user1.asJson.noSpaces, "user-added", "1"),
          ServerSentEvent(user1.username, "user-removed", "2")
        )
      }
    }

    "respond to a valid GET /user-events with the seqNo given by Last-Event-ID with an OK a correct UserEvents" in {
      val userRepository = TestProbe(): TestProbe // TODO Remove type ascription once IntelliJ is intelligent enough!
      userRepository.setAutoPilot(new TestActor.AutoPilot {
        override def run(sender: ActorRef, msg: Any) = msg match {
          case GetUserEvents(2) =>
            sender ! UserEvents(Source(Vector(
              (2L, UserRemoved(user1.username))
            )))
            TestActor.NoAutoPilot
        }
      })
      Get("/user-events").withHeaders(`Last-Event-ID`("1")) ~> UserApi.route(userRepository.ref) ~> check {
        status shouldBe OK
        val eventSource = responseAs[Source[ServerSentEvent, Any]]
        val events = Await.result(
          eventSource.take(2).runFold(Vector.empty[ServerSentEvent])(_ :+ _),
          10.seconds.dilated
        )
        events shouldBe Vector(
          ServerSentEvent(user1.username, "user-removed", "2")
        )
      }
    }

    "respond to a valid GET /user-events with a BadRequest" in {
      Get("/user-events").withHeaders(`Last-Event-ID`("x")) ~> UserApi.route(system.deadLetters) ~> check {
        status shouldBe BadRequest
      }
    }
  }
}
